package fr.jais.scraper.converters

import com.google.gson.JsonObject
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.News
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.exceptions.NewsException
import fr.jais.scraper.exceptions.animes.AnimeImageNotFoundException
import fr.jais.scraper.exceptions.animes.AnimeNameNotFoundException
import fr.jais.scraper.exceptions.animes.AnimeNotFoundException
import fr.jais.scraper.exceptions.animes.NotSimulcastAnimeException
import fr.jais.scraper.exceptions.episodes.*
import fr.jais.scraper.exceptions.news.NewsDescriptionNotFoundException
import fr.jais.scraper.exceptions.news.NewsReleaseDateNotFoundException
import fr.jais.scraper.exceptions.news.NewsTitleNotFoundException
import fr.jais.scraper.exceptions.news.NewsUrlNotFoundException
import fr.jais.scraper.platforms.CrunchyrollPlatform
import fr.jais.scraper.utils.*
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

        return Const.gson.fromJson(response.body(), JsonObject::class.java)
            .get("data").asJsonObject.get("session_id").asString
    }

    private fun getSeriesId(mediaId: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.crunchyroll.com/info.0.json?session_id=$sessionId&media_id=$mediaId"))
            .build()
        val response = Const.httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw Exception("Error while getting crunchyroll session")
        }

        return Const.gson.fromJson(response.body(), JsonObject::class.java)
            .get("data").asJsonObject.get("series_id").asString
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

        val data = Const.gson.fromJson(response.body(), JsonObject::class.java).get("data").asJsonObject
        return data["portrait_image"].asJsonObject["full_url"].asString to data["description"].asString
    }

    private fun convertAnime(checkedCountry: ICountry, jsonObject: JsonObject): Anime {
        Logger.config("Convert anime from $jsonObject")

        // ----- NAME -----
        Logger.info("Get name...")
        val name = jsonObject.get("seriesTitle")?.asString() ?: throw AnimeNameNotFoundException("No name found")
        Logger.config("Name: $name")

        if (platform.simulcasts[checkedCountry]?.contains(name.lowercase()) != true) {
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
            val episodeUrl = jsonObject.get("link")?.asString()
            val animeId =
                episodeUrl?.split("/")?.get(4) ?: throw AnimeNotFoundException("No anime id found in $episodeUrl")

            if (useApi) {
                val pair = getFromApi(jsonObject, checkedCountry)
                description = pair.first
                image = pair.second
            } else {
                val pair = parseWebsite(checkedCountry, animeId)
                description = pair.first
                image = pair.second
            }

            if (image == null) throw AnimeImageNotFoundException("No image found")

            cache.add(CrunchyrollAnime(checkedCountry, animeId, name, image, description))
        }

        // ----- GENRES -----
        Logger.info("Get genres...")
        val genres = jsonObject.get("keywords")?.asString()?.split(", ") ?: emptyList()
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
        val result = Browser(Browser.BrowserType.FIREFOX, url).launch()

        // ----- IMAGE -----
        Logger.info("Get image...")
        val image = result.selectXpath("//*[@id=\"content\"]/div/div[2]/div/div[1]/div[2]/div/div/div[2]/div[2]/figure/picture/img").attr("src").toHTTPS()
        Logger.config("Image: $image")

        // ----- DESCRIPTION -----
        Logger.info("Get description...")
        val description = result.selectXpath("//*[@id=\"content\"]/div/div[2]/div/div[2]/div[1]/div[1]/div[5]/div/div/div/p").text()
        Logger.config("Description: $description")

        return Pair(description, image)
    }

    private fun getFromApi(
        jsonObject: JsonObject,
        checkedCountry: ICountry,
    ): Pair<String?, String?> {
        // ----- MEDIA ID -----
        Logger.info("Get media id...")
        val id = jsonObject.get("mediaId")?.asString() ?: throw EpisodeIdNotFoundException("No media id found")
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
        jsonObject.get("title")!!.asString()?.contains(
            "($it)",
            true
        ) ?: false
    }

    fun convertEpisode(
        checkedCountry: ICountry,
        jsonObject: JsonObject,
        cachedEpisodes: List<String>
    ): Episode {
        Logger.config("Convert episode from $jsonObject")

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
        val subtitles = jsonObject.get("subtitleLanguages")?.asString?.split(",") ?: emptyList()
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
        val id = jsonObject.get("mediaId")?.asLong() ?: throw EpisodeIdNotFoundException("No id found")
        Logger.config("Id: $id")

        // ----- LANG TYPE -----
        Logger.info("Get lang type...")
        val langType = if (isDub(jsonObject)) LangType.VOICE else LangType.SUBTITLES
        Logger.config("Lang type: $langType")

        // ----- RELEASE DATE -----
        Logger.info("Get release date...")
        val releaseDate = CalendarConverter.fromGMTLine(jsonObject.get("pubDate")?.asString())
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
        val season = jsonObject.get("season")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No season found, using 1")
            1
        }
        Logger.config("Season: $season")

        // ----- NUMBER -----
        Logger.info("Get number...")
        val number = jsonObject.get("episodeNumber")?.asString()?.toIntOrNull() ?: run {
            Logger.warning("No number found, using -1...")
            -1
        }
        Logger.config("Number: $number")

        // ----- EPISODE TYPE -----
        Logger.info("Get episode type...")
        val episodeType = if (number == -1) EpisodeType.SPECIAL else EpisodeType.EPISODE
        Logger.config("Episode type: $episodeType")

        // ----- TITLE -----
        Logger.info("Get title...")
        val title = jsonObject.get("episodeTitle")?.asString() ?: run {
            Logger.warning("No title found")
            null
        }
        Logger.config("Title: $title")

        // ----- URL -----
        Logger.info("Get url...")
        val url = jsonObject.get("link")?.asString()?.toHTTPS() ?: throw EpisodeUrlNotFoundException("No url found")
        Logger.config("Url: $url")

        // ----- IMAGE -----
        Logger.info("Get image...")
        val thumbnails = jsonObject.getAsJsonArray("thumbnail")?.mapNotNull { it.asJsonObject() }
            ?: throw EpisodeImageNotFoundException("No thumbnail available")
        val largeThumbnail = thumbnails.maxByOrNull { it.get("width").asLong }
        val image =
            largeThumbnail?.get("url")?.asString()?.toHTTPS() ?: throw EpisodeImageNotFoundException("No image found")
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

    fun convertNews(checkedCountry: ICountry, jsonObject: JsonObject, cachedNews: List<String>): News {
        Logger.config("Convert news from $jsonObject")

        // ----- RELEASE DATE -----
        Logger.info("Get release date...")
        val releaseDate = CalendarConverter.fromGMTLine(jsonObject.get("pubDate")?.asString())
            ?: throw NewsReleaseDateNotFoundException("No release date found")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

        if (cachedNews.contains(News.calculateHash(platform.getPlatform(), releaseDate.toISO8601(), checkedCountry.getCountry()))) {
            throw NewsException("News already released")
        }

        // ----- TITLE -----
        Logger.info("Get title...")
        val title = jsonObject.get("title")?.asString() ?: throw NewsTitleNotFoundException("No title found")
        Logger.config("Title: $title")

        // ----- DESCRIPTION -----
        Logger.info("Get description...")
        val description =
            jsonObject.get("title")?.asString() ?: throw NewsDescriptionNotFoundException("No description found")
        Logger.config("description: $description")

        // ----- URL -----
        Logger.info("Get url...")
        val url = jsonObject.get("guid")?.asString()?.toHTTPS() ?: throw NewsUrlNotFoundException("No url found")
        Logger.config("Url: $url")

        return News(
            platform.getPlatform(),
            checkedCountry.getCountry(),
            releaseDate.toISO8601(),
            title,
            description,
            url
        )
    }
}
