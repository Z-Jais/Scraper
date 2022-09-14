package fr.jais.scraper.entities

import fr.jais.scraper.platforms.IPlatform
import java.util.*

data class Episode(
    val platform: IPlatform,
    val anime: Anime,
    val releaseDate: Calendar,
    val season: Int,
    val number: Int,
    val title: String?,
    val url: String,
    val image: String,
    val duration: Long
)
