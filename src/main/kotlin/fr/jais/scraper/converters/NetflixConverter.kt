package fr.jais.scraper.converters

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.episodes.EpisodeNotAvailableException
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.CalendarConverter
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.toDate
import java.util.*

class NetflixConverter(private val platform: NetflixPlatform) {
    private fun convertAnime(
        checkedCountry: ICountry,
        netflixContent: NetflixPlatform.NetflixContent,
        netflixAnime: NetflixPlatform.NetflixAnime
    ): Anime {
        return Anime(
            checkedCountry.getCountry(),
            netflixAnime.name,
            netflixContent.image,
            netflixAnime.description,
            netflixAnime.genres
        )
    }

    fun convertEpisode(
        checkedCountry: ICountry,
        calendar: Calendar,
        netflixContent: NetflixPlatform.NetflixContent,
        netflixEpisode: NetflixPlatform.NetflixEpisode,
        cachedEpisodes: List<String>,
    ): Episode {
        val id = "${netflixContent.netflixId}${netflixContent.season}${netflixEpisode.number}".toLong()
        val calculatedHash = Episode.calculateHash(platform.getPlatform(), id, checkedCountry.getCountry().tag, netflixContent.langType)

        if (cachedEpisodes.contains(calculatedHash)) {
            throw EpisodeNotAvailableException("Episode already released")
        }

        // ----- ANIME -----
        Logger.info("Convert anime...")
        val anime = convertAnime(checkedCountry, netflixContent, netflixEpisode.netflixAnime)
        Logger.config("Anime: $anime")

        return Episode(
            platform.getPlatform(),
            anime,
            CalendarConverter.toUTCDate("${calendar.toDate()}T${netflixContent.releaseTime}Z"),
            netflixContent.season,
            netflixEpisode.number,
            netflixContent.episodeType,
            netflixContent.langType,
            id,
            netflixEpisode.title,
            "${platform.url}title/${netflixContent.netflixId}",
            netflixEpisode.image,
            netflixEpisode.duration
        )
    }
}
