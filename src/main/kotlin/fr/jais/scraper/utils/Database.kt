package fr.jais.scraper.utils

import fr.jais.scraper.entities.Episode
import java.io.File

private const val LOADING_DATABASE = "Loading database..."

class Database {
    data class DB(
        val episodes: MutableList<Episode> = mutableListOf()
    )

    private val file = File("data/database.txt")

    fun load(): DB =
        Const.gson.fromJson(
            try {
                Gzip.decode(file.readText())
            } catch (e: Exception) {
                "{}"
            },
            DB::class.java
        ) ?: DB()

    private fun save(db: DB) {
        val json = Const.gson.toJson(db)
        val jsonSizeInMiB = json.toByteArray().size.toDouble() / 1024.0
        val gzip = Gzip.encode(json)
        val gzipSizeInMiB = gzip.toByteArray().size.toDouble() / 1024.0
        Logger.config("Database size: ${jsonSizeInMiB.toString(2)} KiB -> ${gzipSizeInMiB.toString(2)} KiB")
        file.writeText(gzip)
    }

    companion object {
        fun loadEpisodes(): List<Episode> {
            Logger.info(LOADING_DATABASE)
            val db = Database().load()
            Logger.config("Episodes: ${db.episodes.size}")
            return db.episodes
        }

        fun saveEpisodes(episodes: List<Episode>) {
            if (episodes.isEmpty()) {
                Logger.warning("No episodes to save in database")
                return
            }

            val database = Database()
            Logger.info(LOADING_DATABASE)
            val db = database.load()
            val episodesToAdd = episodes.filter { db.episodes.none { epDb -> it.hash == epDb.hash } }
            Logger.config("Episodes to add: ${episodesToAdd.size}")

            if (episodesToAdd.isEmpty()) {
                Logger.warning("All episodes are already check!")
                return
            }

            Logger.info("Adding episodes to database...")
            db.episodes.addAll(episodesToAdd)
            database.save(db)
            Logger.info("Adding episodes to database done.")
        }
    }
}
