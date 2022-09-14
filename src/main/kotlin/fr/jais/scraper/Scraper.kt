package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.toISO8601
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class Scraper {
    val platforms = listOf(AnimationDigitalNetworkPlatform(this), CrunchyrollPlatform(this))
    private val countries = mutableSetOf<ICountry>()

    fun getCountries(): List<ICountry> =
        platforms.flatMap { it.countries }.distinct().map { it.getConstructor().newInstance() }

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    init {
        Logger.info("Adding countries...")
        countries.addAll(getCountries())
    }

    fun getAllEpisodes(calendar: Calendar): List<Episode> {
        calendar.timeZone = TimeZone.getTimeZone("UTC")

        val list = mutableListOf<Episode>()
        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val callables = platforms.map { platform -> Callable { list.addAll(platform.getEpisodes(calendar)) } }
        Logger.info("Get all episodes...")
        Logger.config("Calendar: ${calendar.toISO8601()}")
        newFixedThreadPool.invokeAll(callables)
        newFixedThreadPool.shutdown()
        return list
    }
}

fun main() {
    val scraper = Scraper()
    scraper.getAllEpisodes(Calendar.getInstance()).forEach { println(it) }
}
