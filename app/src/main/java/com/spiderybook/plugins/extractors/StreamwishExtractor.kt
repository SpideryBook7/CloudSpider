package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import com.spiderybook.util.PackedDecoder
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI

class StreamwishExtractor(private val mainApi: MainAPI) {

    suspend fun extract(url: String): List<MainAPI.ExtractorLink> {
        return try {
            // AnimeFLV often returns 'streamwish.to' links which now just serve an empty JS redirect.
            // By replacing the host with an active mirror (like hglamioz.com), we skip the JS execution requirement and go straight to the video data.
            var finalUrl = url
            if (finalUrl.contains("streamwish.to")) {
                finalUrl = finalUrl.replace("streamwish.to", "hglamioz.com")
            }

            // Dynamically get the base URL for the referer (e.g., https://hglamioz.com/)
            val uri = URI(finalUrl)
            val host = uri.host
            val scheme = uri.scheme
            val baseUrl = "$scheme://$host/"

            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            val request = Request.Builder()
                .url(finalUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Referer", baseUrl)
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.body?.close()
                
            var m3u8Url: String? = null
            // Streamwish embeds usually use JWPlayer or similar HTML5 players where the source is injected into a JS object
            // Use a robust regex to find any m3u8 link including its query parameters (which act as CDN access tokens)
            val regex = Regex("https?://[^\"'\\s]+\\.m3u8[^\"'\\s]*", RegexOption.IGNORE_CASE)
            
            // 1. Try to find the m3u8 link inside obfuscated Javascript (eval(function(p,a,c,k,e,d)...))
            // We search the entire HTML string directly because the unpacked/packed JS might be scattered
            val packedMatch = Regex("eval\\s*\\(\\s*function\\s*\\(p,a,c,k,e,d\\).*?\\{.*?\\}\\s*\\('(.*?)'\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*'(.*?)'\\.split\\('\\|'\\)", RegexOption.DOT_MATCHES_ALL).find(html)
            
            if (packedMatch != null) {
                val unpacked = PackedDecoder.decode(html)
                if (unpacked != null) {
                    val match = regex.find(unpacked)
                    if (match != null) {
                        m3u8Url = match.value
                    }
                }
            }
            
            // 2. Fallback to searching the raw HTML if it wasn't packed or extraction failed
            if (m3u8Url == null) {
                val match = regex.find(html)
                if (match != null) {
                    m3u8Url = match.value
                } else {
                    // 3. Fallback: Search for specifically JWPlayer 'file:' or 'sources:' configuration
                    val fileMatch = Regex("file\\s*:\\s*[\"'](https?://[^\"']+\\.m3u8.*?)[\"']", RegexOption.IGNORE_CASE).find(html)
                    if (fileMatch != null) {
                        m3u8Url = fileMatch.groupValues[1]
                    }
                }
            }
            
            if (m3u8Url != null) {
                listOf(
                    MainAPI.ExtractorLink(
                        name = "Streamwish",
                        url = m3u8Url,
                        referer = baseUrl, // strict referer matching host
                        quality = 0,
                        isM3u8 = true
                    )
                )
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
