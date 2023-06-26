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
    val converter = CrunchyrollConverter(this)
    private var lastSimulcastCheck = 0L
    val simulcasts = mutableMapOf<ICountry, Set<String>>()

    fun getSimulcastCode(name: String): String {
        // Transform simulcastName to simulcastCode like "spring-2023"
        val simulcastCodeTmp = name.lowercase().replace(" ", "-") // "Printemps 2023" -> "printemps-2023"
        val simulcastYear = simulcastCodeTmp.split("-").last() // "printemps-2023" -> "2023"

        val simulcastSeasonCode = when (simulcastCodeTmp.split("-").first()) { // "printemps-2023" -> "printemps"
            "printemps" -> "spring"
            "été" -> "summer"
            "automne" -> "fall"
            "hiver" -> "winter"
            else -> throw EpisodeException("Simulcast season not found")
        }

        return "$simulcastSeasonCode-$simulcastYear" // "printemps-2023" -> "spring-2023"
    }

    fun getPreviousSimulcastCode(currentSimulcastCode: String): String {
        // Get previous simulcast code "spring-2023" -> "winter-2023"
        // "winter-2023" -> "fall-2022"
        // "fall-2022" -> "summer-2022"
        // "summer-2022" -> "spring-2022"
        return when (currentSimulcastCode.split("-").first()) {
            "spring" -> "winter-${currentSimulcastCode.split("-").last()}"
            "winter" -> "fall-${currentSimulcastCode.split("-").last().toInt() - 1}"
            "fall" -> "summer-${currentSimulcastCode.split("-").last().toInt()}"
            "summer" -> "spring-${currentSimulcastCode.split("-").last().toInt()}"
            else -> throw EpisodeException("Simulcast season not found")
        }
    }

    private fun checkSimulcasts(iCountry: ICountry) {
        Logger.info("Checking simulcasts for ${iCountry.name}...")
        // Clear simulcast for this country if exists
        simulcasts.remove(iCountry)
        val countryTag = converter.getCountryTag(iCountry)
        Logger.info("Loading simulcasts for ${iCountry.name}...")

        val content1 = Browser(
            Browser.BrowserType.FIREFOX,
            "https://www.crunchyroll.com/$countryTag/simulcasts"
        ).launchAndWaitForSelector("#content > div > div.app-body-wrapper > div > div > div.erc-browse-collection > div > div:nth-child(1) > div > div > h4 > a")
        val currentSimulcastAnimes = content1.select(".erc-browse-cards-collection > .browse-card > div > div > h4 > a")
            .map { it.text().lowercase() }.toSet()

        val simulcastName =
            content1.select("#content > div > div.app-body-wrapper > div > div > div.header > div > div > span.call-to-action--PEidl.call-to-action--is-m--RVdkI.select-trigger__title-cta--C5-uH.select-trigger__title-cta--is-displayed-on-mobile--6oNk1")
                .text()
        val simulcastCode = getSimulcastCode(simulcastName)
        Logger.info("Simulcast code for ${iCountry.name}: $simulcastCode")
        val previousSimulcastCode = getPreviousSimulcastCode(simulcastCode)
        Logger.info("Previous simulcast code for ${iCountry.name}: $previousSimulcastCode")

        val content2 = Browser(
            Browser.BrowserType.FIREFOX,
            "https://www.crunchyroll.com/$countryTag/simulcasts/seasons/$previousSimulcastCode"
        ).launchAndWaitForSelector("#content > div > div.app-body-wrapper > div > div > div.erc-browse-collection > div > div:nth-child(1) > div > div > h4 > a")
        val previousSimulcastAnimes =
            content2.select(".erc-browse-cards-collection > .browse-card > div > div > h4 > a")
                .map { it.text().lowercase() }.toSet()

        simulcasts[iCountry] = (currentSimulcastAnimes + previousSimulcastAnimes).toSet()
        Logger.info("Found ${simulcasts[iCountry]?.size} simulcasts for ${iCountry.name}!")
        Logger.config("Simulcasts: ${simulcasts[iCountry]?.joinToString(", ")}")
    }

    fun xmlToJson(content: String) =
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
