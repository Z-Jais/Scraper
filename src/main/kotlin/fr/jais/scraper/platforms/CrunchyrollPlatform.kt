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

    fun xmlToJsonWithFilter(
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

    fun getAPIContent(checkedCountry: ICountry, calendar: Calendar): List<JsonObject>? {
        val lang = when (checkedCountry) {
            is FranceCountry -> "frFR"
            else -> throw CountryNotSupportedException("Country not supported")
        }

        return try {
            val apiUrl = "https://www.crunchyroll.com/rss/anime?lang=$lang"
            val content = URL(apiUrl).readText()
            xmlToJsonWithFilter(checkedCountry, calendar, content)
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error while getting API content", e)
            null
        }
    }

    override fun getEpisodes(calendar: Calendar): List<Episode> {
        val countries = scraper.getCountries(this)
        return countries.flatMap { country ->
            Logger.info("Getting episodes for $name in ${country.name}...")
            getAPIContent(country, calendar)?.mapNotNull {
                try {
                    converter.convertEpisode(country, it)
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "Error while converting episode", e)
                    null
                }
            } ?: emptyList()
        }
    }

    override fun reset() {
        converter.cache.clear()
    }
}
