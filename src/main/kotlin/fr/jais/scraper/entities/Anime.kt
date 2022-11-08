package fr.jais.scraper.entities

import fr.jais.scraper.utils.Genre

data class Anime(
    val country: Country,
    var name: String,
    val image: String,
    val description: String? = null,
    val genres: List<Genre> = emptyList()
) {
    override fun toString(): String {
        return "Anime(country=$country, name='$name', image='$image', description=$description, genres=$genres)"
    }
}
