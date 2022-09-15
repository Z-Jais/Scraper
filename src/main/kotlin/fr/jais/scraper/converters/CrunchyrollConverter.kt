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
import fr.jais.scraper.utils.*
import java.util.*

class CrunchyrollConverter(private val platform: CrunchyrollPlatform) {
    data class Cache(val id: String, val image: String, val description: String?)

    private val cache = HashMap<String, Cache>()

    fun convertAnime(checkedCountry: ICountry, jsonObject: JsonObject): Anime {
        Logger.config("Convert anime from $jsonObject")

        Logger.info("Get name...")
        val name = jsonObject.get("seriesTitle")?.asString() ?: throw NoAnimeNameFoundException("No name found")
        Logger.config("Name: $name")

        var image: String?
        var description: String?

        if (cache.containsKey(name)) {
            val animeCached = cache[name]

            Logger.info("Get image...")
            image = animeCached?.image
            Logger.config("Image: $image")

            Logger.info("Get description...")
            description = animeCached?.description
            Logger.config("Description: $description")
        } else {
            val country = when (checkedCountry) {
                is FranceCountry -> "fr"
                else -> throw CountryNotSupportedException("Country not supported")
            }

            val episodeUrl = jsonObject.get("link")?.asString()
            val animeId =
                episodeUrl?.split("/")?.get(4) ?: throw NoAnimeFoundException("No anime id found in $episodeUrl")
            val browser = Browser(Browser.BrowserType.FIREFOX, "https://www.crunchyroll.com/$country/$animeId")
            val result = browser.launch()

            image = result.selectXpath("//*[@id=\"sidebar_elements\"]/li[1]/img").attr("src").toHTTPS()

            val divContent =
                result.selectXpath("/html/body/div[@id='template_scroller']/div/div[@id='template_body']/div[3]/div")
                    .text()

            // Adult content
            if (divContent.startsWith("This content may be inappropriate for some people.")) {
                Logger.warning("Adult content detected, skipping...")
                image = ""
                description = null
            } else {
                description = result.getElementsByClass("more").first()?.text()
                if (description.isNullOrBlank()) description = result.getElementsByClass("trunc-desc").text()
            }

            cache[name] = Cache(animeId, image, description)
        }

        if (image == null) throw NoAnimeImageFoundException("No image found")

        Logger.info("Get genres...")
        val genres = jsonObject.get("keywords")?.asString()?.split(", ") ?: emptyList()
        Logger.config("Genres: ${genres.joinToString(", ")}")

        return Anime(checkedCountry, name, image, description, genres)
    }

    private fun isDub(jsonObject: JsonObject) = LangType.VOICE.data.any {
        jsonObject.get("title")!!.asString()?.contains(
            "($it)",
            true
        ) ?: false
    }

    fun convertEpisode(checkedCountry: ICountry, jsonObject: JsonObject): Episode {
        Logger.config("Convert episode from $jsonObject")

        Logger.info("Convert anime...")
        val anime = convertAnime(checkedCountry, jsonObject)
        Logger.config("Anime: $anime")

        Logger.info("Get release date...")
        val releaseDate = platform.fromTimestamp(jsonObject.get("pubDate")?.asString())
            ?: throw NoEpisodeReleaseDateFoundException("No release date found")
        releaseDate.timeZone = TimeZone.getTimeZone("UTC")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

        Logger.info("Get season...")
        val season = jsonObject.get("season")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No season found, using 1")
            1
        }
        Logger.config("Season: $season")

        Logger.info("Get number...")
        val number = jsonObject.get("episodeNumber")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No number found, using -1...")
            -1
        }
        Logger.config("Number: $number")

        Logger.info("Get episode type...")
        val episodeType = if (number == -1) EpisodeType.SPECIAL else EpisodeType.EPISODE
        Logger.config("Episode type: $episodeType")

        Logger.info("Get lang type...")
        val langType = if (isDub(jsonObject)) LangType.VOICE else LangType.SUBTITLES
        Logger.config("Lang type: $langType")

        Logger.info("Get id...")
        val id = jsonObject.get("mediaId")?.asLong() ?: throw NoEpisodeIdFoundException("No id found")
        Logger.config("Id: $id")

        Logger.info("Get title...")
        val title = jsonObject.get("episodeTitle")?.asString() ?: run {
            Logger.warning("No title found")
            null
        }
        Logger.config("Title: $title")

        Logger.info("Get url...")
        val url = jsonObject.get("link")?.asString()?.toHTTPS() ?: throw NoEpisodeUrlFoundException("No url found")
        Logger.config("Url: $url")

        Logger.info("Get image...")
        val thumbnails = jsonObject.getAsJsonArray("thumbnail")?.mapNotNull { it.asJsonObject() }
            ?: throw NoEpisodeImageFoundException("No thumbnail available")
        val largeThumbnail = thumbnails.maxByOrNull { it.get("width").asLong }
        val image =
            largeThumbnail?.get("url")?.asString()?.toHTTPS() ?: throw NoEpisodeImageFoundException("No image found")
        Logger.config("Image: $image")

        Logger.info("Get duration...")
        val duration = jsonObject.get("duration")?.asLong() ?: run {
            Logger.warning("No duration found, using -1...")
            -1
        }
        Logger.config("Duration: $duration")

        return Episode(
            platform,
            anime,
            releaseDate,
            season,
            number,
            episodeType,
            langType,
            id,
            title,
            url,
            image,
            duration
        )
    }
}