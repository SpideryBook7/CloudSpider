package com.spiderybook.plugins.providers

import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.HomePageResponse
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.MainAPI
import org.jsoup.Jsoup
import javax.inject.Inject

class AnimeFlvProvider @Inject constructor() : MainAPI() {
    override val name = "AnimeFLV"
    override val mainUrl = "https://www3.animeflv.net"
    
    override suspend fun getMainPage(page: Int): HomePageResponse? {
        return try {
            val doc = Jsoup.connect(mainUrl).get()
            val items = mutableListOf<HomePageList>()

            // 1. Latest Episodes
            val episodeList = mutableListOf<SearchResponse>()
            val episodeElements = doc.select("ul.ListEpisodios li")
            for (element in episodeElements) {
                val title = element.select("strong.Title").text()
                val episodeText = element.select("span.Capi").text()
                val link = element.select("a").attr("href")
                val imagePath = element.select("span.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"
                
                // Combine title and episode for display
                val displayTitle = "$title - $episodeText"

                episodeList.add(
                    SearchResponse(
                        name = displayTitle,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            if (episodeList.isNotEmpty()) {
                items.add(HomePageList("Últimos Episodios", episodeList, isHorizontal = true))
            }

            // 2. Latest Animes
            val animeList = mutableListOf<SearchResponse>()
            val animeElements = doc.select("ul.ListAnimes li")
            for (element in animeElements) {
                val title = element.select("h3.Title").text()
                val typeText = element.select("span.Type").text()
                val link = element.select("article a").attr("href")
                val imagePath = element.select("div.Image img").attr("src")
                val imageUrl = if (imagePath.startsWith("http")) imagePath else "https://animeflv.net$imagePath"

                animeList.add(
                    SearchResponse(
                        name = title,
                        url = if (link.startsWith("http")) link else "$mainUrl$link",
                        apiName = name,
                        type = TvType.Anime,
                        posterUrl = imageUrl,
                        year = null
                    )
                )
            }
            if (animeList.isNotEmpty()) {
                items.add(HomePageList("Últimos Animes Agregados", animeList, isHorizontal = true))
            }

            HomePageResponse(items)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun search(query: String): List<SearchResponse>? {
        // Todo: Implement search
        return emptyList()
    }

    override suspend fun load(url: String): com.spiderybook.domain.model.LoadResponse? {
        return try {
            val doc = Jsoup.connect(url).get()

            // Parse details
            val title = doc.select("h1.Title").text()
            val plot = doc.select("div.Description p").text()
            val rating = doc.select("span#votes_prmd").text().toDoubleOrNull()
            val year = null // Difficult to extract reliably without more parsing
            val posterPath = doc.select("div.Image img").attr("src")
            val posterUrl = if (posterPath.startsWith("http")) posterPath else "https://animeflv.net$posterPath"
            
            // Parse additional info
            val status = doc.select("p.AnmStts span").text()
            val type = doc.select("span.Type").first()?.text() ?: "Anime"
            
            val genres = doc.select("nav.Nvgnrs a").map { it.text() }

            // Parse episodes from script
            val scripts = doc.select("script")
            var episodeList = listOf<com.spiderybook.domain.model.Episode>()
            
            for (script in scripts) {
                val html = script.html()
                if (html.contains("var episodes =")) {
                    // Extract episodes array: [[num, id], [num, id], ...]
                    val pattern = java.util.regex.Pattern.compile("var episodes = (\\[\\[.*?\\]\\]);")
                    val matcher = pattern.matcher(html)
                    if (matcher.find()) {
                        val json = matcher.group(1)
                        // Manual parsing of simple JSON array of arrays
                        val cleanJson = json?.removePrefix("[")?.removeSuffix("]")
                        if (cleanJson != null) {
                            val items = cleanJson.split("],[")
                            episodeList = items.mapNotNull { item ->
                                val parts = item.replace("[", "").replace("]", "").split(",")
                                if (parts.size >= 2) {
                                    val number = parts[0].trim()
                                    // Construct episode URL: /ver/{anime-slug}-{number}
                                    // We need to extract the anime slug from the current URL
                                    // URL: https://www3.animeflv.net/anime/{slug}
                                    val msgUrl = url.replace("https://www3.animeflv.net", "")
                                        .replace("https://animeflv.net", "")
                                        .replace("/anime/", "")
                                    
                                    val episodeUrl = "/ver/$msgUrl-$number"
                                    
                                    com.spiderybook.domain.model.Episode(
                                        name = "Episodio $number",
                                        url = "$mainUrl$episodeUrl", // Absolute URL
                                        season = 1,
                                        episode = number.toIntOrNull() ?: 0
                                    )
                                } else null
                            }
                        }
                    }
                    break
                }
            }
            
            com.spiderybook.domain.model.LoadResponse(
                url = url,
                name = title,
                apiName = name,
                type = TvType.Anime,
                posterUrl = posterUrl,
                year = year,
                plot = plot,
                tags = genres,
                rating = rating,
                recommendations = emptyList(), // Can be implemented later
                episodes = episodeList
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun loadLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        // Todo: Implement loadLinks
        return false
    }
}
