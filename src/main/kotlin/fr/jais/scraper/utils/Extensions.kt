package fr.jais.scraper.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

fun String.toHTTPS() = this.replace("http://", "https://")
fun Calendar.toISO8601(): String = CalendarConverter.fromUTCTimestampString(this)
fun Calendar.toDate(): String = CalendarConverter.fromUTCTimestampString(this).substringBefore("T")
fun Calendar.toFrenchDate(): String = toDate().split("-").reversed().joinToString("-")
fun Calendar.getYear(): String = toDate().split("-").first()
fun Calendar.getMonth(): String = toDate().split("-")[1]
fun Calendar.getDay(): String = toDate().split("-").last()

fun JsonElement.asString(): String? = if (this.isJsonNull) null else this.asString?.trim()
fun JsonElement.asJsonArray(): JsonArray? = if (this.isJsonArray) this.asJsonArray else null
fun JsonElement.asLong(): Long? = if (this.isJsonNull) null else this.asLong
fun JsonElement.asJsonObject(): JsonObject? = if (this.isJsonObject) this.asJsonObject else null
fun Double.toString(numberOfDecimals: Int): String = String.format("%.${numberOfDecimals}f", this)

fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

fun String.capitalizeWords(): String = split(" ", "-").joinToString(" ") {
    it.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
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
    val pixels = IntArray(width * height)
    getRGB(0, 0, width, height, pixels, 0, width)

    for (x in 0 until width) {
        for (y in 0 until height) {
            val rgba = pixels[x * width + y]
            val alpha = rgba shr 24 and 0xFF
            val red = rgba shr 16 and 0xFF
            val green = rgba shr 8 and 0xFF
            val blue = rgba and 0xFF
            val rgb = red shl 16 or (green shl 8) or blue

            val invert = 0xFF - rgb
            pixels[x * width + y] = alpha shl 24 or invert
        }
    }

    setRGB(0, 0, width, height, pixels, 0, width)
    return this
}