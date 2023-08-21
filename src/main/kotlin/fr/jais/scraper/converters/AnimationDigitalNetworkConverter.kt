package fr.jais.scraper.converters

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.animes.*
import fr.jais.scraper.exceptions.episodes.*
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.utils.*
import java.io.File
import java.util.*

class AnimationDigitalNetworkConverter(private val platform: AnimationDigitalNetworkPlatform) {
    private val file = File("data/animation_digital_network.json")

    /// Convert anime from AnimationDigitalNetworkPlatform jsonObject to entity Anime
    private fun convertAnime(checkedCountry: ICountry, calendar: Calendar, jsonObject: JsonObject): Anime {
        val showJson = jsonObject.getAsJsonObject("show") ?: throw AnimeNotFoundException("No show found")

        if (!file.exists()) {
            file.createNewFile()
            file.writeText("[]")
        }

        val whitelistAnimes = Gson().fromJson(file.readText(), Array<String>::class.java).toList()

        // ----- NAME -----
        Logger.info("Get name...")
        var name =
            (showJson["shortTitle"]?.asString() ?: showJson["title"]?.asString())?.replace(Regex("Saison \\d"), "")
                ?.trim() ?: throw AnimeNameNotFoundException(
                "No name found"
            )
        // Remove " -" at the end of the name
        if (name.endsWith(" -")) name = name.substring(0, name.length - 2)
        name = name.trim()
        Logger.config("Name: $name")

        // ----- IMAGE -----
        Logger.info("Get image...")
        val image = showJson["image2x"]?.asString()?.toHTTPS() ?: throw AnimeImageNotFoundException("No image found")
        Logger.config("Image: $image")

        // ----- DESCRIPTION -----
        Logger.info("Get description...")
        val description = showJson["summary"]?.asString() ?: run {
            Logger.warning("No description found")
            null
        }
        Logger.config("Description: $description")

        // ----- GENRES -----
        Logger.info("Get genres...")
        val genres = showJson.getAsJsonArray("genres")?.mapNotNull { it.asString() } ?: emptyList()
        Logger.config("Genres: ${genres.joinToString(", ")}")

        if (!genres.any { it.startsWith("Animation ", true) }) throw NotJapaneseAnimeException("Show is not an anime")

        // ----- SIMULCAST -----
        Logger.info("Checking if anime is simulcasted...")
        val simulcasted =
            showJson["simulcast"]?.asBoolean == true || showJson["firstReleaseYear"]?.asString == calendar.getYear()
        Logger.config("Simulcasted: $simulcasted")

        val descriptionLowercase = description?.lowercase()
        val isAlternativeSimulcast =
            whitelistAnimes.contains(name) || (descriptionLowercase?.startsWith("(Premier épisode ".lowercase()) == true ||
                    descriptionLowercase?.startsWith("(Diffusion des ".lowercase()) == true ||
                    descriptionLowercase?.startsWith("(Diffusion du premier épisode".lowercase()) == true ||
                    descriptionLowercase?.startsWith("(Diffusion de l'épisode 1 le".lowercase()) == true)

        if (!simulcasted && !isAlternativeSimulcast) throw NotSimulcastAnimeException("Anime is not simulcasted")

        return Anime(checkedCountry.getCountry(), name, image, description, genres)
    }

    /// Convert episode from AnimationDigitalNetworkPlatform jsonObject to entity Episode
    fun convertEpisode(
        checkedCountry: ICountry,
        calendar: Calendar,
        jsonObject: JsonObject,
        cachedEpisodes: List<String>
    ): Episode {
        // ----- ANIME -----
        Logger.info("Convert anime...")
        val anime = convertAnime(checkedCountry, calendar, jsonObject)
        Logger.config("Anime: $anime")

        // ----- RELEASE DATE -----
        Logger.info("Get release date...")
        val releaseDate =
            CalendarConverter.fromUTCDate(jsonObject["releaseDate"]?.asString())
                ?: throw EpisodeReleaseDateNotFoundException("No release date found")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

        // ----- SEASON -----
        Logger.info("Get season...")
        val season = jsonObject["season"]?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No season found, using 1")
            1
        }
        Logger.config("Season: $season")

        // ----- NUMBER -----
        Logger.info("Get number...")

        if ("Bande-annonce" == jsonObject["shortNumber"]?.asString) {
            throw EpisodeNotAvailableException("Trailer detected")
        }

        val number = jsonObject["shortNumber"]?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No number found, using -1...")
            -1
        }
        Logger.config("Number: $number")

        // ----- EPISODE TYPE -----
        Logger.info("Get episode type...")
        val episodeType = when (jsonObject["shortNumber"]?.asString()) {
            "OAV" -> EpisodeType.SPECIAL
            "Film" -> EpisodeType.FILM
            else -> EpisodeType.EPISODE
        }
        Logger.config("Episode type: $episodeType")

        // ----- LANG TYPE -----
        Logger.info("Get lang type...")
        val langType = LangType.fromString(jsonObject["languages"]?.asJsonArray()?.lastOrNull()?.asString() ?: "")
        Logger.config("Lang type: $langType")

        if (langType == LangType.UNKNOWN) throw EpisodeLangTypeNotFoundException("No lang type found")

        // ----- ID -----
        Logger.info("Get id...")
        val id = jsonObject["id"]?.asLong() ?: throw EpisodeIdNotFoundException("No id found")
        Logger.config("Id: $id")

        if (cachedEpisodes.contains(
                Episode.calculateHash(
                    platform.getPlatform(),
                    id,
                    checkedCountry.getCountry().tag,
                    langType
                )
            )
        ) {
            throw EpisodeNotAvailableException("Episode already released")
        }

        // ----- TITLE -----
        Logger.info("Get title...")
        val title = jsonObject["name"]?.asString() ?: run {
            Logger.warning("No title found")
            null
        }
        Logger.config("Title: $title")

        // ----- URL -----
        Logger.info("Get url...")
        val url = jsonObject["url"]?.asString()?.toHTTPS() ?: throw EpisodeUrlNotFoundException("No url found")
        Logger.config("Url: $url")

        // ----- IMAGE -----
        Logger.info("Get image...")
        val image =
            jsonObject["image2x"]?.asString()?.toHTTPS() ?: throw EpisodeImageNotFoundException("No image found")
        Logger.config("Image: $image")

        // ----- DURATION -----
        Logger.info("Get duration...")
        val duration = jsonObject["duration"]?.asLong() ?: run {
            Logger.warning("No duration found, using -1...")
            -1
        }
        Logger.config("Duration: $duration")

        return Episode(
            platform.getPlatform(),
            anime,
            releaseDate.toISO8601(),
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
