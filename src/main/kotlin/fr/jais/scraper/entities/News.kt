package fr.jais.scraper.entities

import fr.jais.scraper.utils.Gzip

data class News(
    val platform: Platform,
    val country: Country,
    val releaseDate: String,
    var title: String,
    val description: String,
    val url: String,
) {
    val hash = "${platform.name.substring(0 until 4).uppercase()}-${
        Gzip.encode("$platform$releaseDate$country").substring(0 until 12)
    }"

    override fun toString(): String {
        return "News(platform=$platform, country=$country, releaseDate='$releaseDate', title='$title', description='$description', url='$url', hash='$hash')"
    }
}
