package fr.jais.scraper.utils

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Logger

object Logger : Logger("Scraper", null) {
    class Formatter : java.util.logging.Formatter() {
        private val simpleDateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.FRANCE)

        override fun format(record: LogRecord?): String {
            val message = formatMessage(record)
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            pw.println()
            record?.thrown?.printStackTrace(pw)
            pw.close()
            val throwable: String = sw.toString()
            return "[${this.simpleDateFormat.format(Date())} ${record?.level?.localizedName}] ${message}${throwable}${if (throwable.isEmpty()) System.lineSeparator() else ""}"
        }
    }

    init {
        val formatter = Formatter()

        this.useParentHandlers = false
        val consoleHandler = ConsoleHandler()
        consoleHandler.formatter = formatter
        consoleHandler.level = Level.ALL
        this.addHandler(consoleHandler)
        val logsFolder = File("logs")
        if (!logsFolder.exists()) logsFolder.mkdirs()
        val fileHandler = FileHandler("${logsFolder.absolutePath}${File.separator}log.log", 5 * 1024 * 1024, 1, true)
        fileHandler.formatter = formatter
        fileHandler.level = Level.ALL
        this.addHandler(fileHandler)
        this.level = Level.ALL
    }
}