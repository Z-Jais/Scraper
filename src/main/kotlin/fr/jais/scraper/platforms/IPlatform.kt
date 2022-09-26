package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.Manga
import fr.jais.scraper.entities.News
import fr.jais.scraper.entities.Platform
import java.util.*

abstract class IPlatform(
    val scraper: Scraper,
    val name: String,
    val url: String,
    val countries: List<Class<out ICountry>>
) {
    fun getPlatform() = Platform(name, url)

    open fun getEpisodes(calendar: Calendar): List<Episode> = emptyList()
    open fun getNews(calendar: Calendar): List<News> = emptyList()
    open fun getMangas(calendar: Calendar): List<Manga> = emptyList()

    open fun reset() {
        // Can be implemented
    }

    override fun toString(): String {
        return "IPlatform(name='$name', url='$url', countries=$countries)"
    }
}
