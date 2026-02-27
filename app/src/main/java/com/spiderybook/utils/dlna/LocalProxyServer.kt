package com.spiderybook.utils.dlna

import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class LocalProxyServer(port: Int) : NanoHTTPD(port) {

    private var targetUrl: String? = null
    private var headersToInject: Map<String, String> = emptyMap()

    fun setMediaSource(url: String, headers: Map<String, String>) {
        this.targetUrl = url
        this.headersToInject = headers
    }

    override fun serve(session: IHTTPSession): Response {
        val urlToProxy = targetUrl ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "No media source set")
        
        try {
            android.util.Log.d("SpideryProxy", "--- TV Request: ${session.method.name} ${session.uri} ---")
            
            val url = URL(urlToProxy)
            val connection = url.openConnection() as HttpURLConnection
            
            // 1. Pass through headers requested by the TV (like Range for seeking)
            session.headers.forEach { (key, value) ->
                android.util.Log.d("SpideryProxy", "TV Header -> $key: $value")
                // Avoid overriding host or connection specific headers that would confuse the remote server
                if (!key.equals("host", ignoreCase = true) && !key.equals("connection", ignoreCase = true)) {
                    connection.setRequestProperty(key, value)
                }
            }

            // 2. Inject our Anti-Leech bypass headers (Referer, User-Agent, etc)
            headersToInject.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            connection.requestMethod = session.method.name
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // Connect to remote server
            connection.connect()

            val responseCode = connection.responseCode
            android.util.Log.d("SpideryProxy", "Remote Response Code: $responseCode")
            
            val status = when (responseCode) {
                200 -> Response.Status.OK
                206 -> Response.Status.PARTIAL_CONTENT
                else -> Response.Status.lookup(responseCode) ?: Response.Status.INTERNAL_ERROR
            }

            val inputStream: InputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: "".byteInputStream()
            }

            val contentType = connection.contentType ?: MIME_PLAINTEXT
            val contentLength = connection.getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
            android.util.Log.d("SpideryProxy", "Remote Content-Type: $contentType, Length: $contentLength")

            // --- M3U8 PROXY BYPASS (REDIRECT) ---
            // Live Demuxing causes WebOS Error 501 (Action Failed) because it lacks a finite Content-Length.
            // Text M3U8 spoofing causes WebOS Error 704 (Format Not Supported) or 500.
            // The only 100% reliable method for modern LG TVs is to issue an HTTP 302 Redirect.
            // The Android DLNA Manager casts the "Local Proxy URL", but when the TV connects, 
            // the proxy instantly redirects the TV to the real CDN URL. The TV's native networking stack takes over.
            if (urlToProxy.contains(".m3u8", ignoreCase = true) || contentType.contains("mpegurl", ignoreCase = true)) {
                android.util.Log.d("SpideryProxy", "M3U8 detected. Issuing HTTP 302 Redirect to TV: $urlToProxy")
                val response = newFixedLengthResponse(Response.Status.REDIRECT, MIME_PLAINTEXT, "")
                response.addHeader("Location", urlToProxy)
                return response
            }

            // --- STANDARD VIDEO LOGIC (MP4, chunks) ---
            // Pipe the raw remote video stream back to the LG TV
            val response = newChunkedResponse(status, contentType, inputStream)
            
            // Pass remote response headers back to TV (like Accept-Ranges, Content-Range)
            val headerFields = connection.headerFields
            for ((headerName, headerValues) in headerFields) {
                if (headerName != null && headerValues.isNotEmpty()) {
                    response.addHeader(headerName, headerValues[0])
                }
            }

            return response

        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, e.message)
        }
    }
}
