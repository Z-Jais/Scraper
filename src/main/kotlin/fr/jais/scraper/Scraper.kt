package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform
import fr.jais.scraper.platforms.IPlatform

class Scraper {
    val platforms = listOf(AnimationDigitalNetworkPlatform(this))
    private val countries = mutableSetOf<ICountry>()

    fun getCountries(): List<ICountry> =
        platforms.flatMap { it.countries }.distinct().map { it.getConstructor().newInstance() }

    fun getCountries(platform: IPlatform): List<ICountry> =
        countries.filter { platform.countries.contains(it.javaClass) }

    init {
        countries.addAll(getCountries())
    }
}

fun main(args: Array<String>) {
    Scraper()
}
