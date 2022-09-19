package fr.jais.scraper.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object Gzip {
    fun decode(string: String): String {
        val gzip = GZIPInputStream(ByteArrayInputStream(Base64.getDecoder().decode(string)))
        val compressed = gzip.readBytes()
        gzip.close()
        return String(compressed)
    }

    fun decode(byteArray: ByteArray): String {
        val gzip = GZIPInputStream(ByteArrayInputStream(byteArray))
        val compressed = gzip.readBytes()
        gzip.close()
        return String(compressed)
    }

    fun encode(string: String): String = Base64.getEncoder().encodeToString(encode(string.toByteArray()))

    fun encode(bytes: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(bytes.size)
        val gzip = GZIPOutputStream(bos)
        gzip.write(bytes)
        gzip.close()
        val compressed = bos.toByteArray()
        bos.close()
        return compressed
    }
}