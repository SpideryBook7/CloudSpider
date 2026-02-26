package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import com.spiderybook.util.PackedDecoder
import org.jsoup.Jsoup

class StreamwishExtractor(private val mainApi: MainAPI) {

    suspend fun extract(url: String): List<MainAPI.ExtractorLink> {
        return try {
            val doc = Jsoup.connect(url)
                // Use the exact Chrome 120 User-Agent verified for Streamwish
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0")
                .header("Referer", "https://streamwish.to/")
                .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                .timeout(10000)
                .get()
                
            val scripts = doc.select("script")
            var m3u8Url: String? = null
            // Streamwish embeds usually use JWPlayer or similar HTML5 players where the source is injected into a JS object
            // Use a robust regex to find any m3u8 link
            val regex = Regex("https?://[^\"'\\s]+\\.m3u8", RegexOption.IGNORE_CASE)
            
            // 1. Try to find the m3u8 link inside obfuscated Javascript (eval(function(p,a,c,k,e,d)...))
            for (script in scripts) {
                val scriptContent = script.html()
                if (scriptContent.contains("eval(function(p,a,c,k,e,d)")) {
                    val unpacked = PackedDecoder.decode(scriptContent)
                    if (unpacked != null) {
                        val match = regex.find(unpacked)
                        if (match != null) {
                            m3u8Url = match.value
                            break
                        }
                    }
                }
            }
            
            // 2. Fallback to searching the raw HTML if it wasn't packed
            if (m3u8Url == null) {
                val html = doc.html()
                val match = regex.find(html)
                if (match != null) {
                    m3u8Url = match.value
                }
            }
            
            if (m3u8Url != null) {
                listOf(
                    MainAPI.ExtractorLink(
                        name = "Streamwish",
                        url = m3u8Url,
                        referer = "https://streamwish.to/", // strict referer
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
