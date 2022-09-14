package fr.jais.scraper.utils

import java.text.SimpleDateFormat
import java.util.*

fun String.toHTTPS() = this.replace("http://", "https://")
fun Calendar.toISO8601(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return simpleDateFormat.format(Date.from(this.toInstant()))
}