package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.converters.AnimationDigitalNetworkConverter
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.toDate
import java.net.URL
import java.util.*
import java.util.logging.Level

class AnimationDigitalNetworkPlatform(scraper: Scraper) :
    IPlatform(
        scraper,
        PlatformType.API,
        "Animation Digital Network",
        "https://animationdigitalnetwork.fr/",
        listOf(FranceCountry::class.java)
    ) {
    val converter = AnimationDigitalNetworkConverter(this)

    fun getAPIContent(checkedCountry: ICountry, calendar: Calendar): List<JsonObject>? {
        if (checkedCountry !is FranceCountry) throw CountryNotSupportedException("Country not supported")

        return try {
            val apiUrl = "https://gw.api.animationdigitalnetwork.fr/video/calendar?date=${calendar.toDate()}"
            val content = URL(apiUrl).readText()
            Gson().fromJson(content, JsonObject::class.java)?.getAsJsonArray("videos")?.mapNotNull { it.asJsonObject }
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
}
