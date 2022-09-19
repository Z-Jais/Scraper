package fr.jais.scraper.platforms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.converters.CrunchyrollConverter
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.News
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.utils.CalendarConverter
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.toDate
import java.net.URL
import java.util.*
import java.util.logging.Level

class CrunchyrollPlatform(scraper: Scraper) : IPlatform(
    scraper,
    PlatformType.FLOWS,
    "Crunchyroll",
    "https://www.crunchyroll.com/",
    listOf(FranceCountry::class.java)
) {
    val converter = CrunchyrollConverter(this)

    fun xmlToJson(content: String) =
        Gson().fromJson(ObjectMapper().writeValueAsString(XmlMapper().readTree(content)), JsonObject::class.java)
            ?.getAsJsonObject("channel")?.getAsJsonArray("item")?.mapNotNull { it.asJsonObject }

    fun xmlToJsonWithEpisodeFilter(
        checkedCountry: ICountry,
        calendar: Calendar,
        content: String
    ): List<JsonObject>? {
        val restriction = when (checkedCountry) {
            is FranceCountry -> "fr"
            else -> throw CountryNotSupportedException("Country not supported")
        }

        return xmlToJson(content)
            ?.filter {
                val releaseDate = CalendarConverter.fromGMTLine(it.get("pubDate")?.asString)
                val countryRestrictions = it.getAsJsonObject("restriction")?.get("")?.asString?.split(" ")
                val subtitles = it.get("subtitleLanguages")?.asString?.split(",")

                releaseDate?.toDate() == calendar.toDate() &&
                        countryRestrictions?.any { r -> r == restriction } ?: false &&
                        subtitles?.any { s -> s == "fr - fr" } ?: false
            }
    }

    private fun xmlToJsonWithNewsFilter(
        calendar: Calendar,
        content: String
    ): List<JsonObject>? {
        return xmlToJson(content)
            ?.filter {
                val releaseDate = CalendarConverter.fromGMTLine(it.get("pubDate")?.asString)
                releaseDate?.toDate() == calendar.toDate()
            }
    }

    private fun getLang(checkedCountry: ICountry): String {
        val lang = when (checkedCountry) {
            is FranceCountry -> "frFR"
            else -> throw CountryNotSupportedException("Country not supported")
        }
        return lang
    }

    private fun getEpisodeAPIContent(checkedCountry: ICountry, calendar: Calendar): List<JsonObject>? {
        val lang = getLang(checkedCountry)

        return try {
            val apiUrl = "https://www.crunchyroll.com/rss/anime?lang=$lang"
            val content = URL(apiUrl).readText()
            xmlToJsonWithEpisodeFilter(checkedCountry, calendar, content)
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error while getting API content", e)
            null
        }
    }

    private fun getNewsAPIContent(checkedCountry: ICountry, calendar: Calendar): List<JsonObject>? {
        val lang = getLang(checkedCountry)

        return try {
            val apiUrl = "https://www.crunchyroll.com/newsrss?lang=$lang"
            val content = URL(apiUrl).readText()
            xmlToJsonWithNewsFilter(calendar, content)
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error while getting API content", e)
            null
        }
    }

    override fun getEpisodes(calendar: Calendar): List<Episode> {
        val countries = scraper.getCountries(this)
        return countries.flatMap { country ->
            Logger.info("Getting episodes for $name in ${country.name}...")
            getEpisodeAPIContent(country, calendar)?.mapNotNull {
                try {
                    converter.convertEpisode(country, it)
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "Error while converting episode", e)
                    null
                }
            } ?: emptyList()
        }
    }

    override fun getNews(calendar: Calendar): List<News> {
        val countries = scraper.getCountries(this)
        return countries.flatMap { country ->
            Logger.info("Getting news for $name in ${country.name}...")
            getNewsAPIContent(country, calendar)?.mapNotNull {
                try {
                    converter.convertNews(country, it)
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "Error while converting news", e)
                    null
                }
            } ?: emptyList()
        }
    }

    override fun reset() {
        converter.cache.clear()
    }
}
