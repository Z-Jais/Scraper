package fr.jais.scraper

import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ScraperKtTest {
    private val platforms = listOf(AnimationDigitalNetworkPlatform())

    @Test
    fun getCountries() {
        val countries = getCountries(platforms)
        assertEquals(1, countries.size)
        assertEquals("France", countries[0].name)
    }
}