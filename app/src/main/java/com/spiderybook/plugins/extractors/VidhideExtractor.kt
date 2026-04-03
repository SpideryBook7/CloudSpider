package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import com.spiderybook.util.PackedDecoder
import org.jsoup.Jsoup

class VidhideExtractor(private val mainApi: MainAPI) {

    suspend fun extract(url: String): List<MainAPI.ExtractorLink> {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .get()
            
            val scripts = doc.select("script")
            var videoUrl: String? = null

            for (script in scripts) {
                val html = script.html()
                if (html.contains("eval(function(p,a,c,k,e,d)")) {
                    val unpacked = PackedDecoder.decode(html)
                    if (unpacked != null) {
                        // Strategy 1: Loosely parse "links" object (var/let/const links = { ... })
                        // We capture the content inside the curly braces
                        val linksMatch = Regex("(?:var|let|const)\\s+links\\s*=\\s*\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL).find(unpacked)
                        
                        if (linksMatch != null) {
                            val linksContent = linksMatch.groupValues[1]
                            // Extract values key:"value" allowing for potential mix of quotes
                            val urlMatches = Regex("[\"'](hls\\d*)[\"']\\s*:\\s*[\"'](.*?)[\"']").findAll(linksContent)
                            
                            val candidates = mutableListOf<String>()
                            for (match in urlMatches) {
                                // Clean up the URL: remove whitespace, unescape slashes
                                var cleanedUrl = match.groupValues[2].replace("\\s".toRegex(), "")
                                cleanedUrl = cleanedUrl.replace("\\/", "/")
                                candidates.add(cleanedUrl)
                            }
                            
                            // Prioritize absolute URLs (hls2/hls3 often direct CDN)
                            val bestUrl = candidates.firstOrNull { it.startsWith("http") } 
                                       ?: candidates.firstOrNull()
                                       
                            if (bestUrl != null && (bestUrl.contains(".m3u8") || bestUrl.contains(".txt"))) {
                                videoUrl = if (bestUrl.startsWith("http")) {
                                    bestUrl
                                } else {
                                    val uri = java.net.URI(url)
                                    val scheme = uri.scheme ?: "https"
                                    val host = uri.host ?: "vidhideplus.com"
                                    "$scheme://$host$bestUrl" 
                                }
                            }
                        }

                        if (videoUrl == null) {
                            // Strategy 2: Broad scan for any absolute https://...m3u8
                            val robustM3u8Regex = Regex("[\"'](https?://[^\"'\\s]+\\.m3u8[^\"']*)[\"']", RegexOption.IGNORE_CASE)
                            val m3u8Match = robustM3u8Regex.find(unpacked)
                            if (m3u8Match != null) {
                                videoUrl = m3u8Match.groupValues[1].replace("\\/", "/")
                            } else {
                                // Strategy 3: Relative path fallback
                                val relativeRegex = Regex("[\"'](/[^\"'\\s]+\\.m3u8.*?)[\"']")
                                val relMatch = relativeRegex.find(unpacked)
                                if (relMatch != null) {
                                    val path = relMatch.groupValues[1]
                                    val uri = java.net.URI(url)
                                    val scheme = uri.scheme ?: "https"
                                    val host = uri.host ?: "vidhideplus.com"
                                    videoUrl = "$scheme://$host$path"
                                }
                            }
                        }
                    }
                    if (videoUrl != null) break
                }
            }

            val results = mutableListOf<MainAPI.ExtractorLink>()
            
            if (videoUrl != null) {
                // Resolve relative URL if needed
                if (videoUrl.startsWith("/")) {
                    val uri = java.net.URI(url)
                    val scheme = uri.scheme ?: "https"
                    val host = uri.host ?: "vidhideplus.com"
                    videoUrl = "$scheme://$host$videoUrl"
                }

                results.add(
                    MainAPI.ExtractorLink(
                        name = "Vidhide",
                        url = videoUrl,
                        referer = url, 
                        quality = 0,
                        isM3u8 = true
                    )
                )
            }
            
            // NEW: Hunt for MP4 fallback inside scripts for OS DownloadManager
            for (script in scripts) {
                val html = script.html()
                val mp4FallbackRegex = "[\"'](http[s]?://[^\"'\\s]+\\.mp4[^\"'\\s]*)[\"']".toRegex(RegexOption.IGNORE_CASE)
                
                // Scan both raw script and unpacked if it exists
                val contentToScan = if (html.contains("eval(function(p,a,c,k,e,d)")) {
                    PackedDecoder.decode(html) ?: html
                } else {
                    html
                }
                
                mp4FallbackRegex.findAll(contentToScan).forEach { mp4Match ->
                    val mp4Url = mp4Match.groupValues[1].replace("\\/", "/")
                    if (!results.any { it.url == mp4Url }) {
                        results.add(
                            MainAPI.ExtractorLink(
                                name = "Vidhide (MP4)",
                                url = mp4Url,
                                referer = url,
                                quality = 0,
                                isM3u8 = false
                            )
                        )
                    }
                }
            }

            return results
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}
