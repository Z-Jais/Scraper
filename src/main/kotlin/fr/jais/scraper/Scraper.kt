package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.jobs.AyaneJob
import fr.jais.scraper.jobs.CheckJob
import fr.jais.scraper.jobs.ClearJob
import fr.jais.scraper.jobs.JobManager
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.Const
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.ThreadManager

class Scraper {
    val platforms = listOf(
        AnimationDigitalNetworkPlatform(this),
        CrunchyrollPlatform(this),
        NetflixPlatform(this)
    )
    val countries = platforms.flatMap { it.countries }.distinct().mapNotNull { it.getConstructor().newInstance() }
    private val jobManager = JobManager()

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    fun startThreadCommand() {
        ThreadManager.start("Command") {
            while (true) {
                val command = readlnOrNull() ?: continue
                val args = command.split(" ")

                when (args[0]) {
                    "ayane" -> {
                        AyaneJob().execute(null)
                    }

                    else -> {
                        Logger.info("Unknown command")
                    }
                }
            }
        }
    }

    fun startThreadCron() {
        jobManager.scheduleJob("0 0 9 * * ?", AyaneJob::class.java)
        jobManager.scheduleJob("0 0 0 * * ?", ClearJob::class.java)
        jobManager.scheduleJob("0 */2 * * * ?", CheckJob::class.java)
        jobManager.start()
    }

    companion object {
        val instance = Scraper()
    }
}

fun main() {
    Logger.info("Initializing...")
    Const.gson
    Logger.info("Initialization done!")

    Logger.info("Start command thread...")
    Scraper.instance.startThreadCommand()
    Logger.info("Start cron thread...")
    Scraper.instance.startThreadCron()
    Logger.info("Done!")
}
