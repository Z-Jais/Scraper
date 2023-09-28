package fr.jais.scraper.converters

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.exceptions.animes.AnimeImageNotFoundException
import fr.jais.scraper.exceptions.animes.AnimeNameNotFoundException
import fr.jais.scraper.exceptions.animes.AnimeNotFoundException
import fr.jais.scraper.exceptions.animes.NotSimulcastAnimeException
import fr.jais.scraper.exceptions.episodes.*
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.utils.*
import java.io.File
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class CrunchyrollConverter(private val platform: CrunchyrollPlatform) {
    data class CrunchyrollAnime(
        val iCountry: ICountry,
        val id: String,
        val name: String,
        val image: String,
        val description: String?
    )

    private val useApi = false
    val cache = mutableListOf<CrunchyrollAnime>()
    private val sessionId: String = crunchyrollSession()
    private val file = File("data/crunchyroll.json")

    fun getCountryTag(checkedCountry: ICountry): String {
        val country = when (checkedCountry) {
            is FranceCountry -> "fr"
            else -> throw CountryNotSupportedException("Country not supported")
        }
        return country
    }

    private fun crunchyrollSession(): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.crunchyroll.com/start_session.0.json?access_token=LNDJgOit5yaRIWN&device_type=com.crunchyroll.windows.desktop&device_id=${UUID.randomUUID()}"))
            .build()
        val response = Const.httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw Exception("Error while getting crunchyroll session ${response.statusCode()} : ${response.body()}")
        }

        return Const.gson.fromJson(response.body(), JsonObject::class.java)["data"].asJsonObject["session_id"].asString
    }

    private fun getSeriesId(mediaId: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.crunchyroll.com/info.0.json?session_id=$sessionId&media_id=$mediaId"))
            .build()
        val response = Const.httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw Exception("Error while getting crunchyroll session")
        }

        return Const.gson.fromJson(response.body(), JsonObject::class.java)["data"].asJsonObject["series_id"].asString
    }

    private fun getAnimeDetail(iCountry: ICountry, mediaId: String): Pair<String?, String?> {
        val seriesId = getSeriesId(mediaId)

        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "https://api.crunchyroll.com/info.0.json?session_id=$sessionId&series_id=$seriesId&locale=${
                        platform.getLang(
                            iCountry
                        )
                    }"
                )
            )
            .build()
        val response = Const.httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw Exception("Error while getting crunchyroll session")
        }

        val data = Const.gson.fromJson(response.body(), JsonObject::class.java)["data"].asJsonObject
        return data["portrait_image"].asJsonObject["full_url"].asString to data["description"].asString
    }

    private fun convertAnime(checkedCountry: ICountry, jsonObject: JsonObject): Anime {
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("[]")
        }

        val whitelistAnimes = Gson().fromJson(file.readText(), Array<String>::class.java).toList()

        // ----- NAME -----
        Logger.info("Get name...")
        val name = jsonObject["seriesTitle"]?.asString() ?: throw AnimeNameNotFoundException("No name found")
        Logger.config("Name: $name")

        if (!whitelistAnimes.contains(name) && (!isFilm(jsonObject) && platform.simulcasts[checkedCountry]?.contains(
                name.lowercase()
            ) != true)
        ) {
            Logger.info("Anime is not simulcasted")
            throw NotSimulcastAnimeException("Anime is not simulcasted")
        }

        val image: String?
        val description: String?

        if (cache.any { getInCache(it, checkedCountry, name) }) {
            val animeCached = cache.first { getInCache(it, checkedCountry, name) }

            // ----- IMAGE -----
            Logger.info("Get image from cache...")
            image = animeCached.image
            Logger.config("Image: $image")

            // ----- DESCRIPTION -----
            Logger.info("Get description from cache...")
            description = animeCached.description
            Logger.config("Description: $description")
        } else {
            val episodeUrl = jsonObject["link"]?.asString()
            Logger.config("Episode url: $episodeUrl")
            val split = episodeUrl?.split("/")
            val animeId = split?.get(split.size - 2) ?: throw AnimeNotFoundException("No anime id found in $episodeUrl")

            if (useApi) {
                val pair = getFromApi(jsonObject, checkedCountry)
                description = pair.first
                image = pair.second
            } else {
                val pair = parseWebsite(checkedCountry, animeId)
                description = pair.first
                image = pair.second
            }

            if (image.isNullOrBlank()) throw AnimeImageNotFoundException("No image found")

            cache.add(CrunchyrollAnime(checkedCountry, animeId, name, image, description))
        }

        // ----- GENRES -----
        Logger.info("Get genres...")
        val genres = jsonObject["keywords"]?.asString()?.split(", ") ?: emptyList()
        Logger.config("Genres: ${genres.joinToString(", ")}")

        return Anime(checkedCountry.getCountry(), name, image, description, genres)
    }

    private fun parseWebsite(
        checkedCountry: ICountry,
        animeId: String,
    ): Pair<String?, String?> {
        val country = when (checkedCountry) {
            is FranceCountry -> "fr"
            else -> throw CountryNotSupportedException("Country not supported")
        }

        // ----- ANIME PAGE -----
        Logger.info("Get anime page...")
        val url = "https://www.crunchyroll.com/$country/$animeId"
        Logger.config("Anime page: $url")
        val result = Browser(url).launchAndWaitForSelector("div.undefined:nth-child(1) > figure:nth-child(1) > picture:nth-child(1) > img:nth-child(2)")

        // ----- IMAGE -----
        Logger.info("Get image...")
        val image =
            result.selectXpath("//*[@id=\"content\"]/div/div[2]/div/div[1]/div[2]/div/div/div[2]/div[2]/figure/picture/img")
                .attr("src").toHTTPS()
        Logger.config("Image: $image")

        // ----- DESCRIPTION -----
        Logger.info("Get description...")
        val description =
            result.selectXpath("//*[@id=\"content\"]/div/div[2]/div/div[2]/div[1]/div[1]/div[5]/div/div/div/p").text()
        Logger.config("Description: $description")

        return Pair(description, image)
    }

    private fun getFromApi(
        jsonObject: JsonObject,
        checkedCountry: ICountry,
    ): Pair<String?, String?> {
        // ----- MEDIA ID -----
        Logger.info("Get media id...")
        val id = jsonObject["mediaId"]?.asString() ?: throw EpisodeIdNotFoundException("No media id found")
        Logger.config("Media id: $id")

        val animeDetail = getAnimeDetail(checkedCountry, id)
        return Pair(animeDetail.second, animeDetail.first ?: "")
    }

    private fun getInCache(
        crunchyrollAnime: CrunchyrollAnime,
        iCountry: ICountry,
        name: String
    ) = crunchyrollAnime.iCountry == iCountry && crunchyrollAnime.name.equals(name, true)

    private fun isDub(jsonObject: JsonObject) = LangType.VOICE.data.any {
        jsonObject["title"]!!.asString()?.contains(
            "($it)",
            true
        ) ?: false
    }

    private fun isFilm(jsonObject: JsonObject): Boolean {
        val title = jsonObject["title"]!!.asString()?.lowercase() ?: return false
        return title.contains("film") || title.contains("movie")
    }

    fun convertEpisode(
        checkedCountry: ICountry,
        jsonObject: JsonObject,
        cachedEpisodes: List<String>
    ): Episode {
        // ----- RESTRICTIONS -----
        Logger.info("Get restrictions...")
        val countryRestrictions =
            jsonObject.getAsJsonObject("restriction")?.get("")?.asString?.split(" ") ?: emptyList()
        val restrictionTag = getCountryTag(checkedCountry)
        Logger.config("Country restrictions: ${countryRestrictions.joinToString(", ")}")
        Logger.config("Restriction tag: $restrictionTag")

        if (countryRestrictions.isEmpty() || !countryRestrictions.contains(restrictionTag)) {
            throw EpisodeNotAvailableException("Episode not available in $checkedCountry")
        }

        // ----- SUBTITLES -----
        Logger.info("Get subtitles...")
        val subtitles = jsonObject["subtitleLanguages"]?.asString?.split(",") ?: emptyList()
        val countrySubtitles = when (checkedCountry) {
            is FranceCountry -> "fr - fr"
            else -> throw CountryNotSupportedException("Country not supported: $checkedCountry")
        }
        Logger.config("Subtitles: ${subtitles.joinToString(", ")}")
        Logger.config("Country subtitles: $countrySubtitles")

        if (!isDub(jsonObject) && (subtitles.isEmpty() || !subtitles.contains(countrySubtitles))) {
            throw EpisodeNotAvailableException("Subtitles not available in $checkedCountry")
        }

        // ----- ID -----
        Logger.info("Get id...")
        val id = jsonObject["mediaId"]?.asLong() ?: throw EpisodeIdNotFoundException("No id found")
        Logger.config("Id: $id")

        // ----- LANG TYPE -----
        Logger.info("Get lang type...")
        val langType = if (isDub(jsonObject)) LangType.VOICE else LangType.SUBTITLES
        Logger.config("Lang type: $langType")

        // ----- RELEASE DATE -----
        Logger.info("Get release date...")
        val releaseDate = CalendarConverter.fromGMTLine(jsonObject["pubDate"]?.asString())
            ?: throw EpisodeReleaseDateNotFoundException("No release date found")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

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

        // ----- ANIME -----
        Logger.info("Convert anime...")
        val anime = convertAnime(checkedCountry, jsonObject)
        Logger.config("Anime: $anime")

        // ----- SEASON -----
        Logger.info("Get season...")
        val season = jsonObject["season"]?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No season found, using 1")
            1
        }
        Logger.config("Season: $season")

        // ----- NUMBER -----
        Logger.info("Get number...")
        val number = jsonObject["episodeNumber"]?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No number found, using -1...")
            -1
        }
        Logger.config("Number: $number")

        // ----- EPISODE TYPE -----
        Logger.info("Get episode type...")
        val episodeType =
            if (isFilm(jsonObject)) EpisodeType.FILM else if (number == -1) EpisodeType.SPECIAL else EpisodeType.EPISODE
        Logger.config("Episode type: $episodeType")

        // ----- TITLE -----
        Logger.info("Get title...")
        val title = jsonObject["episodeTitle"]?.asString() ?: run {
            Logger.warning("No title found")
            null
        }
        Logger.config("Title: $title")

        // ----- URL -----
        Logger.info("Get url...")
        val url = jsonObject["link"]?.asString()?.toHTTPS() ?: throw EpisodeUrlNotFoundException("No url found")
        Logger.config("Url: $url")

        // ----- IMAGE -----
        Logger.info("Get image...")
        val thumbnails = jsonObject.getAsJsonArray("thumbnail")?.mapNotNull { it.asJsonObject() }
        val largeThumbnail = thumbnails?.maxByOrNull { it["width"].asLong }
        val image = largeThumbnail?.get("url")?.asString()?.toHTTPS()
            ?: "https://jais.ziedelth.fr/attachments/banner_640x360.png"
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
