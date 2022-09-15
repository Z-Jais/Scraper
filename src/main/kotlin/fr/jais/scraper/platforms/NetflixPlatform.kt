package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry

class NetflixPlatform(scraper: Scraper) : IPlatform(
    scraper,
    PlatformType.FLOWS,
    "Netflix",
    "https://netflix.com/",
    "",
    listOf(FranceCountry::class.java)
)