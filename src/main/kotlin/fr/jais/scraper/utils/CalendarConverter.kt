package fr.jais.scraper.utils

import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

object CalendarConverter {
    private val utcFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val gmtLineFormatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    private val utcWithTimezoneFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    fun toUTCDate(iso8601string: String?): String {
        this.utcFormatter.timeZone = TimeZone.getTimeZone("UTC")
        return this.utcFormatter.format(Date.from(ZonedDateTime.parse(iso8601string).toInstant()))
    }

    fun fromUTCTimestampString(iso8601calendar: Calendar?): String {
        this.utcFormatter.timeZone = TimeZone.getTimeZone("UTC")
        return this.utcFormatter.format(Date.from(iso8601calendar?.toInstant()))
    }

    fun fromUTCDate(iso8601string: String?): Calendar? {
        if (iso8601string.isNullOrBlank()) return null
        val calendar = Calendar.getInstance()
        this.utcFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val date = this.utcFormatter.parse(toUTCDate(iso8601string))
        calendar.time = date
        return calendar
    }

    fun fromGMTLine(line: String?): Calendar? {
        if (line.isNullOrBlank()) return null
        val calendar = GregorianCalendar.getInstance()
        val date = this.gmtLineFormatter.parse(line)
        calendar.time = date
        val formatted = this.utcWithTimezoneFormatter.format(date)
        return fromUTCDate("${formatted.substring(0, 22)}:${formatted.substring(22)}")
    }
}