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
            
            val results = mutableListOf<MainAPI.ExtractorLink>()
            
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
                results.add(
                    MainAPI.ExtractorLink(
                        name = "Streamwish",
                        url = videoUrl,
                        referer = "https://embedwish.com/",
                        quality = 0,
                        isM3u8 = true
                    )
                )
            }
            
            // NEW: Search for MP4 fallback for Native Android DownloadManager
            val mp4Regex = "[\"'](http[s]?://[^\"'\\s]+\\.mp4[^\"'\\s]*)[\"']".toRegex(RegexOption.IGNORE_CASE)
            mp4Regex.findAll(unpackedHtml).forEach { mp4Match ->
                val mp4Url = mp4Match.groupValues[1]
                if (!results.any { it.url == mp4Url }) {
                    results.add(
                        MainAPI.ExtractorLink(
                            name = "Streamwish (MP4)",
                            url = mp4Url,
                            referer = "https://embedwish.com/",
                            quality = 0,
                            isM3u8 = false
                        )
                    )
                }
            }

            return results
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun unpackJs(packed: String): String {
        val re = """return\s+p\}\('(.*)',\s*(\d+),\s*(\d+),\s*'(.*?)'\.split\('\|'\)""".toRegex()
        val match = re.find(packed) ?: return packed
        
        val p = match.groupValues[1]
        val a = match.groupValues[2].toInt()
        val c = match.groupValues[3].toInt()
        val kArray = match.groupValues[4].split("|")

        // Optimize: One-pass regex replace to avoid CPU UI freezing!
        val replacerRegex = "\\b[a-zA-Z0-9]+\\b".toRegex()
        return replacerRegex.replace(p) { matchResult ->
            val wordBase = matchResult.value
            val index = baseStringToInt(wordBase, a)
            if (index < c && index < kArray.size && kArray[index].isNotEmpty()) {
                kArray[index]
            } else {
                wordBase
            }
        }
    }

    private fun baseStringToInt(str: String, radix: Int): Int {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var num = 0
        for (char in str) {
            val v = chars.indexOf(char)
            if (v < 0) return 0 // Ignore invalid chars
            num = num * radix + v
        }
        return num
    }

    // Function removed because it is explicitly not needed given the inverse mapping logic above
}
