package fr.jais.scraper.entities

import fr.jais.scraper.countries.ICountry

data class Anime(
    val country: ICountry,
    val name: String,
    val image: String,
    val description: String?,
    val genres: List<String>
) {
    override fun toString(): String {
        return "Anime(country=$country, name='$name', image='$image', description=$description, genres=$genres)"
    }
}
