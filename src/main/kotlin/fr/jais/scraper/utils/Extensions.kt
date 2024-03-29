package fr.jais.scraper.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.util.*

fun String.toHTTPS() = this.replace("http://", "https://")
fun Calendar.toISO8601(): String = CalendarConverter.fromUTCTimestampString(this)
fun Calendar.toDate(): String = CalendarConverter.fromUTCTimestampString(this).substringBefore("T")
fun Calendar.getYear(): String = toDate().split("-").first()

fun JsonElement.asString(): String? = if (this.isJsonNull) null else this.asString?.trim()
fun JsonElement.asJsonArray(): JsonArray? = if (this.isJsonArray) this.asJsonArray else null
fun JsonElement.asLong(): Long? = if (this.isJsonNull) null else this.asLong
fun JsonElement.asJsonObject(): JsonObject? = if (this.isJsonObject) this.asJsonObject else null
fun Double.toString(numberOfDecimals: Int): String = String.format("%.${numberOfDecimals}f", this)

fun String.capitalizeWords(): String {
    val delimiters = arrayOf(" ", ",")

    return this.split(*delimiters).joinToString(" ") {
        it.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(
                Locale.getDefault()
            ) else char.toString()
        }
    }
}

fun String.onlyLettersAndDigits(): String = this.filter { it.isLetterOrDigit() }

fun BufferedImage.opacity(opacity: Float): BufferedImage {
    val w = width
    val h = height
    val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

    val g2 = output.createGraphics()
    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
    g2.drawImage(this, 0, 0, null)
    g2.dispose()

    return output
}

fun BufferedImage.resize(width: Int, height: Int): BufferedImage {
    val tmp = getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH)
    val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2 = resized.createGraphics()
    g2.drawImage(tmp, 0, 0, null)
    g2.dispose()
    return resized
}

fun Graphics.fillRoundRectShadow(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
    val tmpColor = color

    color = Color(0, 0, 0, 50)
    fillRoundRect(x + 3, y + 3, width, height, arcWidth, arcHeight)
    color = tmpColor
    fillRoundRect(x, y, width, height, arcWidth, arcHeight)
}

fun BufferedImage.invert(): BufferedImage {
    for (x in 0 until width) {
        for (y in 0 until height) {
            val rgba = getRGB(x, y)
            val alpha = rgba shr 24 and 0xFF
            val red = rgba shr 16 and 0xFF
            val green = rgba shr 8 and 0xFF
            val blue = rgba and 0xFF
            val rgb = red shl 16 or (green shl 8) or blue

            val invert = 0xFF - rgb
            setRGB(x, y, alpha shl 24 or invert)
        }
    }

    return this
}