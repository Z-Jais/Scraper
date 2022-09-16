package fr.jais.scraper.utils

import com.google.gson.GsonBuilder
import fr.jais.scraper.entities.Episode
import java.io.File

class Database {
    private val file = File("database.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun load(): MutableList<Episode> = gson.fromJson(file.readText(), Array<Episode>::class.java)?.toMutableList() ?: mutableListOf()
    fun save(episodes: List<Episode>) = file.writeText(gson.toJson(episodes))
}