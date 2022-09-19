package fr.jais.scraper.commands

import fr.jais.scraper.Scraper
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.platforms.IPlatform
import fr.jais.scraper.utils.CalendarConverter
import fr.jais.scraper.utils.Logger
import fr.jais.scraper.utils.toISO8601
import java.text.SimpleDateFormat
import java.util.*

private const val separator = "------------------------------"

class CheckCommand(scraper: Scraper) : ICommand(scraper, "check", "Check episode availability") {
    private fun setCheckedCalendar(calendar: Calendar): Calendar {
        return CalendarConverter.fromUTCDate("${SimpleDateFormat("yyyy-MM-dd").format(Date.from(calendar.toInstant()))}T23:50:00Z")!!
    }

    override fun execute(args: List<String>) {
        if (args.isEmpty()) return

        val list = mutableListOf<Episode>()

        if (args.firstOrNull() == "--month") {
            if (args.size == 1) {
                val calendar = Calendar.getInstance()
                val dayInMonth = calendar.get(Calendar.DAY_OF_MONTH) - 1
                checkForDay(dayInMonth, list)
                list.forEach { println(it) }
                return
            }

            val month = (args[1].toIntOrNull() ?: 1) * 30
            checkForDay(month, list)
            list.forEach { println(it) }
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy")

        args.forEach { date ->
            var calendar = Calendar.getInstance()
            calendar.time = sdf.parse(date)
            calendar = setCheckedCalendar(calendar)

            Logger.info("Check for ${calendar.toISO8601()}")
            Logger.info(separator)
            list.addAll(scraper.getAllEpisodes(calendar, platformType = IPlatform.PlatformType.API))
            Logger.info(separator)
        }

        list.forEach { println(it) }
    }

    private fun checkForDay(dayInMonth: Int, list: MutableList<Episode>) {
        for (i in dayInMonth downTo 1) {
            var checkedCalendar = Calendar.getInstance()
            checkedCalendar.add(Calendar.DAY_OF_MONTH, -i)
            checkedCalendar = setCheckedCalendar(checkedCalendar)

            Logger.info("Check for ${checkedCalendar.toISO8601()}")
            Logger.info(separator)
            list.addAll(scraper.getAllEpisodes(checkedCalendar, platformType = IPlatform.PlatformType.API))
            Logger.info(separator)
        }
    }
}
