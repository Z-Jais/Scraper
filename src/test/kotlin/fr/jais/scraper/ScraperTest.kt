package fr.jais.scraper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ScraperTest {
    private val scraper = Scraper()

    @Test
    fun getCountries() {
        val countries = scraper.countries
        assertEquals(1, countries.size)
        assertEquals("France", countries[0].name)
    }

    @Test
    fun testGetCountries() {
        val countries = scraper.getCountries(scraper.platforms.first())
        assertEquals(1, countries.size)
        assertEquals("France", countries[0].name)
    }
}