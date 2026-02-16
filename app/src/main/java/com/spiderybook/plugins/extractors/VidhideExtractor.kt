package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import com.spiderybook.util.PackedDecoder
import org.jsoup.Jsoup

class VidhideExtractor(private val mainApi: MainAPI) {

    suspend fun extract(url: String): List<MainAPI.ExtractorLink> {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
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
                                       
                            if (bestUrl != null && (bestUrl.contains(".m3u8") || bestUrl.contains(".txt"))) { // sometimes they obscure extensions
                                videoUrl = if (bestUrl.startsWith("http")) {
                                    bestUrl
                                } else {
                                    // Relative URL resolution
                                    val uri = java.net.URI(url)
                                    val scheme = uri.scheme ?: "https"
                                    val host = uri.host ?: "vidhideplus.com" // Fallback only if host is null
                                    "$scheme://$host$bestUrl" 
                                }
                            }
                        }

                        if (videoUrl == null) {
                            val robustM3u8Regex = Regex("https?://[^\"'\\s]+\\.m3u8", RegexOption.IGNORE_CASE)
                            val match = robustM3u8Regex.find(unpacked)
                            if (match != null) {
                                videoUrl = match.value.replace("\\/", "/")
                            } else {
                                // Try relative path fallback
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

            if (videoUrl != null) {
                // Formatting fix for relative url if needed (e.g. from Strategy 2 or fallback)
                if (videoUrl.startsWith("/")) {
                     val uri = java.net.URI(url)
                     val scheme = uri.scheme ?: "https"
                     val host = uri.host ?: "vidhideplus.com"
                     videoUrl = "$scheme://$host$videoUrl"
                }

                listOf(
                    MainAPI.ExtractorLink(
                        name = "Vidhide",
                        url = videoUrl,
                        referer = url, // IMPORTANT: The player needs the Embed URL as Referer
                        quality = 0 
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
