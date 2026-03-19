package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import okhttp3.OkHttpClient
import okhttp3.Request

class StreamtapeExtractor(private val mainApi: MainAPI, private val client: OkHttpClient = OkHttpClient()) {

    suspend fun extract(url: String, refererHeader: String = "https://streamtape.com/"): List<MainAPI.ExtractorLink> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                .header("Referer", "https://www4.animeflv.net/")
                .build()
            
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            response.close()
            
            // Buscar la línea mágica de Streamtape
            // Ejemplo: document.getElementById('robotlink').innerHTML = '//streamtape.com/g'+ ('xcdet_video?id=...').substring(2).substring(1);
            val magicRegex = "document\\.getElementById\\('robotlink'\\)\\.innerHTML\\s*=\\s*(.*?);".toRegex()
            val match = magicRegex.find(html)

            if (match != null) {
                val code = match.groupValues[1]
                
                val domainMatch = "'([^']+)'".toRegex().find(code)
                val tokenMatch = "\\+\\s*\\('([^']+)'\\)".toRegex().find(code)
                val substrings = "\\.substring\\((\\d+)\\)".toRegex().findAll(code)

                if (domainMatch != null && tokenMatch != null) {
                    val domainUrl = domainMatch.groupValues[1]
                    var token = tokenMatch.groupValues[1]

                    for (subMatch in substrings) {
                        val index = subMatch.groupValues[1].toInt()
                        if (index <= token.length) {
                            token = token.substring(index)
                        }
                    }

                    var finalVideoUrl = domainUrl + token
                    if (finalVideoUrl.startsWith("//")) {
                        finalVideoUrl = "https:$finalVideoUrl"
                    }
                    
                    return listOf(
                        MainAPI.ExtractorLink(
                            name = "Streamtape",
                            url = finalVideoUrl,
                            referer = refererHeader,
                            quality = 0,
                            isM3u8 = false
                        )
                    )
                }
            }
            
            return emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}