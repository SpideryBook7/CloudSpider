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
        // Todo: Implement load
        return null
    }

    override suspend fun loadLinks(data: String, callback: (ExtractorLink) -> Unit): Boolean {
        // Todo: Implement loadLinks
        return false
    }
}
