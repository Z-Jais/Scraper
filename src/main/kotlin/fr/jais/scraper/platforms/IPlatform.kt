package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.Platform
import java.util.*

abstract class IPlatform(
    val scraper: Scraper,
    val type: PlatformType,
    val name: String,
    val url: String,
    val countries: List<Class<out ICountry>>
) {
    enum class PlatformType {
        API,
        FLOWS,
    }

    fun getPlatform() = Platform(name, url)

    open fun getEpisodes(calendar: Calendar): List<Episode> = emptyList()
    open fun getNews(calendar: Calendar) {}
    open fun getMangas(calendar: Calendar) {}
    override fun toString(): String {
        return "IPlatform(name='$name', url='$url', countries=$countries)"
    }
}