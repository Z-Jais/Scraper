package fr.jais.scraper.converters

import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.episodes.EpisodeIdNotFoundException
import fr.jais.scraper.exceptions.episodes.EpisodeNotAvailableException
import fr.jais.scraper.exceptions.episodes.EpisodeNumberNotFoundException
import fr.jais.scraper.platforms.WakanimPlatform
import fr.jais.scraper.utils.Browser
import fr.jais.scraper.utils.CalendarConverter
import fr.jais.scraper.utils.Logger
import java.util.*
import kotlin.math.pow

private const val EPISODE_NOT_AVAILABLE_YET = "Episode not available yet"

class WakanimConverter(private val platform: WakanimPlatform) {
    val cache = mutableMapOf<WakanimPlatform.WakanimAgendaEpisode, Episode>()

    fun convertEpisode(calendar: Calendar, wakanimAgendaEpisode: WakanimPlatform.WakanimAgendaEpisode, cachedEpisodes: List<String>): Episode {
        if (cache.containsKey(wakanimAgendaEpisode)) {
            Logger.info("Get episode from cache")
            return cache[wakanimAgendaEpisode]!!
        }

        if (calendar.before(CalendarConverter.fromUTCDate(wakanimAgendaEpisode.releaseDate))) throw EpisodeNotAvailableException(
            EPISODE_NOT_AVAILABLE_YET
        )

        val type = wakanimAgendaEpisode.tmpUrl.split("/")[6]
        val content = Browser(Browser.BrowserType.FIREFOX, wakanimAgendaEpisode.tmpUrl).launch()

        val card = if (type.equals("episode", true)) {
            content.select(".currentEp").firstOrNull {
                it.select(".slider_item_number").text().toIntOrNull() == wakanimAgendaEpisode.number
            }
        } else {
            if (content.selectFirst("NoEpisodes") != null) throw EpisodeNotAvailableException(EPISODE_NOT_AVAILABLE_YET)

            content.select(".slider_item").firstOrNull {
                it.hasClass("-big") && it.select("slider_item_number").text()
                    .toIntOrNull() == wakanimAgendaEpisode.number
            }
        }

        // ----- NUMBER -----
        Logger.info("Get number...")
        val number = card?.select(".slider_item_number")?.text()?.toIntOrNull() ?: throw EpisodeNumberNotFoundException(
            "Episode number not found"
        )
        Logger.config("Number: $number")

        if (number != wakanimAgendaEpisode.number) throw EpisodeNotAvailableException(EPISODE_NOT_AVAILABLE_YET)

        // ----- URL -----
        Logger.info("Get url...")
        val url = "https://www.wakanim.tv${card.select(".slider_item_star").attr("href")}"
        Logger.config("Url: $url")

        // ----- ID -----
        Logger.info("Get id...")
        val id = url.split("/")[7].toLongOrNull() ?: throw EpisodeIdNotFoundException("Episode id not found")
        Logger.config("Id: $id")

        if (cachedEpisodes.contains(
                Episode.calculateHash(
                    platform.getPlatform(),
                    id,
                    wakanimAgendaEpisode.anime.country.tag,
                    wakanimAgendaEpisode.langType
                )
            )
        ) {
            throw EpisodeNotAvailableException("Episode already released")
        }

        // ----- IMAGE -----
        Logger.info("Get image...")
        val image = "https:${card.select("img").attr("src")}"
        Logger.config("Image: $image")

        val cardSeasonText = card.select(".slider_item_season").text()

        // ----- SEASON -----
        Logger.info("Get season...")
        val season = if (cardSeasonText.contains("Saison", true)) {
            val split = cardSeasonText.split(" ")
            split[split.indexOf("Saison") + 1].toIntOrNull() ?: 1
        } else {
            1
        }
        Logger.config("Season: $season")

        // ----- DURATION -----
        val cardDuration = card.select(".slider_item_duration").text().split(":")
        Logger.info("Get duration...")
        var duration = cardDuration.mapIndexed { i, t ->
            (t.ifEmpty { "0" }.toLongOrNull()?.times(60.0.pow(((cardDuration.size - i) - 1).toDouble())) ?: 0L).toLong()
        }.sum()
        if (duration <= 0L) duration = -1L
        Logger.config("Duration: $duration")

        val episode = Episode(
            platform.getPlatform(),
            wakanimAgendaEpisode.anime,
            wakanimAgendaEpisode.releaseDate,
            season,
            number,
            wakanimAgendaEpisode.episodeType,
            wakanimAgendaEpisode.langType,
            id,
            null,
            url,
            image,
            duration
        )

        cache[wakanimAgendaEpisode] = episode
        return episode
    }
}
