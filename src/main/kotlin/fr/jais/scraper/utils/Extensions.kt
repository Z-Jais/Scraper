package fr.jais.scraper.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.*

fun String.toHTTPS() = this.replace("http://", "https://")
fun Calendar.toISO8601(): String = CalendarConverter.fromUTCTimestampString(this)
fun Calendar.toDate(): String = CalendarConverter.fromUTCTimestampString(this).substringBefore("T")

fun JsonElement.asString(): String? = if (this.isJsonNull) null else this.asString
fun JsonElement.asJsonArray(): JsonArray? = if (this.isJsonArray) this.asJsonArray else null
fun JsonElement.asLong(): Long? = if (this.isJsonNull) null else this.asLong
fun JsonElement.asJsonObject(): JsonObject? = if (this.isJsonObject) this.asJsonObject else null