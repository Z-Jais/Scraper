package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.*
import java.lang.Integer.min
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.system.exitProcess

private const val separator = "------------------------------"

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

    private fun getAllEpisodes(
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
            .sortedBy { CalendarConverter.fromUTCDate(it.releaseDate) }
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

    private fun setCheckedCalendar(calendar: Calendar): Calendar {
        return CalendarConverter.fromUTCDate("${SimpleDateFormat("yyyy-MM-dd").format(Date.from(calendar.toInstant()))}T23:50:00Z")!!
    }

    fun startThreadConsole() {
        ThreadManager.start {
            val scanner = Scanner(System.`in`)

            while (true) {
                val line = scanner.nextLine()
                val allArgs = line.split(" ")

                val command = allArgs[0]
                val args = allArgs.subList(min(1, allArgs.size), allArgs.size)

                when (command.lowercase()) {
                    "exit" -> {
                        ThreadManager.stopAll()
                        exitProcess(0)
                    }

                    "check" -> {
                        if (args.isEmpty()) continue

                        val list = mutableListOf<Episode>()

                        if (args.firstOrNull() == "--month") {
                            val calendar = Calendar.getInstance()
                            val dayInMonth = calendar.get(Calendar.DAY_OF_MONTH) - 1

                            for (i in dayInMonth downTo 1) {
                                var checkedCalendar = Calendar.getInstance()
                                checkedCalendar.add(Calendar.DAY_OF_MONTH, -i)
                                checkedCalendar = setCheckedCalendar(checkedCalendar)

                                Logger.info("Check for ${checkedCalendar.toISO8601()}")
                                Logger.info(separator)
                                list.addAll(getAllEpisodes(checkedCalendar, platformType = IPlatform.PlatformType.API))
                                Logger.info(separator)
                            }

                            list.sortedBy { it.releaseDate }.forEach { println(it) }

                            continue
                        }


                        val sdf = SimpleDateFormat("dd/MM/yyyy")

                        args.forEach { date ->
                            var calendar = Calendar.getInstance()
                            calendar.time = sdf.parse(date)
                            calendar = setCheckedCalendar(calendar)

                            Logger.info("Check for ${calendar.toISO8601()}")
                            Logger.info(separator)
                            list.addAll(getAllEpisodes(calendar, platformType = IPlatform.PlatformType.API))
                            Logger.info(separator)
                        }

                        list.sortedBy { it.releaseDate }.forEach { println(it) }
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
