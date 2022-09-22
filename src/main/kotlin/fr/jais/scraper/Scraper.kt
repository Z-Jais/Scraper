package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.News
import fr.jais.scraper.platforms.*
import fr.jais.scraper.utils.*
import java.util.*

class Scraper {
    val platforms = listOf(AnimationDigitalNetworkPlatform(this), AnimeNewsNetworkPlatform(this), CrunchyrollPlatform(this), NetflixPlatform(this), WakanimPlatform(this))
    val countries = platforms.flatMap { it.countries }.distinct().mapNotNull { it.getConstructor().newInstance() }

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    private fun getAllEpisodes(calendar: Calendar): List<Episode> {
        Logger.config("Calendar: ${calendar.toISO8601()}")

        Logger.info("Get all episodes...")
        val episodes = platforms
            .flatMap { it.getEpisodes(calendar) }
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
        return episodes
    }

    private fun getAllNews(calendar: Calendar): List<News> {
        Logger.config("Calendar: ${calendar.toISO8601()}")

        Logger.info("Get all news...")
        val news = platforms
            .flatMap { it.getNews(calendar) }
            .filter { calendar.after(CalendarConverter.fromUTCDate(it.releaseDate)) }
            .sortedWith(
                compareBy { CalendarConverter.fromUTCDate(it.releaseDate) }
            )
        Logger.config("News: ${news.size}")
        Database.saveNews(news)
        return news
    }

    fun startThreadCheck() {
        ThreadManager.start {
            var lastCheck = Calendar.getInstance().toDate()

            while (true) {
                val calendar = Calendar.getInstance()
                val today = calendar.toDate()

                if (lastCheck != today) {
                    Logger.info("Reset all platforms...")
                    lastCheck = today
                    platforms.forEach { it.reset() }
                }

                getAllEpisodes(calendar).forEach { println(it) }
                getAllNews(calendar).forEach { println(it) }

                // Wait 5 minutes
                Thread.sleep(5 * 60 * 1000)
            }
        }
    }
}

fun main() {
    val scraper = Scraper()
    scraper.startThreadCheck()
}
