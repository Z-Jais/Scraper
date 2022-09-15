package fr.jais.scraper.converters

import com.google.gson.JsonObject
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.exceptions.animes.NoAnimeFoundException
import fr.jais.scraper.exceptions.animes.NoAnimeImageFoundException
import fr.jais.scraper.exceptions.animes.NoAnimeNameFoundException
import fr.jais.scraper.exceptions.episodes.NoEpisodeIdFoundException
import fr.jais.scraper.exceptions.episodes.NoEpisodeImageFoundException
import fr.jais.scraper.exceptions.episodes.NoEpisodeReleaseDateFoundException
import fr.jais.scraper.exceptions.episodes.NoEpisodeUrlFoundException
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.platforms.NetflixPlatform
import fr.jais.scraper.utils.*
import java.util.Calendar

class NetflixConverter(private val platform: NetflixPlatform) {
    fun convertAnime(checkedCountry: ICountry, netflixContent: NetflixPlatform.NetflixContent, netflixAnime: NetflixPlatform.NetflixAnime): Anime {
        return Anime(checkedCountry, netflixAnime.name, netflixContent.image, netflixAnime.description, netflixAnime.genres)
    }

    fun convertEpisode(checkedCountry: ICountry, calendar: Calendar, netflixContent: NetflixPlatform.NetflixContent, netflixEpisode: NetflixPlatform.NetflixEpisode): Episode {
        val anime = convertAnime(checkedCountry, netflixContent, netflixEpisode.netflixAnime)

        return Episode(
            platform,
            anime,
            calendar,
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