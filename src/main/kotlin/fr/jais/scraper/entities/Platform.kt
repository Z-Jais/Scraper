package fr.jais.scraper.entities

data class Platform(
    val name: String,
    val url: String,
    val image: String
) {
    override fun toString(): String {
        return "Platform(name='$name', url='$url', image='$image')"
    }
}
