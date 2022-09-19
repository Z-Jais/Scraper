package fr.jais.scraper.converters

import com.google.gson.JsonObject
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.animes.*
import fr.jais.scraper.exceptions.episodes.*
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.utils.*

class AnimationDigitalNetworkConverter(private val platform: AnimationDigitalNetworkPlatform) {
    private val animeNameSeasonRegex = Regex(".* - Saison \\d")

    fun convertAnime(checkedCountry: ICountry, jsonObject: JsonObject): Anime {
        val showJson = jsonObject.getAsJsonObject("show") ?: throw AnimeNotFoundException("No show found")
        Logger.config("Convert anime from $showJson")

        Logger.info("Get name...")
        var name = showJson.get("shortTitle")?.asString() ?: showJson.get("title")?.asString()
            ?: throw AnimeNameNotFoundException("No name found")
        Logger.config("Name: $name")

        if (name.matches(animeNameSeasonRegex)) {
            Logger.warning("Anime name contains season number, removing it...")
            // Remove the match part of the name
            name = name.replace(Regex(" - Saison \\d"), "")
        }

        Logger.info("Get image...")
        val image = showJson.get("image2x")?.asString()?.toHTTPS() ?: throw AnimeImageNotFoundException("No image found")
        Logger.config("Image: $image")

        Logger.info("Get description...")
        val description = showJson.get("summary")?.asString() ?: run {
            Logger.warning("No description found")
            null
        }
        Logger.config("Description: $description")

        Logger.info("Get genres...")
        val genres = showJson.getAsJsonArray("genres")?.mapNotNull { it.asString() } ?: emptyList()
        Logger.config("Genres: ${genres.joinToString(", ")}")

        if (!genres.any { it == "Animation japonaise" }) throw NotJapaneseAnimeException("Anime is not a Japanese anime")

        Logger.info("Checking if anime is simulcasted...")
        val simulcasted = showJson.get("simulcast")?.asBoolean ?: false
        Logger.config("Simulcasted: $simulcasted")

        if (!simulcasted) throw NotSimulcastAnimeException("Anime is not simulcasted")

        return Anime(checkedCountry.getCountry(), name, image, description, genres)
    }

    fun convertEpisode(checkedCountry: ICountry, jsonObject: JsonObject): Episode {
        Logger.config("Convert episode from $jsonObject")

        Logger.info("Convert anime...")
        val anime = convertAnime(checkedCountry, jsonObject)
        Logger.config("Anime: $anime")

        Logger.info("Get release date...")
        val releaseDate = CalendarConverter.fromUTCDate(jsonObject.get("releaseDate")?.asString())
            ?: throw EpisodeReleaseDateNotFoundException("No release date found")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

        Logger.info("Get season...")
        val season = jsonObject.get("season")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No season found, using 1")
            1
        }
        Logger.config("Season: $season")

        Logger.info("Get number...")
        val number = jsonObject.get("shortNumber")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No number found, using -1...")
            -1
        }
        Logger.config("Number: $number")

        Logger.info("Get episode type...")
        val episodeType = when (jsonObject.get("shortNumber")?.asString()) {
            "OAV" -> EpisodeType.SPECIAL
            "Film" -> EpisodeType.FILM
            else -> EpisodeType.EPISODE
        }
        Logger.config("Episode type: $episodeType")

        Logger.info("Get lang type...")
        val langType = LangType.fromString(jsonObject.get("languages")?.asJsonArray()?.lastOrNull()?.asString() ?: "")
        Logger.config("Lang type: $langType")

        if (langType == LangType.UNKNOWN) throw EpisodeLangTypeNotFoundException("No lang type found")

        Logger.info("Get id...")
        val id = jsonObject.get("id")?.asLong() ?: throw EpisodeIdNotFoundException("No id found")
        Logger.config("Id: $id")

        Logger.info("Get title...")
        val title = jsonObject.get("name")?.asString() ?: run {
            Logger.warning("No title found")
            null
        }
        Logger.config("Title: $title")

        Logger.info("Get url...")
        val url = jsonObject.get("url")?.asString()?.toHTTPS() ?: throw EpisodeUrlNotFoundException("No url found")
        Logger.config("Url: $url")

        Logger.info("Get image...")
        val image =
            jsonObject.get("image2x")?.asString()?.toHTTPS() ?: throw EpisodeImageNotFoundException("No image found")
        Logger.config("Image: $image")

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
