package com.spiderybook.utils.dlna

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jsoup.Jsoup
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException

class DLNAManager(private val context: Context) {

    private val SSDP_IP = "239.255.255.250"
    private val SSDP_PORT = 1900
    private val SEARCH_MESSAGE = """
        M-SEARCH * HTTP/1.1
        HOST: $SSDP_IP:$SSDP_PORT
        MAN: "ssdp:discover"
        MX: 3
        ST: urn:schemas-upnp-org:device:MediaRenderer:1
    """.trimIndent().replace("\n", "\r\n") + "\r\n\r\n"

    private var localProxyServer: LocalProxyServer? = null

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.contains(":") == false) {
                        return address.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    suspend fun discoverDevices(timeoutMs: Int = 3000): List<DLNADevice> = withContext(Dispatchers.IO) {
        val devices = mutableMapOf<String, DLNADevice>()
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifiManager.createMulticastLock("SpideryBookDLNA")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()

        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            socket.soTimeout = timeoutMs

            val inetAddress = InetAddress.getByName(SSDP_IP)
            val sendData = SEARCH_MESSAGE.toByteArray()
            val sendPacket = DatagramPacket(sendData, sendData.size, inetAddress, SSDP_PORT)
            
            // Send search request
            socket.send(sendPacket)

            val receiveData = ByteArray(1024)
            val startTime = System.currentTimeMillis()

            // Listen for responses
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                try {
                    val receivePacket = DatagramPacket(receiveData, receiveData.size)
                    socket.receive(receivePacket)
                    val response = String(receivePacket.data, 0, receivePacket.length)
                    
                    // Parse LOCATION header (URL to the XML description)
                    val locationMatch = "(?i)LOCATION:\\s*(http://[^\\r\\n]+)".toRegex().find(response)
                    locationMatch?.groups?.get(1)?.value?.let { location ->
                        if (!devices.containsKey(location)) {
                            val device = fetchDeviceDetails(location)
                            if (device != null) {
                                devices[location] = device
                            }
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            socket?.close()
            if (multicastLock.isHeld) {
                multicastLock.release()
            }
        }
        
        return@withContext devices.values.toList()
    }

    private suspend fun fetchDeviceDetails(locationUrl: String): DLNADevice? = withContext(Dispatchers.IO) {
        try {
            // locationUrl provides device properties and sub-service URLs
            val doc = Jsoup.connect(locationUrl).timeout(3000).get()
            
            // Extract the TV's Friendly Name (e.g. "LG webOS TV")
            val friendlyName = doc.select("friendlyName").firstOrNull()?.text() ?: "Unknown TV"
            
            // Locate the AVTransport service to get the Control URL
            val services = doc.select("service")
            var controlUrl: String? = null
            
            for (service in services) {
                val serviceType = service.select("serviceType").text()
                if (serviceType.contains("AVTransport")) {
                    val relativeControlUrl = service.select("controlURL").text()
                    
                    // Construct absolute URL
                    val baseUrl = locationUrl.substringBeforeLast("/") + "/"
                    
                    controlUrl = if (relativeControlUrl.startsWith("/")) {
                        val hostMatch = "(http://[^/]+)".toRegex().find(locationUrl)
                        val host = hostMatch?.groups?.get(1)?.value ?: baseUrl
                        host + relativeControlUrl
                    } else {
                        baseUrl + relativeControlUrl
                    }
                    if (!controlUrl.startsWith("http")) { // Sanitize just in case
                         controlUrl = baseUrl + relativeControlUrl
                    }
                    break
                }
            }
            
            return@withContext DLNADevice(name = friendlyName, location = locationUrl, avTransportControlUrl = controlUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun playMedia(device: DLNADevice, mediaUrl: String, mediaReferer: String? = null): Boolean = withContext(Dispatchers.IO) {
        val controlUrl = device.avTransportControlUrl ?: return@withContext false
        android.util.Log.d("SpideryDebug", "Attempting DLNA Cast to TV: ${device.name}, ControlURL: $controlUrl, MediaURL: $mediaUrl")
        
        var finalMediaUrl = mediaUrl

        // We must bridge the connection through a local Android Proxy Server.
        val localIp = getLocalIpAddress()
        if (localIp != null) {
            try {
                localProxyServer?.stop()
                localProxyServer = LocalProxyServer(8192)
                
                val headers = mutableMapOf<String, String>()
                headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                if (!mediaReferer.isNullOrEmpty()) {
                    headers["Referer"] = mediaReferer
                }
                
                localProxyServer?.setMediaSource(mediaUrl, headers)
                localProxyServer?.start()

                val extension = if (mediaUrl.contains(".m3u8", ignoreCase = true)) ".m3u8" else ".mp4"
                finalMediaUrl = "http://$localIp:8192/video$extension"
                android.util.Log.d("SpideryDebug", "Local proxy started for DLNA relay at $finalMediaUrl")
            } catch (e: Exception) {
                android.util.Log.e("SpideryDebug", "Failed to start local proxy", e)
            }
        }

        val isM3u8 = finalMediaUrl.contains(".m3u8", ignoreCase = true)
        val mimeType = if (isM3u8) "application/vnd.apple.mpegurl" else "video/mp4"
        val protocolInfoStr = if (isM3u8) {
             "http-get:*:$mimeType:DLNA.ORG_PN=APPLE_HTTP_LIVE_STREAMING;DLNA.ORG_OP=11;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000"
        } else {
             "http-get:*:$mimeType:DLNA.ORG_PN=AVC_MP4_BL_CIF15_AAC_520;DLNA.ORG_OP=11;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000"
        }

        // Minimal generic DIDL-Lite required by WebOS TVs to avoid Error 500
        val didlLite = """
            <DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/" xmlns:sec="http://www.sec.co.kr/">
                <item id="1" parentID="0" restricted="1">
                    <dc:title>SpideryBook Video</dc:title>
                    <upnp:class>object.item.videoItem</upnp:class>
                    <sec:CaptionInfoEx sec:type="srt"></sec:CaptionInfoEx>
                    <res protocolInfo="$protocolInfoStr">${finalMediaUrl.replace("&", "&amp;")}</res>
                </item>
            </DIDL-Lite>
        """.trimIndent().replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

        // 1. Send SetAVTransportURI (Tells TV what to play)
        val setURIEnvelope = """
            <?xml version="1.0" encoding="utf-8"?>
            <s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
                <s:Body>
                    <u:SetAVTransportURI xmlns:u="urn:schemas-upnp-org:service:AVTransport:1">
                        <InstanceID>0</InstanceID>
                        <CurrentURI>${finalMediaUrl.replace("&", "&amp;")}</CurrentURI>
                        <CurrentURIMetaData>$didlLite</CurrentURIMetaData>
                    </u:SetAVTransportURI>
                </s:Body>
            </s:Envelope>
        """.trimIndent()

        // 2. Send Play (Commands TV to start buffer/playback)
        val playEnvelope = """
            <?xml version="1.0" encoding="utf-8"?>
            <s:Envelope s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
                <s:Body>
                    <u:Play xmlns:u="urn:schemas-upnp-org:service:AVTransport:1">
                        <InstanceID>0</InstanceID>
                        <Speed>1</Speed>
                    </u:Play>
                </s:Body>
            </s:Envelope>
        """.trimIndent()

        return@withContext withTimeoutOrNull(15000L) {
            try {
                // 1. Send SetAVTransportURI
                val setUriResponse = Jsoup.connect(controlUrl)
                    .timeout(10000)
                    .method(org.jsoup.Connection.Method.POST)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true) // vital to read SOAP Faults Instead of crashing
                    .header("Content-Type", "text/xml; charset=\"utf-8\"")
                    .header("SOAPAction", "\"urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI\"")
                    .header("User-Agent", "Android/14 UPnP/1.1 SpideryBook/1.0")
                    .header("Connection", "close")
                    .requestBody(setURIEnvelope)
                    .execute()

                if (setUriResponse.statusCode() != 200) {
                    android.util.Log.e("SpideryDebug", "DLNA SetURI Failed (${setUriResponse.statusCode()}): ${setUriResponse.body()}")
                    return@withTimeoutOrNull false
                }
                
                android.util.Log.d("SpideryDebug", "DLNA SetURI Success. Waiting for TV buffer...")
                kotlinx.coroutines.delay(1500) // Give TV 1.5s to parse the M3U8 playlist before commanding PLAY

                // 2. Send Play
                val playResponse = Jsoup.connect(controlUrl)
                    .timeout(10000)
                    .method(org.jsoup.Connection.Method.POST)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .header("Content-Type", "text/xml; charset=\"utf-8\"")
                    .header("SOAPAction", "\"urn:schemas-upnp-org:service:AVTransport:1#Play\"")
                    .header("User-Agent", "Android/14 UPnP/1.1 SpideryBook/1.0")
                    .header("Connection", "close")
                    .requestBody(playEnvelope)
                    .execute()
                
                if (playResponse.statusCode() != 200) {
                    android.util.Log.e("SpideryDebug", "DLNA Play Failed (${playResponse.statusCode()}): ${playResponse.body()}")
                    // Some TVs start playing automatically after SetURI, so Play failing is not always fatal
                } else {
                    android.util.Log.d("SpideryDebug", "DLNA Play Success.")
                }
                
                true
            } catch (e: java.net.SocketTimeoutException) {
                android.util.Log.w("SpideryDebug", "DLNA command timed out, but TV might still be buffering. Assuming success.")
                true
            } catch (e: Exception) {
                android.util.Log.e("SpideryDebug", "DLNA General Error: ${e.message}")
                e.printStackTrace()
                false
            }
        } ?: false
}
}
