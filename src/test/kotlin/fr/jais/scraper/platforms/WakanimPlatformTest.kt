package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.utils.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.expect

internal class WakanimPlatformTest {
    private val scraper = Scraper()
    private val country = FranceCountry()
    private val platform = WakanimPlatform(scraper)

    private fun testCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(2022, Calendar.SEPTEMBER, 16, 23, 59, 0)
        return calendar
    }

    @Test
    fun addCacheCatalogue() {
        platform.addCacheCatalogue()
        expect(true) { platform.cacheCatalogue.isNotEmpty() }
    }

    @Test
    fun getAgendaEpisode() {
        platform.addCacheCatalogue()
        expect(2) { platform.getAgendaEpisode(country, testCalendar()).size }
    }

    @Test
    fun convertEpisode() {
        platform.addCacheCatalogue()
        expect(2) { platform.getAgendaEpisode(country, testCalendar()).map { platform.converter.convertEpisode(testCalendar(), it) }.size }
    }
}
