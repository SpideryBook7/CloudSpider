package com.spiderybook.plugins.extractors

import com.spiderybook.plugins.MainAPI
import okhttp3.OkHttpClient
import okhttp3.Request

class StreamwishExtractor(private val mainApi: MainAPI, private val client: OkHttpClient = OkHttpClient()) {

    suspend fun extract(url: String, refererHeader: String = "https://awish.pro/"): List<MainAPI.ExtractorLink> {
        return try {
            val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            val referer = "https://www4.animeflv.net/"
            
            // Forzamos el mirror embedwish.com (Bypass de Cloudflare natural, ignora bloqueos de awish.pro)
            val mirrorUrl = url.replace("streamwish.to", "embedwish.com")
                               .replace("awish.pro", "embedwish.com")
                               .replace("hglamioz.com", "embedwish.com")

            val req1 = Request.Builder()
                .url(mirrorUrl)
                .header("User-Agent", userAgent)
                .header("Referer", referer)
                .build()

            val response1 = client.newCall(req1).execute()
            val html = response1.body?.string() ?: ""
            response1.close()

            // Desempaquetar JS
            val unpackedHtml = unpackJs(html)
            
            // Buscar el link del video .m3u8 (La estructura cambió a var links = { "hls2": "https://..." })
            var videoUrl: String? = null
            val m3u8Regex = "[\"'](http[s]?://[^\"']+\\.m3u8[^\"']*)[\"']".toRegex()
            val matchObj = m3u8Regex.find(unpackedHtml)
            
            if (matchObj != null) {
                videoUrl = matchObj.groupValues[1]
            } else {
                val backupRegex = "[\"'](/[^\"']+\\.m3u8[^\"']*)[\"']".toRegex()
                val backupMatch = backupRegex.find(unpackedHtml)
                if (backupMatch != null) {
                    videoUrl = "https://embedwish.com" + backupMatch.groupValues[1]
                }
            }

            if (videoUrl != null) {
                listOf(
                    MainAPI.ExtractorLink(
                        name = "Streamwish",
                        url = videoUrl,
                        referer = "https://embedwish.com/", // REFERER GLOBAL: Vital para evitar el 404 en ExoPlayer
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

    private fun unpackJs(packed: String): String {
        val re = """return\s+p\}\('(.*)',\s*(\d+),\s*(\d+),\s*'(.*?)'\.split\('\|'\)""".toRegex()
        val match = re.find(packed) ?: return packed
        
        var p = match.groupValues[1]
        val a = match.groupValues[2].toInt()
        val c = match.groupValues[3].toInt()
        val kArray = match.groupValues[4].split("|")

        for (i in c - 1 downTo 0) {
            val word = if (i < kArray.size && kArray[i].isNotEmpty()) kArray[i] else intToBaseString(i, a)
            if (word.isNotEmpty()) {
                val searchRegex = "\\b${intToBaseString(i, a)}\\b".toRegex()
                p = p.replace(searchRegex, word)
            }
        }
        return p
    }

    private fun intToBaseString(i: Int, radix: Int): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        if (i == 0) return "0"
        var num = i
        var result = ""
        while (num > 0) {
            result = chars[num % radix] + result
            num /= radix
        }
        return result
    }
}
