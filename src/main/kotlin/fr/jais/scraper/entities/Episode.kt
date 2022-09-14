package fr.jais.scraper.entities

import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.utils.EpisodeType
import fr.jais.scraper.utils.LangType
import fr.jais.scraper.utils.toISO8601
import java.util.*

data class Episode(
    val platform: IPlatform,
    val anime: Anime,
    val releaseDate: Calendar,
    val season: Int,
    val number: Int,
    val episodeType: EpisodeType,
    val langType: LangType,
    val id: Long,
    val title: String?,
    val url: String,
    val image: String,
    val duration: Long
) {
    override fun toString(): String {
        return "Episode(platform=$platform, anime=$anime, releaseDate=${releaseDate.toISO8601()}, season=$season, number=$number, episodeType=$episodeType, langType=$langType, id=$id, title=$title, url='$url', image='$image', duration=$duration)"
    }
}
