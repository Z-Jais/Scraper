package fr.jais.scraper.platforms

import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.converters.AnimationDigitalNetworkConverter
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.utils.Const
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.toDate
import java.net.URI
import java.util.*
import java.util.logging.Level

class AnimationDigitalNetworkPlatform(scraper: Scraper) :
    IPlatform(
        scraper,
        "Animation Digital Network",
        "https://animationdigitalnetwork.fr/",
        "animation_digital_network.png",
        listOf(FranceCountry::class.java)
    ) {
    val converter = AnimationDigitalNetworkConverter(this)
    private var apiContent: List<JsonObject>? = null
    private var lastCheck = 0L

    private fun getAPIContent(checkedCountry: ICountry, calendar: Calendar): List<JsonObject>? {
        if (checkedCountry !is FranceCountry) throw CountryNotSupportedException("Country not supported")

        if (apiContent == null || System.currentTimeMillis() - lastCheck > 1 * 60 * 60 * 1000) {
            lastCheck = System.currentTimeMillis()
            Logger.info("Getting API content...")

            apiContent = try {
                val apiUrl = "https://gw.api.animationdigitalnetwork.fr/video/calendar?date=${calendar.toDate()}"
                val content = URI(apiUrl).toURL().readText()
                getVideosArray(content)
            } catch (e: Exception) {
                Logger.log(Level.SEVERE, "Error while getting API content", e)
                null
            }
        }

        return apiContent
    }

    fun getVideosArray(content: String) =
        Const.gson.fromJson(content, JsonObject::class.java)?.getAsJsonArray("videos")
            ?.mapNotNull { it.asJsonObject }

    override fun getEpisodes(calendar: Calendar, cachedEpisodes: List<String>): List<Episode> {
        val countries = scraper.getCountries(this)
        return countries.flatMap { country ->
            Logger.info("Getting episodes for $name in ${country.name}...")
            getAPIContent(country, calendar)?.mapNotNull {
                try {
                    converter.convertEpisode(country, calendar, it, cachedEpisodes)
                } catch (e: Exception) {
                    Logger.log(Level.SEVERE, "Error while converting episode", e)
                    null
                }
            } ?: emptyList()
        }
    }

    override fun reset() {
        apiContent = null
    }
}
