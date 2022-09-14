package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.utils.toHTTPS
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class AnimationDigitalNetworkPlatform(scraper: Scraper) :
    IPlatform(
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

    fun getAPIContent(calendar: Calendar): List<JsonObject>? {
        return try {
            val apiUrl = "https://gw.api.animationdigitalnetwork.fr/video/calendar?date=${toISODate(calendar)}"
            val content = URL(apiUrl).readText()
            Gson().fromJson(content, JsonObject::class.java)?.getAsJsonArray("videos")?.mapNotNull { it.asJsonObject }
        } catch (e: Exception) {
            null
        }
    }

    fun convertAnime(checkedCountry: ICountry, jsonObject: JsonObject): Anime? {
        val showJson = jsonObject.getAsJsonObject("show") ?: return null
        val name = showJson.get("shortTitle")?.asString ?: showJson.get("title")?.asString ?: return null
        val image = showJson.get("image2x")?.asString?.toHTTPS() ?: return null
        val description = showJson.get("summary")?.asString
        val genres = showJson.getAsJsonArray("genres")?.mapNotNull { it.asString } ?: emptyList()
        if (!genres.any { it == "Animation japonaise" }) return null
        return Anime(checkedCountry, name, image, description, genres)
    }

    fun convertEpisode(checkedCountry: ICountry, jsonObject: JsonObject): Episode? {
        val anime = convertAnime(checkedCountry, jsonObject) ?: return null
        val releaseDate = fromISOTimestamp(jsonObject.get("releaseDate")?.asString) ?: return null
        val season = jsonObject.get("season")?.asString?.toIntOrNull() ?: 1
        val number = jsonObject.get("shortNumber")?.asString?.toIntOrNull() ?: -1
        val title = jsonObject.get("name")?.asString
        val url = jsonObject.get("url")?.asString?.toHTTPS() ?: return null
        val image = jsonObject.get("image2x")?.asString?.toHTTPS() ?: return null
        val duration = jsonObject.get("duration")?.asLong ?: -1
        return Episode(this, anime, releaseDate, season, number, title, url, image, duration)
    }

    override fun getEpisodes(calendar: Calendar): List<Episode> {
        val country = scraper.getCountries(this).firstOrNull() ?: return emptyList()
        return getAPIContent(calendar)?.mapNotNull { convertEpisode(country, it) } ?: emptyList()
    }
}