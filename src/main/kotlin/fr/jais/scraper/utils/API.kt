package fr.jais.scraper.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Country
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.Platform
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private const val URL = "https://beta-api.ziedelth.fr/"

object API {
    private fun get(url: String): HttpResponse<String> {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun post(url: String, json: String): HttpResponse<String> {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun getCountry(country: Country): JsonObject? {
        val response = get("${URL}countries")
        val json = Gson().fromJson(response.body(), JsonArray::class.java) ?: return null
        return json.firstOrNull { it.asJsonObject["tag"].asString == country.tag }?.asJsonObject
    }

    private fun createCountry(country: Country): JsonObject? {
        val response = post("${URL}countries", Gson().toJson(country))
        return if (response.statusCode() == 201) Gson().fromJson(response.body(), JsonObject::class.java) else null
    }

    private fun getAnimeByHash(country: Country, anime: Anime): JsonObject? {
        val hash = anime.name.lowercase().filter { it.isLetterOrDigit() || it.isWhitespace() || it == '-' }.trim().replace("\\s+".toRegex(), "-").replace("--", "-")
        val response = get("${URL}animes/country/${country.tag}/search/hash/$hash")
        return if (response.statusCode() == 200) Gson().fromJson(response.body(), JsonObject::class.java) else null
    }

    private fun toAnime(country: JsonObject, anime: Anime, releaseDate: String): JsonObject {
        return JsonObject()
            .apply { add("country", JsonObject().apply { addProperty("uuid", country["uuid"].asString) }) }
            .apply { addProperty("name", anime.name) }
            .apply { addProperty("releaseDate", releaseDate) }
            .apply { addProperty("image", anime.image) }
            .apply { addProperty("description", anime.description) }
    }

    private fun createAnime(country: JsonObject, releaseDate: String, anime: Anime): JsonObject? {
        val response = post("${URL}animes", toAnime(country, anime, releaseDate).toString())
        return if (response.statusCode() == 201) Gson().fromJson(response.body(), JsonObject::class.java) else null
    }

    private fun getPlatform(platform: Platform): JsonObject? {
        val response = get("${URL}platforms")
        val json = Gson().fromJson(response.body(), JsonArray::class.java) ?: return null
        return json.firstOrNull { it.asJsonObject["name"].asString == platform.name }?.asJsonObject
    }

    private fun createPlatform(platform: Platform): JsonObject? {
        val response = post("${URL}platforms", Gson().toJson(platform))
        return if (response.statusCode() == 201) Gson().fromJson(response.body(), JsonObject::class.java) else null
    }

    private fun getEpisodeType(type: EpisodeType): JsonObject? {
        val response = get("${URL}episodetypes")
        val json = Gson().fromJson(response.body(), JsonArray::class.java) ?: return null
        return json.firstOrNull { it.asJsonObject["name"].asString == type.name }?.asJsonObject
    }

    private fun createEpisodeType(type: EpisodeType): JsonObject? {
        val response = post("${URL}episodetypes", JsonObject().apply { addProperty("name", type.name) }.toString())
        return if (response.statusCode() == 201) Gson().fromJson(response.body(), JsonObject::class.java) else null
    }

    private fun getLangType(type: LangType): JsonObject? {
        val response = get("${URL}langtypes")
        val json = Gson().fromJson(response.body(), JsonArray::class.java) ?: return null
        return json.firstOrNull { it.asJsonObject["name"].asString == type.name }?.asJsonObject
    }

    private fun createLangType(type: LangType): JsonObject? {
        val response = post("${URL}langtypes", JsonObject().apply { addProperty("name", type.name) }.toString())
        return if (response.statusCode() == 201) Gson().fromJson(response.body(), JsonObject::class.java) else null
    }

    private fun toEpisode(platform: JsonObject, anime: JsonObject, episodeType: JsonObject, langType: JsonObject, episode: Episode): JsonObject {
        return JsonObject()
            .apply { add("platform", JsonObject().apply { addProperty("uuid", platform["uuid"].asString) }) }
            .apply { add("anime", JsonObject().apply { addProperty("uuid", anime["uuid"].asString) }) }
            .apply { add("episodeType", JsonObject().apply { addProperty("uuid", episodeType["uuid"].asString) }) }
            .apply { add("langType", JsonObject().apply { addProperty("uuid", langType["uuid"].asString) }) }
            .apply { addProperty("hash", episode.hash) }
            .apply { addProperty("releaseDate", episode.releaseDate) }
            .apply { addProperty("season", episode.season) }
            .apply { addProperty("number", episode.number) }
            .apply { addProperty("title", episode.title) }
            .apply { addProperty("url", episode.url) }
            .apply { addProperty("image", episode.image) }
            .apply { addProperty("duration", episode.duration) }
    }

    fun saveEpisodes(episodes: List<Episode>) {
        val countriesApi = episodes.map { it.anime.country }.distinctBy { it.tag }.map { it to (getCountry(it) ?: createCountry(it)) }
        val platformsApi = episodes.map { it.platform }.distinctBy { it.name }.map { it to (getPlatform(it) ?: createPlatform(it)) }
        val episodeTypesApi = episodes.map { it.episodeType }.distinctBy { it.name }.map { it to (getEpisodeType(it) ?: createEpisodeType(it)) }
        val langTypesApi = episodes.map { it.langType }.distinctBy { it.name }.map { it to (getLangType(it) ?: createLangType(it)) }

        post("${URL}episodes/multiple", Gson().toJson(episodes.mapNotNull { episode ->
            val country = countriesApi.first { it.first == episode.anime.country }.second ?: return@mapNotNull null
            val anime = getAnimeByHash(episode.anime.country, episode.anime) ?: createAnime(country, episode.releaseDate, episode.anime) ?: return@mapNotNull null
            val platform = platformsApi.first { it.first == episode.platform }.second ?: return@mapNotNull null
            val episodeType = episodeTypesApi.first { it.first == episode.episodeType }.second ?: return@mapNotNull null
            val langType = langTypesApi.first { it.first == episode.langType }.second ?: return@mapNotNull null
            toEpisode(platform, anime, episodeType, langType, episode)
        }))
    }
}