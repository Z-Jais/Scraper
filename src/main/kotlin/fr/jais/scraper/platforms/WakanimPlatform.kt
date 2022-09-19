package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonArray
import fr.jais.scraper.Scraper
import fr.jais.scraper.converters.WakanimConverter
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.exceptions.CatalogueNotFoundException
import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.exceptions.animes.AnimeImageNotFoundException
import fr.jais.scraper.exceptions.animes.AnimeNameNotFoundException
import fr.jais.scraper.exceptions.episodes.EpisodeNumberNotFoundException
import fr.jais.scraper.utils.*
import java.net.URL
import java.util.*
import java.util.logging.Level

class WakanimPlatform(scraper: Scraper) : IPlatform(
    scraper,
    PlatformType.API,
    "Wakanim",
    "https://wakanim.tv/",
    listOf(FranceCountry::class.java)
) {
    data class WakanimCatalogue(
        val name: String,
        val image: String,
        val description: String?,
        val genres: List<String>
    )

    data class WakanimAgendaEpisode(
        val iCountry: ICountry,
        val releaseDate: String,
        val anime: Anime,
        val number: Int,
        val episodeType: EpisodeType,
        val langType: LangType,
        val tmpUrl: String
    )

    val converter = WakanimConverter(this)
    val cacheCatalogue = mutableListOf<WakanimCatalogue>()
    private val cacheAgenda = mutableListOf<WakanimAgendaEpisode>()
    private var lastCheck = 0L

    private fun getCatalogue(): List<WakanimCatalogue> {
        val content = Gson().fromJson(URL("https://account.wakanim.tv/api/catalogue").readText(), JsonArray::class.java) ?: throw CatalogueNotFoundException("Wakanim catalogue not found")
        return content.filter { it?.isJsonObject == true }.mapNotNull { element ->
            val obj = element.asJsonObject
            WakanimCatalogue(
                obj.get("name")?.asString() ?: throw AnimeNameNotFoundException("Wakanim anime name not found"),
                obj.get("imageUrl")?.asString()?.toHTTPS() ?: throw AnimeImageNotFoundException("Wakanim anime image not found"),
                obj.get("smallSummary")?.asString(),
                obj.get("genres")?.asJsonArray?.mapNotNull { it.asJsonObject()?.get("name")?.asString() } ?: emptyList()
            )
        }
    }

    fun getAgendaEpisode(checkedCountry: ICountry, calendar: Calendar): List<WakanimAgendaEpisode> {
        val lang = when (checkedCountry) {
            is FranceCountry -> "fr"
            else -> throw CountryNotSupportedException("Country not supported")
        }

        val date = calendar.toFrenchDate()
        val content = Browser(Browser.BrowserType.FIREFOX, "https://www.wakanim.tv/$lang/v2/agenda/getevents?s=$date&e=$date&free=false").launch()

        return content.select(".Calendar-ep").mapNotNull {
            val textSplit = it?.text()?.split(" ") ?: return@mapNotNull null
            val releaseDate = "${calendar.toDate()}T${textSplit[0]}:00Z"
            val toIndex = textSplit.indexOf("Séries")
            val anime = textSplit.subList(1, toIndex).joinToString(" ")
            val number = textSplit[textSplit.size - 2].replace(" ", "").toIntOrNull() ?: throw EpisodeNumberNotFoundException("Episode number not found")
            val etc = textSplit.subList(toIndex + 1, textSplit.size - 2).joinToString(" ")
            val episodeType = if (etc.contains("Film", true)) { EpisodeType.FILM } else if (etc.contains("Spécial", true) || etc.contains("OAV", true)) { EpisodeType.SPECIAL } else { EpisodeType.EPISODE }
            val langType = LangType.fromString(textSplit[textSplit.size - 1].replace(" ", ""))
            val tmpUrl = "https://www.wakanim.tv${it.selectFirst(".Calendar-linkImg")?.attr("href")}"

            val catalogue = this.cacheCatalogue.firstOrNull { catalogue -> catalogue.name.equals(anime, true) } ?: throw CatalogueNotFoundException("Wakanim catalogue not found")

            WakanimAgendaEpisode(
                checkedCountry,
                releaseDate,
                Anime(
                    checkedCountry.getCountry(),
                    anime,
                    catalogue.image,
                    catalogue.description,
                    catalogue.genres
                ),
                number,
                episodeType,
                langType,
                tmpUrl
            )
        }
    }

    override fun getEpisodes(calendar: Calendar): List<Episode> {
        addCacheCatalogue()
        val countries = scraper.getCountries(this)

        val needCheck = System.currentTimeMillis() - lastCheck > 1 * 60 * 60 * 1000

        if (needCheck) {
            lastCheck = System.currentTimeMillis()
        }

        return countries.flatMap { country ->
            try {
                if (needCheck || cacheAgenda.none { it.iCountry == country }) {
                    Logger.info("Get agenda for ${country.name}...")
                    val agenda = getAgendaEpisode(country, calendar)
                    Logger.config("Agenda for ${country.name} found (${agenda.size} episodes)")

                    cacheAgenda.addAll(agenda)
                }

                cacheAgenda.map { converter.convertEpisode(calendar, it) }
            } catch (e: Exception) {
                Logger.log(Level.SEVERE, "Error while converting episode", e)
                emptyList()
            }
        }
    }

    fun addCacheCatalogue() {
        if (cacheCatalogue.isEmpty()) {
            cacheCatalogue.addAll(getCatalogue())
        }
    }

    override fun reset() {
        cacheCatalogue.clear()
        cacheAgenda.clear()
        converter.cache.clear()
    }
}
