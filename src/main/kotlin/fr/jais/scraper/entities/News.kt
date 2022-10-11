package fr.jais.scraper.entities

import fr.jais.scraper.utils.toMD5

data class News(
    val platform: Platform,
    val country: Country,
    val releaseDate: String,
    var title: String,
    val description: String,
    val url: String
) {
    val hash = calculateHash(platform, releaseDate, country)

    override fun toString(): String {
        return "News(platform=$platform, country=$country, releaseDate='$releaseDate', title='$title', description='$description', url='$url', hash='$hash')"
    }

    companion object {
        fun calculateHash(platform: Platform, releaseDate: String, country: Country) =
            "${platform.name.substring(0 until 4).uppercase()}-${
                "$platform$releaseDate$country".toMD5().substring(0 until 12).uppercase()
            }"
    }
}
