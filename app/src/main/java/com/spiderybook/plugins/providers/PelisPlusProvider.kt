

package com.spiderybook.plugins.providers

import android.util.Base64
import com.spiderybook.domain.model.Episode
import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.domain.model.LoadResponse
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.MainAPI
import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets

class PelisPlusProvider : MainAPI() {
    override val name = "PelisPlus"
    override val mainUrl = "https://tioplus.app"

    override suspend fun getMainPage(page: Int): HomePageResponse? {
        val url = "$mainUrl/peliculas"
        return try {
            val doc = Jsoup.connect(url).get()
            val items = doc.select("article.item")
            val list = mutableListOf<SearchResponse>()
            for (it in items) {
                val title = it.select("h2").text()
                val link = it.select("a").attr("href")
                val poster = it.select("img").attr("data-src").ifEmpty { it.select("img").attr("src") }
                if (title.isNotEmpty() && link.isNotEmpty()) {
                    list.add(
                        SearchResponse(
                            name = title,
                            url = link,
                            apiName = name,
                            type = TvType.Movie,
                            posterUrl = if (poster.startsWith("http")) poster else "$mainUrl$poster",
                            year = null
                        )
                    )
                }
            }
            HomePageResponse(listOf(HomePageList("Películas", list)))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun search(query: String): List<SearchResponse>? {
        val url = "$mainUrl/search/$query"
        return try {
            val doc = Jsoup.connect(url).get()
            val items = doc.select("article.item")
            val list = mutableListOf<SearchResponse>()
            for (it in items) {
                val title = it.select("h2").text()
                val link = it.select("a").attr("href")
                val poster = it.select("img").attr("data-src").ifEmpty { it.select("img").attr("src") }
                if (title.isNotEmpty() && link.isNotEmpty()) {
                    list.add(
                        SearchResponse(
                            name = title,
                            url = link,
                            apiName = name,
                            type = TvType.Movie,
                            posterUrl = if (poster.startsWith("http")) poster else "$mainUrl$poster",
                            year = null
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        return try {
            val doc = Jsoup.connect(url).get()
            val title = doc.select("h1.slugh1").text()
            val poster = doc.select(".bg").attr("style").substringAfter("url(\"").substringBefore("\")")
            val synopsis = doc.select(".description p").text()
            val rating = doc.select(".genres.rating span:contains(Rating)").text().replace("Rating:", "").trim().toDoubleOrNull()
            val year = doc.select(".genres.rating span:contains(Año) a").text().toIntOrNull()

            val episodes = listOf(Episode("Película", url, 1, 1))

            LoadResponse(
                url = url,
                name = title,
                apiName = name,
                type = TvType.Movie,
                posterUrl = if (poster.startsWith("http")) poster else "https://image.tmdb.org/t/p/w500$poster",
                year = year,
                plot = synopsis,
                rating = rating,
                episodes = episodes
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun loadLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        return try {
            val doc = Jsoup.connect(data).get()
            val options = doc.select(".subselect li")
            for (option in options) {
                val serverName = option.select("span").first()?.text() ?: "Server"
                val dataServer = option.attr("data-server")
                if (dataServer.isNotEmpty()) {
                    try {
                        // JS Logic: btoa(dataServer) -> Encoded, not decoded
                        val encoded = Base64.encodeToString(dataServer.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                        val playerUrl = "$mainUrl/player/$encoded"
                        
                        // Fetch the player page with a proper User-Agent
                        val playerDoc = Jsoup.connect(playerUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .get()
                        
                        var targetUrl = ""

                        // 1. Check for JS redirect (window.location.href)
                        val scripts = playerDoc.select("script")
                        for (script in scripts) {
                            val content = script.html()
                            val match = Regex("window\\.location\\.href\\s*=\\s*'([^']+)'").find(content)
                            if (match != null) {
                                targetUrl = match.groupValues[1]
                                break
                            }
                        }

                        // 2. Fallback to iframe
                        if (targetUrl.isEmpty()) {
                            targetUrl = playerDoc.select("iframe").attr("src")
                        }

                        if (targetUrl.isNotEmpty()) {
                            val resolvedSrc = if (targetUrl.startsWith("//")) "https:$targetUrl" else targetUrl
                            
                            if (resolvedSrc.contains("streamtape") || resolvedSrc.contains("stape")) {
                                val extractor = com.spiderybook.plugins.extractors.StreamtapeExtractor(this)
                                val links = extractor.extract(resolvedSrc)
                                for (link in links) {
                                    callback(link)
                                }
                            } else if (resolvedSrc.contains("vidhide") || resolvedSrc.contains("filemoon") || resolvedSrc.contains("moonplayer")) {
                                val extractor = com.spiderybook.plugins.extractors.VidhideExtractor(this)
                                val links = extractor.extract(resolvedSrc)
                                for (link in links) {
                                    callback(link)
                                }
                            } else {
                                callback(
                                    ExtractorLink(
                                        name = serverName,
                                        url = resolvedSrc,
                                        referer = playerUrl,
                                        quality = 0
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

