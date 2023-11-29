package fr.jais.scraper.countries

import fr.jais.scraper.entities.Country

abstract class ICountry(private val code: String, val name: String) {
    fun getCountry() = Country(code, name)

    override fun toString(): String {
        return "ICountry(code='$code', name='$name')"
    }
}
