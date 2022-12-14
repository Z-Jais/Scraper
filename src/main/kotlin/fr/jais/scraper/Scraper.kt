package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.*
import java.util.*

class Scraper {
    val platforms = listOf(
        AnimationDigitalNetworkPlatform(this),
        CrunchyrollPlatform(this),
        NetflixPlatform(this),
//        WakanimPlatform(this)
    )
    val countries = platforms.flatMap { it.countries }.distinct().mapNotNull { it.getConstructor().newInstance() }

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    private fun getAllEpisodes(calendar: Calendar): List<Episode> {
        Logger.config("Calendar: ${calendar.toISO8601()}")

        Logger.info("Getting cached episodes...")
        val cachedEpisodes = Database.loadEpisodes().map { it.hash }

        Logger.info("Get all episodes...")
        val episodes = platforms
            .flatMap { it.getEpisodes(calendar, cachedEpisodes) }
            .filter { calendar.after(CalendarConverter.fromUTCDate(it.releaseDate)) }
            .sortedWith(
                compareBy(
                    { CalendarConverter.fromUTCDate(it.releaseDate) },
                    { it.anime.name.lowercase() },
                    { it.season },
                    { it.number }
                )
            )
        Logger.config("Episodes: ${episodes.size}")
        Database.saveEpisodes(episodes)
        API.saveEpisodes(episodes)
        return episodes
    }

    fun startThreadCheck() {
        ThreadManager.start("Checker") {
            var lastCheck: String? = null

            while (true) {
                val calendar = Calendar.getInstance()
                val today = calendar.toDate()

                if (lastCheck != today) {
                    Logger.info("Reset all platforms...")
                    lastCheck = today
                    platforms.forEach { it.reset() }
                }

                getAllEpisodes(calendar).forEach { println(it) }

                // Wait 5 minutes
                Thread.sleep(5 * 60 * 1000)
            }
        }
    }
}

fun main() {
    Logger.info("Initializing...")
    Const.gson
    Logger.info("Initialization done!")
    Logger.info("Starting...")
    val scraper = Scraper()
    Logger.info("Start main thread...")
    scraper.startThreadCheck()
}
