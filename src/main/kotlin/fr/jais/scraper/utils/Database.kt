package fr.jais.scraper.utils

import com.google.gson.Gson
import fr.jais.scraper.entities.Episode
import java.io.File

class Database {
    private val gson = Gson()
    private val file = File("database")

    fun load(): MutableList<Episode> =
        gson.fromJson(
            try {
                Gzip.decode(file.readBytes())
            } catch (e: Exception) {
                "[]"
            },
            Array<Episode>::class.java
        )?.toMutableList() ?: mutableListOf()

    private fun save(episodes: List<Episode>) {
        val json = gson.toJson(episodes).toByteArray()
        val jsonSizeInMiB = json.size.toDouble() / 1024.0
        val gzip = Gzip.encode(json)
        val gzipSizeInMiB = gzip.size.toDouble() / 1024.0
        Logger.config("Database size: ${jsonSizeInMiB.toString(2)} KiB -> ${gzipSizeInMiB.toString(2)} KiB")
        file.writeBytes(gzip)
    }

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
