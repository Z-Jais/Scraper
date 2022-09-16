package fr.jais.scraper.entities

data class Country(val code: String, val name: String) {
    override fun toString(): String {
        return "Country(code='$code', name='$name')"
    }
}
