package fr.jais.scraper.utils

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter
import java.util.logging.Logger

object Logger : Logger("Scraper", null) {
    class LogFormatter : Formatter() {
        private val reset = "\u001B[0m"
        private val red = "\u001B[31m"
        private val green = "\u001B[32m"
        private val yellow = "\u001B[33m"
        private val blue = "\u001B[34m"
        private val purple = "\u001B[35m"
        private val cyan = "\u001B[36m"
        private val white = "\u001B[37m"
        private val simpleDateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.FRANCE)

        override fun format(record: LogRecord?): String {
            val message = formatMessage(record)
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            pw.println()
            record?.thrown?.printStackTrace(pw)
            pw.close()
            val throwable: String = sw.toString()
            val color = when (record?.level) {
                Level.SEVERE -> red
                Level.WARNING -> yellow
                Level.INFO -> green
                Level.CONFIG -> blue
                Level.FINE -> purple
                Level.FINER -> cyan
                Level.FINEST -> white
                else -> reset
            }
            return "$color[${this.simpleDateFormat.format(Date())} ${record?.level?.localizedName}] ${message}${throwable}${if (throwable.isEmpty()) System.lineSeparator() else ""}${reset}"
        }
    }

    init {
        val formatter = LogFormatter()

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