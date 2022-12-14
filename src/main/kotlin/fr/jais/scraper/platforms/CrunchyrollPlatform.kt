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
    val simulcasts = mutableMapOf<ICountry, List<String>>()

    private fun checkSimulcasts(iCountry: ICountry) {
        Logger.info("Checking simulcasts for ${iCountry.name}...")
        // Clear simulcast for this country if exists
        simulcasts.remove(iCountry)
        val countryTag = converter.getCountryTag(iCountry)
        // https://www.crunchyroll.com/fr/videos/anime/simulcasts/ajax_page?pg=0
        var page = 0
        var loadMore: Boolean
        val list = mutableListOf<String>()

        do {
            Logger.info("Loading page $page...")
            val content = Browser(
                Browser.BrowserType.FIREFOX,
                "https://www.crunchyroll.com/$countryTag/videos/anime/simulcasts/ajax_page?pg=${page++}"
            ).launch()
            // Get all elements with attribute itemprop="name"
            val elements = content.getElementsByAttributeValue("itemprop", "name").map { it.text().lowercase() }
            list.addAll(elements)
            loadMore = elements.size % 40 == 0

            if (page > 5) {
                Logger.warning("Too many pages, stopping...")
                break
            }

            Logger.config("Load more: $loadMore")
        } while (loadMore)

        simulcasts[iCountry] = list.distinct()
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
