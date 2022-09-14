package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import java.util.*

abstract class IPlatform(
    val scraper: Scraper,
    val name: String,
    val url: String,
    image: String,
    val countries: List<Class<out ICountry>>
) {
    open fun getEpisodes(calendar: Calendar): List<Episode> = emptyList()
    open fun getNews(calendar: Calendar) {}
    open fun getMangas(calendar: Calendar) {}
}