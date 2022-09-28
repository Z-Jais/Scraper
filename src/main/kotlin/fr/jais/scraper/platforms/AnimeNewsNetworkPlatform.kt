package fr.jais.scraper.platforms

import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.converters.AnimeNewsNetworkConverter
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.News
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.utils.CalendarConverter
import fr.jais.scraper.utils.Const
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.toDate
import java.net.URL
import java.util.*
import java.util.logging.Level

class AnimeNewsNetworkPlatform(scraper: Scraper) : IPlatform(
    scraper,
    "Anime News Network",
    "https://www.animenewsnetwork.com/",
    "anime_news_network.png",
    listOf(FranceCountry::class.java)
) {
    val converter = AnimeNewsNetworkConverter(this)

    fun xmlToJson(content: String) =
        Const.gson.fromJson(Const.objectMapper.writeValueAsString(Const.xmlMapper.readTree(content)), JsonObject::class.java)
            ?.getAsJsonObject("channel")?.getAsJsonArray("item")?.mapNotNull { it.asJsonObject }

    private fun xmlToJsonWithFilter(
        calendar: Calendar,
        content: String
    ): List<JsonObject>? {
        return xmlToJson(content)
            ?.filter {
                val releaseDate = CalendarConverter.fromGMTLine(it.get("pubDate")?.asString)
                releaseDate?.toDate() == calendar.toDate()
            }
    }

    private fun getNewsAPIContent(checkedCountry: ICountry, calendar: Calendar): List<JsonObject>? {
        val lang = when (checkedCountry) {
            is FranceCountry -> "fr"
            else -> throw CountryNotSupportedException("Country not supported")
        }

        return try {
            val apiUrl = "https://www.animenewsnetwork.com/all/rss.xml?ann-edition=$lang"
            val content = URL(apiUrl).readText()
            xmlToJsonWithFilter(calendar, content)
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error while getting API content", e)
            null
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
}
