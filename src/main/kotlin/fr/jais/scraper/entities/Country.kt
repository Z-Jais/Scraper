package fr.jais.scraper.entities

data class Country(val tag: String, val name: String) {
    override fun toString(): String {
        return "Country(tag='$tag', name='$name')"
    }
}
