package fr.jais.scraper.exceptions.animes

import fr.jais.scraper.exceptions.AnimeException

class NoAnimeNameFoundException(s: String) : AnimeException(s)