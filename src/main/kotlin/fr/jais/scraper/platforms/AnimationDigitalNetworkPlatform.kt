package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.animes.NoAnimeFoundException
import fr.jais.scraper.exceptions.animes.NoAnimeImageFoundException
import fr.jais.scraper.exceptions.animes.NoAnimeNameFoundException
import fr.jais.scraper.exceptions.episodes.*
import fr.jais.scraper.utils.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level

class AnimationDigitalNetworkPlatform(scraper: Scraper) :
    IPlatform<Nothing>(
        scraper,
        "Animation Digital Network",
        "https://animationdigitalnetwork.fr/",
        "",
        listOf(FranceCountry::class.java)
    ) {
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

    fun getAPIContent(checkedCountry: ICountry, calendar: Calendar): List<JsonObject>? {
        if (checkedCountry !is FranceCountry) return null

        return try {
            val apiUrl = "https://gw.api.animationdigitalnetwork.fr/video/calendar?date=${toISODate(calendar)}"
            val content = URL(apiUrl).readText()
            Gson().fromJson(content, JsonObject::class.java)?.getAsJsonArray("videos")?.mapNotNull { it.asJsonObject }
        } catch (e: Exception) {
            null
        }
    }

    fun convertAnime(checkedCountry: ICountry, jsonObject: JsonObject): Anime? {
        val showJson = jsonObject.getAsJsonObject("show") ?: throw NoAnimeFoundException("No show found")
        Logger.config("Convert anime from $showJson")

        Logger.info("Get name...")
        val name = showJson.get("shortTitle")?.asString ?: showJson.get("title")?.asString
        ?: throw NoAnimeNameFoundException("No name found")
        Logger.config("Name: $name")

        Logger.info("Get image...")
        val image = showJson.get("image2x")?.asString?.toHTTPS() ?: throw NoAnimeImageFoundException("No image found")
        Logger.config("Image: $image")

        Logger.info("Get description...")
        val description = showJson.get("summary")?.asString ?: run {
            Logger.warning("No description found")
            null
        }
        Logger.config("Description: $description")

        Logger.info("Get genres...")
        val genres = showJson.getAsJsonArray("genres")?.mapNotNull { it.asString } ?: emptyList()
        Logger.config("Genres: ${genres.joinToString(", ")}")

        if (!genres.any { it == "Animation japonaise" }) {
            Logger.warning("Not a Japanese anime, skipping...")
            return null
        }

        return Anime(checkedCountry, name, image, description, genres)
    }

    fun convertEpisode(checkedCountry: ICountry, jsonObject: JsonObject): Episode {
        Logger.config("Convert episode from $jsonObject")

        Logger.info("Convert anime...")
        val anime = convertAnime(checkedCountry, jsonObject) ?: throw NoAnimeFoundException("No anime found")
        Logger.config("Anime: $anime")

        Logger.info("Get release date...")
        val releaseDate = fromISOTimestamp(jsonObject.get("releaseDate")?.asString)
            ?: throw NoEpisodeReleaseDateFoundException("No release date found")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

        Logger.info("Get season...")
        val season = jsonObject.get("season")?.asString?.toIntOrNull() ?: run {
            Logger.warning("No season found, using 1")
            1
        }
        Logger.config("Season: $season")

        Logger.info("Get number...")
        val number = jsonObject.get("shortNumber")?.asString?.toIntOrNull() ?: run {
            Logger.warning("No number found, using -1...")
            -1
        }
        Logger.config("Number: $number")

        Logger.info("Get episode type...")
        val episodeType = when (jsonObject.get("shortNumber")?.asString) {
            "OAV" -> EpisodeType.SPECIAL
            "Film" -> EpisodeType.FILM
            else -> EpisodeType.EPISODE
        }
        Logger.config("Episode type: $episodeType")

        Logger.info("Get lang type...")
        val langType = LangType.fromString(jsonObject.get("languages")?.asJsonArray?.lastOrNull()?.asString ?: "")
        Logger.config("Lang type: $langType")

        if (langType == LangType.UNKNOWN) throw NoEpisodeLangTypeFoundException("No lang type found")

        Logger.info("Get id...")
        val id = jsonObject.get("id")?.asLong ?: throw NoEpisodeIdFoundException("No id found")
        Logger.config("Id: $id")

        Logger.info("Get title...")
        val title = jsonObject.get("name")?.asString ?: run {
            Logger.warning("No title found")
            null
        }
        Logger.config("Title: $title")

        Logger.info("Get url...")
        val url = jsonObject.get("url")?.asString?.toHTTPS() ?: throw NoEpisodeUrlFoundException("No url found")
        Logger.config("Url: $url")

        Logger.info("Get image...")
        val image =
            jsonObject.get("image2x")?.asString?.toHTTPS() ?: throw NoEpisodeImageFoundException("No image found")
        Logger.config("Image: $image")

        Logger.info("Get duration...")
        val duration = jsonObject.get("duration")?.asLong ?: run {
            Logger.warning("No duration found, using -1...")
            -1
        }
        Logger.config("Duration: $duration")

        return Episode(this, anime, releaseDate, season, number, episodeType, langType, id, title, url, image, duration)
    }

    override fun getEpisodes(calendar: Calendar): List<Episode> {
        val countries = scraper.getCountries(this)
        return countries.flatMap { country ->
            getAPIContent(country, calendar)?.mapNotNull {
                try {
                    convertEpisode(country, it)
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "Error while converting episode", e)
                    null
                }
            } ?: emptyList()
        }
    }
}