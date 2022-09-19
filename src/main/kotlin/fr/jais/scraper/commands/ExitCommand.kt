package fr.jais.scraper.commands

import fr.jais.scraper.Scraper
import kotlin.system.exitProcess

class ExitCommand(scraper: Scraper) : ICommand(scraper, "exit", "Exit the program") {
    override fun execute(args: List<String>) {
        exitProcess(0)
    }
}
