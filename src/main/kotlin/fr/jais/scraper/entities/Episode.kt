package fr.jais.scraper.entities

import fr.jais.scraper.exceptions.CountryNotSupportedException
import fr.jais.scraper.utils.EpisodeType
import fr.jais.scraper.utils.LangType

data class Episode(
    val platform: Platform,
    val anime: Anime,
    val releaseDate: String,
    val season: Int,
    val number: Int,
    val episodeType: EpisodeType,
    val langType: LangType,
    @Transient
    val id: Long,
    var title: String?,
    val url: String,
    val image: String,
    val duration: Long
) {
    var hash = calculateHash(platform, id, anime.country.tag, langType)

    init {
        title = title?.ifBlank { null }
    }

    override fun toString(): String {
        return "Episode(platform=$platform, anime=$anime, releaseDate=$releaseDate, season=$season, number=$number, episodeType=$episodeType, langType=$langType, id=$id, title=$title, url='$url', image='$image', duration=$duration, hash='$hash')"
    }

    companion object {
        private fun suffix(tag: String, langType: LangType): String {
            return when (tag) {
                "fr" -> {
                    if (langType == LangType.SUBTITLES) "VOSTFR" else "VF"
                }

                else -> throw CountryNotSupportedException("Country $tag is not supported")
            }
        }

        fun calculateHash(platform: Platform, id: Long, tag: String, langType: LangType) =
            "${platform.name.substring(0 until 4).uppercase()}-$id-${suffix(tag, langType)}"
    }
}
