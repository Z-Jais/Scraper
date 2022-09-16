package fr.jais.scraper

import fr.jais.scraper.commands.CheckCommand
import fr.jais.scraper.commands.ExitCommand
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.*
import java.lang.Integer.min
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.logging.Level

private const val separator = "------------------------------"

class Scraper {
    enum class CheckingType {
        SYNCHRONOUS,
        ASYNCHRONOUS,
    }

    val platforms = listOf(AnimationDigitalNetworkPlatform(this), CrunchyrollPlatform(this), NetflixPlatform(this))
    private val countries = platforms.flatMap { it.countries }.distinct().map { it.getConstructor().newInstance() }
    private val commands = listOf(ExitCommand(this), CheckCommand(this))

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    fun getAllEpisodes(
        calendar: Calendar,
        checkingType: CheckingType = CheckingType.SYNCHRONOUS,
        platformType: IPlatform.PlatformType? = null
    ): List<Episode> {
        val list = mutableListOf<Episode>()

        Logger.info("Get all episodes...")
        Logger.config("Calendar: ${calendar.toISO8601()}")

        val filter = platforms.filter { platformType == null || it.type == platformType }

        when (checkingType) {
            CheckingType.ASYNCHRONOUS -> {
                val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
                val callables = filter.map { platform -> Callable { list.addAll(platform.getEpisodes(calendar)) } }
                newFixedThreadPool.invokeAll(callables)
                newFixedThreadPool.shutdown()
            }

            CheckingType.SYNCHRONOUS -> {
                filter.forEach { list.addAll(it.getEpisodes(calendar)) }
            }
        }

        Logger.info("Get all episodes done.")
        val episodes = list.filter { calendar.after(CalendarConverter.fromUTCDate(it.releaseDate)) }
            .sortedWith(
                compareBy(
                    { CalendarConverter.fromUTCDate(it.releaseDate) },
                    { it.anime.name.lowercase() },
                    { it.season },
                    { it.number })
            )
        Logger.config("Episodes: ${episodes.size}")
        Database.save(episodes)
        return episodes
    }

    fun startThreadCheck() {
        ThreadManager.start {
            while (true) {
                getAllEpisodes(Calendar.getInstance()).forEach { println(it) }

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
