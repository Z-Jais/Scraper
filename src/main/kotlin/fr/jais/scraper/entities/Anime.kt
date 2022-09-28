package fr.jais.scraper.entities

data class Anime(
    val country: Country,
    var name: String,
    val image: String,
    val description: String? = null,
    val genres: List<String> = emptyList()
) {
    override fun toString(): String {
        return "Anime(country=$country, name='$name', image='$image', description=$description, genres=$genres)"
    }
}
