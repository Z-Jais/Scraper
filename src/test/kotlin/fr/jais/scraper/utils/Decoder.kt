package fr.jais.scraper.utils

import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.GZIPInputStream

object Decoder {
    fun fromGzip(string: String): String {
        val gzip = GZIPInputStream(ByteArrayInputStream(Base64.getDecoder().decode(string)))
        val compressed = gzip.readBytes()
        gzip.close()
        return String(compressed)
    }
}
