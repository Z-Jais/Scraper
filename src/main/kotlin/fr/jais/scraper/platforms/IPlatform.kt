package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.Platform
import java.util.*

abstract class IPlatform(
    val scraper: Scraper,
    val name: String,
    val url: String,
    val image: String,
    val countries: List<Class<out ICountry>>
) {
    fun getPlatform() = Platform(name, url, image)

    open fun getEpisodes(calendar: Calendar, cachedEpisodes: List<String>): List<Episode> = emptyList()

    open fun reset() {
        // Can be implemented
    }

    override fun toString(): String {
        return "IPlatform(name='$name', url='$url', image='$image', countries=$countries)"
    }
}
