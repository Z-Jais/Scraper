package fr.jais.scraper

import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.platforms.AnimationDigitalNetworkPlatform

fun getCountries(platforms: List<AnimationDigitalNetworkPlatform>): List<ICountry> =
    platforms.flatMap { it.countries }.distinct().map { it.getConstructor().newInstance() }

fun main(args: Array<String>) {
    val platforms = listOf(AnimationDigitalNetworkPlatform())
    val countries = getCountries(platforms)
}
