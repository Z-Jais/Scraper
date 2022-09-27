package fr.jais.scraper.entities

import fr.jais.scraper.utils.toMD5

data class Manga(
    val platform: Platform,
    val anime: Anime,
    val releaseDate: String,
    val url: String,
    val cover: String,
    val editor: String,
    var ref: String? = null,
    var ean: Long? = null,
    var age: Int? = null,
    var price: Double? = null
) {
    val hash = "${platform.name.substring(0 until 4).uppercase()}-${
    "$platform${anime.name}$releaseDate$ref$ean".toMD5().substring(0 until 12).uppercase()
    }"

    override fun toString(): String {
        return "Manga(platform=$platform, anime=$anime, releaseDate='$releaseDate', url='$url', cover='$cover', editor='$editor', ref=$ref, ean=$ean, age=$age, price=$price, hash='$hash')"
    }
}
