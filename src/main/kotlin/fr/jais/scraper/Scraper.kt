package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.Database
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.ThreadManager
import fr.jais.scraper.utils.toISO8601
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class Scraper {
    enum class CheckingType {
        SYNCHRONOUS,
        ASYNCHRONOUS,
    }

    val platforms = listOf(AnimationDigitalNetworkPlatform(this), CrunchyrollPlatform(this), NetflixPlatform(this))
    private val countries = mutableSetOf<ICountry>()

    fun getCountries(): List<ICountry> =
        platforms.flatMap { it.countries }.distinct().map { it.getConstructor().newInstance() }

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    init {
        Logger.info("Adding countries...")
        countries.addAll(getCountries())
    }

    fun getAllEpisodes(
        calendar: Calendar,
        checkingType: CheckingType = CheckingType.SYNCHRONOUS,
        platformType: IPlatform.PlatformType? = null
    ): List<Episode> {
        calendar.timeZone = TimeZone.getTimeZone("UTC")

        val list = mutableListOf<Episode>()

        Logger.info("Get all episodes...")
        Logger.config("Calendar: ${calendar.toISO8601()}")

        val filter = platforms.filter {
            if (platformType == null) true
            else it.type == platformType
        }

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

        val episodes = list.filter { calendar.after(it.releaseDate) }.sortedBy { it.releaseDate }

        val database = Database()
        val episodesInDatabase = database.load()
        episodesInDatabase.addAll(episodes.filter { !episodesInDatabase.any { epDb -> it.hash == epDb.hash } })
        database.save(episodesInDatabase)

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
            while (true) {
                val line = readLine()
                val split = line?.split(" ")
                val command = split?.getOrNull(0) ?: continue
                val args = split.subList(1, split.size)

                when (command.lowercase()) {
                    "exit" -> {
                        ThreadManager.stopAll()
                        exitProcess(0)
                    }

                    "check" -> {
                        if (args.isEmpty()) {
                            getAllEpisodes(Calendar.getInstance()).forEach { println(it) }
                            continue
                        }

                        val sdf = SimpleDateFormat("dd/MM/yyyy")

                        args.forEach { date ->
                            val calendar = Calendar.getInstance()
                            calendar.timeZone = TimeZone.getTimeZone("UTC")
                            calendar.time = sdf.parse(date)
                            calendar.set(Calendar.HOUR_OF_DAY, 23)
                            calendar.set(Calendar.MINUTE, 50)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)

                            Logger.info("Check for ${calendar.toISO8601()}")
                            Logger.info("------------------------------")
                            getAllEpisodes(calendar, platformType = IPlatform.PlatformType.API).forEach { println(it) }
                            Logger.info("------------------------------")
                        }
                    }
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
