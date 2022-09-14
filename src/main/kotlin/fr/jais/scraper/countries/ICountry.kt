package fr.jais.scraper.countries

abstract class ICountry(val code: String, val name: String) {
    override fun toString(): String {
        return "ICountry(code='$code', name='$name')"
    }
}