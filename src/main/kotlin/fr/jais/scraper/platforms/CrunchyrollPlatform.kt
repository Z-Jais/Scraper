package fr.jais.scraper.platforms

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.converters.CrunchyrollConverter
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.exceptions.EpisodeException
import fr.jais.scraper.utils.Browser
import fr.jais.scraper.utils.Const
import fr.jais.scraper.utils.Logger
import java.net.URL
import java.util.*
import java.util.logging.Level

class CrunchyrollPlatform(scraper: Scraper) : IPlatform(
    scraper,
    "Crunchyroll",
    "https://www.crunchyroll.com/",
    "crunchyroll.png",
    listOf(FranceCountry::class.java)
) {
    private val converter = CrunchyrollConverter(this)
    private var lastSimulcastCheck = 0L
    val simulcasts = mutableMapOf<ICountry, Set<String>>()

    private fun checkSimulcasts(iCountry: ICountry) {
        Logger.info("Checking simulcasts for ${iCountry.name}...")
        // Clear simulcast for this country if exists
        simulcasts.remove(iCountry)
        val countryTag = converter.getCountryTag(iCountry)
        Logger.info("Loading simulcasts for ${iCountry.name}...")
        val content = Browser(
            Browser.BrowserType.FIREFOX,
            "https://www.crunchyroll.com/$countryTag/simulcasts"
        ).launchAndWaitForSelector("#content > div > div.app-body-wrapper > div > div > div.erc-browse-collection > div > div:nth-child(1) > div > div > h4 > a")
        simulcasts[iCountry] = content.select(".erc-browse-cards-collection > .browse-card > div > div > h4 > a").map { it.text().lowercase() }.toSet()
        Logger.info("Found ${simulcasts[iCountry]?.size} simulcasts for ${iCountry.name}!")
        Logger.config("Simulcasts: ${simulcasts[iCountry]?.joinToString(", ")}")
    }

    private fun xmlToJson(content: String) =
        Const.gson.fromJson(
            Const.objectMapper.writeValueAsString(Const.xmlMapper.readTree(content)),
            JsonObject::class.java
        )
            ?.getAsJsonObject("channel")?.getAsJsonArray("item")

    fun getLang(checkedCountry: ICountry): String {
        val lang = when (checkedCountry) {
            is FranceCountry -> "frFR"
            else -> throw CountryNotSupportedException("Country not supported")
        }
        return lang
    }

    private fun getEpisodeAPIContent(checkedCountry: ICountry): JsonArray? {
        val lang = getLang(checkedCountry)

        return try {
            val apiUrl = "https://www.crunchyroll.com/rss/anime?lang=$lang"
            val content = URL(apiUrl).readText()
            xmlToJson(content)
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error while getting API content", e)
            null
        }
    }

    override fun getEpisodes(calendar: Calendar, cachedEpisodes: List<String>): List<Episode> {
        val countries = scraper.getCountries(this)

        if (System.currentTimeMillis() - lastSimulcastCheck > 1 * 60 * 60 * 1000) {
            countries.forEach {
                try {
                    checkSimulcasts(it)
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "Error while checking simulcasts", e)
                }
            }

            lastSimulcastCheck = System.currentTimeMillis()
        }

        return countries.flatMap { country ->
            Logger.info("Getting episodes for $name in ${country.name}...")
            getEpisodeAPIContent(country)?.mapNotNull {
                try {
                    converter.convertEpisode(country, it.asJsonObject, cachedEpisodes)
                } catch (e: Exception) {
                    if (e !is EpisodeException) {
                        Logger.log(Level.SEVERE, "Error while converting episode", e)
                    }

                    null
                }
            } ?: emptyList()
        }
    }

    override fun reset() {
        converter.cache.clear()
    }
}
