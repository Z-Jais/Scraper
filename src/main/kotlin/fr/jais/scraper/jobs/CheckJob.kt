package fr.jais.scraper.jobs

import fr.jais.scraper.Scraper
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.utils.*
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.util.*

class CheckJob : Job {
    override fun execute(p0: JobExecutionContext?) {
        getAllEpisodes(Calendar.getInstance()).forEach { println(it) }
    }

    private fun getAllEpisodes(calendar: Calendar): List<Episode> {
        Logger.config("Calendar: ${calendar.toISO8601()}")

        Logger.info("Getting cached episodes...")
        val cachedEpisodes = Database.loadEpisodes().map { it.hash }

        Logger.info("Get all episodes...")
        val allEpisodes = Scraper.instance.platforms.flatMap { it.getEpisodes(calendar, cachedEpisodes) }
        val episodes = getEpisodesAfterDate(allEpisodes, calendar)

        Logger.config("Episodes: ${episodes.size}")
        Database.saveEpisodes(episodes)
        API.saveEpisodes(episodes)
        return episodes
    }

    fun getEpisodesAfterDate(
        allEpisodes: List<Episode>,
        calendar: Calendar
    ) = allEpisodes.filter {
        val convertedReleaseDate = CalendarConverter.fromUTCDate(it.releaseDate)
        calendar == convertedReleaseDate || calendar.toISO8601() == convertedReleaseDate?.toISO8601() || calendar.after(convertedReleaseDate)
    }
        .sortedWith(
            compareBy(
                { CalendarConverter.fromUTCDate(it.releaseDate) },
                { it.anime.name.lowercase() },
                { it.season },
                { it.number }
            )
        )
}
