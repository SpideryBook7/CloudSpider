package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import org.jsoup.Jsoup

class StreamtapeExtractor(private val mainApi: MainAPI) {

    suspend fun extract(url: String): List<MainAPI.ExtractorLink> {
        return try {
            val doc = Jsoup.connect(url).get()
            val scripts = doc.select("script")
            
            var videoUrl: String? = null
            
            for (script in scripts) {
                val html = script.html()
                if (html.contains("document.getElementById('robotlink')")) {
                    // Pattern: document.getElementById('robotlink').innerHTML = '...' + (...)
                    // Usually: document.getElementById('robotlink').innerHTML = '//streamtape.co'+ ('xcdm/get_video...').substring(2).substring(1);
                    
                    // Regex to capture the parts
                    // 1. Base URL (e.g. //streamtape.co)
                    // 2. The string part inside parens
                    // 3. The substring operations
                    
                    val regex = "document\\.getElementById\\('robotlink'\\)\\.innerHTML\\s*=\\s*['\"](.*?)['\"]\\s*\\+\\s*''\\+\\s*\\(['\"](.*?)['\"]\\)(.*);".toRegex()
                    // Adjust regex based on varying patterns. The example showed:
                    // .innerHTML = '//streamtape.co'+ ('xcdm/get_video...').substring(2).substring(1);
                    
                    val simpleRegex = "document\\.getElementById\\('robotlink'\\)\\.innerHTML\\s*=\\s*['\"](.*?)['\"]\\s*\\+\\s*\\(['\"](.*?)['\"]\\)(.*);".toRegex()
                    
                    val match = simpleRegex.find(html)
                    if (match != null) {
                        val baseUrl = match.groupValues[1]
                        var tokenPart = match.groupValues[2]
                        val operations = match.groupValues[3]
                        
                        // Parse substring operations: .substring(2).substring(1)
                        val substringRegex = "\\.substring\\((\\d+)\\)".toRegex()
                        val subMatches = substringRegex.findAll(operations)
                        
                        for (subMatch in subMatches) {
                            val index = subMatch.groupValues[1].toInt()
                            if (index < tokenPart.length) {
                                tokenPart = tokenPart.substring(index)
                            }
                        }
                        
                        val finalUrl = if (baseUrl.startsWith("//")) "https:$baseUrl$tokenPart" else "$baseUrl$tokenPart"
                        videoUrl = finalUrl
                    }
                    break
                }
            }
            
            if (videoUrl != null) {
                listOf(
                    MainAPI.ExtractorLink(
                        name = "Streamtape",
                        url = videoUrl,
                        referer = url,
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
