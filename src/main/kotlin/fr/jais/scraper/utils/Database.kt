package fr.jais.scraper.utils

import com.google.gson.GsonBuilder
import fr.jais.scraper.entities.Episode
import java.io.File

class Database {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("database.json")

    fun load(): MutableList<Episode> =
        gson.fromJson(
            try {
                file.readText()
            } catch (e: Exception) {
                "[]"
            },
            Array<Episode>::class.java
        )?.toMutableList() ?: mutableListOf()

    fun save(episodes: List<Episode>) = file.writeText(gson.toJson(episodes))

    companion object {
        fun save(episodes: List<Episode>) {
            if (episodes.isNotEmpty()) {
                val database = Database()
                Logger.info("Loading database...")
                val episodesInDatabase = database.load()
                val episodesToAdd = episodes.filter { episodesInDatabase.none { epDb -> it.hash == epDb.hash } }
                Logger.config("Episodes to add: ${episodesToAdd.size}")

                if (episodesToAdd.isNotEmpty()) {
                    Logger.info("Adding episodes to database...")
                    episodesInDatabase.addAll(episodesToAdd)
                    database.save(episodesInDatabase)
                    Logger.info("Adding episodes to database done.")
                }
            }
        }
    }
}
