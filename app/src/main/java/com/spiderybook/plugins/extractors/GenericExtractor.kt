package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import org.jsoup.Jsoup

class GenericExtractor(private val mainApi: MainAPI) {

    suspend fun extract(url: String, serverName: String = "Generic", referer: String = ""): List<MainAPI.ExtractorLink> {
        return try {
            val response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .referrer(referer) // Use the passed referer
                .ignoreContentType(true) // Important: Allow non-html content types
                .ignoreHttpErrors(true) // Try to process even if 403/404 (protection pages)
                .timeout(10000) // 10s timeout: Give slow servers/unpacking a chance
                .execute()

            val contentType = response.contentType()
            
            // 1. If it's a direct video stream, return it immediately
            if (contentType != null && (contentType.startsWith("video/") || contentType.contains("application/vnd.apple.mpegurl") || contentType.contains("application/x-mpegURL"))) {
                return listOf(
                    MainAPI.ExtractorLink(
                        name = "$serverName - 2. Direct Stream",
                        url = url,
                        referer = referer, // Use passed referer
                        quality = 0
                    )
                )
            }

            // 2. If it's HTML, parse it for links
            val doc = response.parse()
            val html = doc.html()
            
            val foundLinks = mutableListOf<MainAPI.ExtractorLink>()
            
            // Helper to scan text for links
            fun scanText(text: String) {
                // Regex to find m3u8 or mp4 links (http/https)
                val m3u8Regex = Regex("[\"'](https?://[^\"'\\s]+\\.m3u8[^\"'\\s]*)[\"']", RegexOption.IGNORE_CASE)
                val mp4Regex = Regex("[\"'](https?://[^\"'\\s]+\\.mp4[^\"'\\s]*)[\"']", RegexOption.IGNORE_CASE)
                
                // Common player patterns: file:"url", src:"url"
                val fileRegex = Regex("file\\s*:\\s*[\"'](https?://[^\"'\\s]+)[\"']", RegexOption.IGNORE_CASE)
                val srcRegex = Regex("src\\s*:\\s*[\"'](https?://[^\"'\\s]+)[\"']", RegexOption.IGNORE_CASE)

                m3u8Regex.findAll(text).forEach { match ->
                    val link = match.groupValues[1].replace("\\/", "/")
                    if (!foundLinks.any { it.url == link }) {
                         foundLinks.add(MainAPI.ExtractorLink("$serverName - 2. HLS", link, referer, 0))
                    }
                }

                mp4Regex.findAll(text).forEach { match ->
                    val link = match.groupValues[1].replace("\\/", "/")
                    if (!foundLinks.any { it.url == link }) {
                         foundLinks.add(MainAPI.ExtractorLink("$serverName - 2. MP4", link, referer, 0))
                    }
                }
                
                // Scan for file/src matches that generally point to video
                fileRegex.findAll(text).forEach { match ->
                     val link = match.groupValues[1].replace("\\/", "/")
                     if ((link.contains(".mp4") || link.contains(".m3u8")) && !foundLinks.any { it.url == link }) {
                          foundLinks.add(MainAPI.ExtractorLink("$serverName - 2. Player", link, referer, 0))
                     }
                }
            }

            // 2. Scan raw HTML
            scanText(html)

            // 2. Scan packed JavaScript (eval(function...))
            if (html.contains("eval(function(p,a,c,k,e,d)")) {
                val packedRegex = Regex("eval\\s*\\(\\s*function\\s*\\(p,a,c,k,e,d\\).*?\\{.*?\\}\\s*\\('(.*?)'\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*'(.*?)'\\.split\\('\\|'\\)", RegexOption.DOT_MATCHES_ALL)
                packedRegex.findAll(html).forEach { match ->
                    try {
                        val p = match.groupValues[1]
                        val a = match.groupValues[2].toInt()
                        val c = match.groupValues[3].toInt()
                        val k = match.groupValues[4].split("|")
                        val unpacked = com.spiderybook.util.PackedDecoder.unpack(p, a, c, k)
                        scanText(unpacked)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            foundLinks
        } catch (e: Exception) {
            e.printStackTrace()
            // No fallback here. PelisPlusProvider adds the Raw link manually.
            emptyList()
        }
    }
}
