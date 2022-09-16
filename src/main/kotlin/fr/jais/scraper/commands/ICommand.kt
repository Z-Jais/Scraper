package fr.jais.scraper.commands

import fr.jais.scraper.Scraper

abstract class ICommand(val scraper: Scraper, val command: String, val description: String? = null) {
    abstract fun execute(args: List<String>)
}