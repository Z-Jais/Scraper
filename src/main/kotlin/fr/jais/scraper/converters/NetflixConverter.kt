package fr.jais.scraper.converters

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.NetflixPlatform
import java.text.SimpleDateFormat
import java.util.*

class NetflixConverter(private val platform: NetflixPlatform) {
    fun toISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

    fun fromISOTimestamp(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = simpleDateFormat.parse(iso8601string)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    fun convertAnime(
        checkedCountry: ICountry,
        netflixContent: NetflixPlatform.NetflixContent,
        netflixAnime: NetflixPlatform.NetflixAnime
    ): Anime {
        return Anime(
            checkedCountry,
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
            platform,
            anime,
            fromISOTimestamp("${toISODate(calendar)}T${netflixContent.releaseTime}Z")!!,
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