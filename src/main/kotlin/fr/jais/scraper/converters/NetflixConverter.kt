package fr.jais.scraper.converters

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.CalendarConverter
import fr.jais.scraper.utils.toDate
import java.util.*

class NetflixConverter(private val platform: NetflixPlatform) {
    fun convertAnime(
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
        netflixEpisode: NetflixPlatform.NetflixEpisode
    ): Episode {
        val anime = convertAnime(checkedCountry, netflixContent, netflixEpisode.netflixAnime)

        return Episode(
            platform.getPlatform(),
            anime,
            CalendarConverter.toUTCDate("${calendar.toDate()}T${netflixContent.releaseTime}Z"),
            netflixContent.season,
            netflixEpisode.number,
            netflixContent.episodeType,
            netflixContent.langType,
            "${netflixContent.netflixId}${netflixContent.season}${netflixEpisode.number}".toLong(),
            netflixEpisode.title,
            "${platform.url}title/${netflixContent.netflixId}",
            netflixContent.image,
            netflixEpisode.duration
        )
    }
}
