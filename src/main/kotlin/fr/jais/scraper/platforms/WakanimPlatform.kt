package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry

class WakanimPlatform(scraper: Scraper) : IPlatform(
    scraper,
    PlatformType.API,
    "Wakanim",
    "https://wakanim.tv/",
    listOf(FranceCountry::class.java)
)
