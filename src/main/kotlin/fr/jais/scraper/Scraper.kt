package fr.jais.scraper

import fr.jais.scraper.commands.ICommand
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.News
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.utils.*
import org.reflections.Reflections
import java.lang.Integer.min
import java.util.*
import java.util.logging.Level

class Scraper {
    private val mainPackage = "fr.jais.scraper."

    val platforms = Reflections("${mainPackage}platforms").getSubTypesOf(IPlatform::class.java)
        .mapNotNull { it.getConstructor(Scraper::class.java).newInstance(this) }
    val countries = platforms.flatMap { it.countries }.distinct().mapNotNull { it.getConstructor().newInstance() }
    private val commands = Reflections("${mainPackage}commands").getSubTypesOf(ICommand::class.java)
        .mapNotNull { it.getConstructor(Scraper::class.java).newInstance(this) }

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    fun getAllEpisodes(
        calendar: Calendar,
        platformType: IPlatform.PlatformType? = null
    ): List<Episode> {
        Logger.config("Calendar: ${calendar.toISO8601()}")

        Logger.info("Get all episodes...")
        val episodes = platforms
            .filter { platformType == null || it.type == platformType }
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

    fun getAllNews(calendar: Calendar): List<News> {
        Logger.config("Calendar: ${calendar.toISO8601()}")

        Logger.info("Get all news...")
        val news = platforms
            .flatMap { it.getNews(calendar) }
            .filter { calendar.after(CalendarConverter.fromUTCDate(it.releaseDate)) }
            .sortedWith(
                compareBy(
                    { CalendarConverter.fromUTCDate(it.releaseDate) }
                )
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

    fun startThreadConsole() {
        ThreadManager.start {
            val scanner = Scanner(System.`in`)

            while (true) {
                val line = scanner.nextLine()
                val allArgs = line.split(" ")

                val command = allArgs[0]
                val args = allArgs.subList(min(1, allArgs.size), allArgs.size)

                try {
                    commands.firstOrNull { it.command.lowercase() == command.lowercase() }?.execute(args)
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "An error occurred while executing the command.", e)
                }
            }
        }
    }
}

fun main() {
    val scraper = Scraper()
    scraper.startThreadCheck()
    scraper.startThreadConsole()
}
