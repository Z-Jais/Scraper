package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.converters.NetflixConverter
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.utils.Browser
import fr.jais.scraper.utils.EpisodeType
import fr.jais.scraper.utils.LangType
import fr.jais.scraper.utils.Logger
import org.jsoup.nodes.Document
import java.util.*
import java.util.logging.Level

class NetflixPlatform(scraper: Scraper) : IPlatform(
    scraper,
    "Netflix",
    "https://www.netflix.com/",
    listOf(FranceCountry::class.java)
) {
    data class NetflixAnime(
        val name: String,
        val description: String,
        val genres: List<String>
    )

    data class NetflixEpisode(
        val netflixAnime: NetflixAnime,
        val title: String?,
        val number: Int,
        val duration: Long,
        val image: String
    )

    data class NetflixContent(
        val netflixId: Int,
        val image: String,
        val releaseDay: Int,
        val season: Int = 1,
        val releaseTime: String = "07:01:00",
        val episodeType: EpisodeType = EpisodeType.EPISODE,
        val langType: LangType = LangType.SUBTITLES
    )

    val converter = NetflixConverter(this)
    val contents = mutableListOf(
        NetflixContent(
            81499847,
            "https://cdn.myanimelist.net/images/anime/1743/125204.jpg",
            Calendar.WEDNESDAY
        )
    )

    fun convertToNetflixEpisodes(content: Document): List<NetflixEpisode> {
        Logger.info("Converting to Netflix episodes")

        Logger.info("Getting anime name")
        val animeName = content.selectFirst(".title-title")?.text() ?: return emptyList()
        Logger.config("Anime name: $animeName")

        Logger.info("Getting anime description")
        val animeDescription = content.selectFirst(".title-info-synopsis")?.text() ?: return emptyList()
        Logger.config("Anime description: $animeDescription")

        Logger.info("Getting anime genres")
        val animeGenres =
            content.selectXpath("/html/body/div[1]/div/div[2]/section[1]/div[1]/div[1]/div[2]/div/div[1]/a").text()
        Logger.config("Anime genres: $animeGenres")

        val netflixAnime = NetflixAnime(animeName, animeDescription, animeGenres.split(" "))

        Logger.info("Getting episodes")
        val episodes = content.selectFirst("ol.episodes-container")?.select("li.episode") ?: emptyList()
        Logger.config("Episodes: ${episodes.size}")

        return episodes.mapNotNull { episode ->
            Logger.info("Getting episode title and number")
            val episodeTitleAndNumber = episode.selectFirst(".episode-title")?.text()
            Logger.config("Episode title and number: $episodeTitleAndNumber")

            Logger.info("Getting episode title")
            // Get the episode title in episodeTitleAndNumber
            // Example : "1. Let You Down" -> "Let You Down"
            val episodeTitle = episodeTitleAndNumber?.substringAfter(".")
            Logger.config("Episode title: $episodeTitle")

            Logger.info("Getting episode number")
            // Get the episode number in episodeTitleAndNumber
            // Example : "1. Let You Down" -> "1"
            val episodeNumber = episodeTitleAndNumber?.substringBefore(".")?.toIntOrNull() ?: -1
            Logger.config("Episode number: $episodeNumber")

            Logger.info("Getting episode duration")
            // Get the duration of the episode
            val duration = episode.selectFirst(".episode-runtime")?.text()
            Logger.config("Episode duration: $duration")

            Logger.info("Getting episode image")
            // Get the duration in seconds
            // Example : "10 min" -> 600
            val durationInSeconds = duration?.substringBefore(" ")?.trim()?.toLongOrNull()?.times(60) ?: -1
            Logger.config("Episode duration in seconds: $durationInSeconds")

            Logger.info("Getting episode image")
            // Get the image of the episode
            val image = episode.selectFirst(".episode-thumbnail-image")?.attr("src") ?: return@mapNotNull null
            Logger.config("Episode image: $image")
            // Remove all after ".jpg" in the image url
            // Example : "https://occ-0-1723-1722.1.nflxso.net/dnm/api/v6/.../image.jpg?r=abc" -> "https://occ-0-1723-1722.1.nflxso.net/dnm/api/v6/.../image.jpg"
            val imageWithoutParams = image.substringBefore("?")
            Logger.config("Episode image without params: $imageWithoutParams")

            NetflixEpisode(netflixAnime, episodeTitle, episodeNumber, durationInSeconds, imageWithoutParams)
        }
    }

    fun getAPIContent(checkedCountry: ICountry, netflixId: Int): List<NetflixEpisode>? {
        val lang = when (checkedCountry) {
            is FranceCountry -> "fr"
            else -> throw CountryNotSupportedException("Country not supported")
        }

        return try {
            val apiUrl = "https://www.netflix.com/$lang/title/$netflixId"
            val content = Browser(Browser.BrowserType.CHROME, apiUrl).launch()
            convertToNetflixEpisodes(content)
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error while getting API content", e)
            null
        }
    }

    override fun getEpisodes(calendar: Calendar): List<Episode> {
        val countries = scraper.getCountries(this)
        return countries.flatMap { country ->
            Logger.info("Getting episodes for $name in ${country.name}...")
            val filter = contents.filter { calendar.get(Calendar.DAY_OF_WEEK) == it.releaseDay }
            Logger.config("Release content today: ${filter.size}")

            if (filter.isEmpty()) emptyList<Episode>()

            filter.flatMapIndexed { _, content ->
                try {
                    val episodes = getAPIContent(country, content.netflixId) ?: return@flatMapIndexed emptyList()
                    episodes.mapNotNull {
                        try {
                            converter.convertEpisode(country, calendar, content, it)
                        } catch (e: Exception) {
                            Logger.log(Level.SEVERE, "Error while converting episode", e)
                            null
                        }
                    }
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "Error while converting episode", e)
                    emptyList()
                }
            }
        }
    }
}
