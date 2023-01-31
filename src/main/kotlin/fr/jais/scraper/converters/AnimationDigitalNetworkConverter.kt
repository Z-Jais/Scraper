package fr.jais.scraper.converters

import com.google.gson.JsonObject
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.animes.*
import fr.jais.scraper.exceptions.episodes.*
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.utils.*
import java.util.Calendar

class AnimationDigitalNetworkConverter(private val platform: AnimationDigitalNetworkPlatform) {
    /// Convert anime from AnimationDigitalNetworkPlatform jsonObject to entity Anime
    private fun convertAnime(checkedCountry: ICountry, jsonObject: JsonObject): Anime {
        val showJson = jsonObject.getAsJsonObject("show") ?: throw AnimeNotFoundException("No show found")
//        Logger.config("Convert anime from $showJson")

        // ----- NAME -----
        Logger.info("Get name...")
        val name = (
                showJson.get("shortTitle")?.asString() ?: showJson.get("title")
                    ?.asString()
                )?.replace(Regex("Saison \\d"), "")?.trim()
            ?: throw AnimeNameNotFoundException("No name found")
        Logger.config("Name: $name")

        // ----- IMAGE -----
        Logger.info("Get image...")
        val image =
            showJson.get("image2x")?.asString()?.toHTTPS() ?: throw AnimeImageNotFoundException("No image found")
        Logger.config("Image: $image")

        // ----- DESCRIPTION -----
        Logger.info("Get description...")
        val description = showJson.get("summary")?.asString() ?: run {
            Logger.warning("No description found")
            null
        }
        Logger.config("Description: $description")

        // ----- GENRES -----
        Logger.info("Get genres...")
        val genres = showJson.getAsJsonArray("genres")?.mapNotNull { it.asString() } ?: emptyList()
        Logger.config("Genres: ${genres.joinToString(", ")}")

        if (!genres.any { it == "Animation japonaise" }) throw NotJapaneseAnimeException("Anime is not a Japanese anime")

        // ----- SIMULCAST -----
        Logger.info("Checking if anime is simulcasted...")
        val simulcasted = showJson.get("simulcast")?.asBoolean ?: false
        Logger.config("Simulcasted: $simulcasted")

        val descriptionLowercase = description?.lowercase()
        val isAlternativeSimulcast = descriptionLowercase?.startsWith("(Premier épisode ".lowercase()) == true || descriptionLowercase?.startsWith("(Diffusion des ".lowercase()) == true
        if (!simulcasted && !isAlternativeSimulcast) throw NotSimulcastAnimeException("Anime is not simulcasted")

        return Anime(checkedCountry.getCountry(), name, image, description, genres)
    }

    /// Convert episode from AnimationDigitalNetworkPlatform jsonObject to entity Episode
    fun convertEpisode(checkedCountry: ICountry, jsonObject: JsonObject, cachedEpisodes: List<String>): Episode {
//        Logger.config("Convert episode from $jsonObject")

        // ----- ANIME -----
        Logger.info("Convert anime...")
        val anime = convertAnime(checkedCountry, jsonObject)
        Logger.config("Anime: $anime")

        // ----- RELEASE DATE -----
        Logger.info("Get release date...")
        val releaseDate = CalendarConverter.fromUTCDate(jsonObject.get("releaseDate")?.asString())
            ?: throw EpisodeReleaseDateNotFoundException("No release date found")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

        // ----- SEASON -----
        Logger.info("Get season...")
        val season = jsonObject.get("season")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No season found, using 1")
            1
        }
        Logger.config("Season: $season")

        // ----- NUMBER -----
        Logger.info("Get number...")
        val number = jsonObject.get("shortNumber")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No number found, using -1...")
            -1
        }
        Logger.config("Number: $number")

        // ----- EPISODE TYPE -----
        Logger.info("Get episode type...")
        val episodeType = when (jsonObject.get("shortNumber")?.asString()) {
            "OAV" -> EpisodeType.SPECIAL
            "Film" -> EpisodeType.FILM
            else -> EpisodeType.EPISODE
        }
        Logger.config("Episode type: $episodeType")

        // ----- LANG TYPE -----
        Logger.info("Get lang type...")
        val langType = LangType.fromString(jsonObject.get("languages")?.asJsonArray()?.lastOrNull()?.asString() ?: "")
        Logger.config("Lang type: $langType")

        if (langType == LangType.UNKNOWN) throw EpisodeLangTypeNotFoundException("No lang type found")

        // ----- ID -----
        Logger.info("Get id...")
        val id = jsonObject.get("id")?.asLong() ?: throw EpisodeIdNotFoundException("No id found")
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
        val title = jsonObject.get("name")?.asString() ?: run {
            Logger.warning("No title found")
            null
        }
        Logger.config("Title: $title")

        // ----- URL -----
        Logger.info("Get url...")
        val url = jsonObject.get("url")?.asString()?.toHTTPS() ?: throw EpisodeUrlNotFoundException("No url found")
        Logger.config("Url: $url")

        // ----- IMAGE -----
        Logger.info("Get image...")
        val image =
            jsonObject.get("image2x")?.asString()?.toHTTPS() ?: throw EpisodeImageNotFoundException("No image found")
        Logger.config("Image: $image")

        // ----- DURATION -----
        Logger.info("Get duration...")
        val duration = jsonObject.get("duration")?.asLong() ?: run {
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
