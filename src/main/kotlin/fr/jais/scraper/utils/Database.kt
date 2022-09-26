package fr.jais.scraper.utils

import com.google.gson.Gson
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.Manga
import fr.jais.scraper.entities.News
import java.io.File

private const val LOADING_DATABASE = "Loading database..."

class Database {
    data class DB(
        val episodes: MutableList<Episode> = mutableListOf(),
        val news: MutableList<News> = mutableListOf(),
        val mangas: MutableList<Manga> = mutableListOf()
    )

    private val gson = Gson()
    private val file = File("database.txt")

    fun load(): DB =
        gson.fromJson(
            try {
                Gzip.decode(file.readText())
            } catch (e: Exception) {
                "{}"
            },
            DB::class.java
        ) ?: DB()

    private fun save(db: DB) {
        val json = gson.toJson(db)
        val jsonSizeInMiB = json.toByteArray().size.toDouble() / 1024.0
        val gzip = Gzip.encode(json)
        val gzipSizeInMiB = gzip.toByteArray().size.toDouble() / 1024.0
        Logger.config("Database size: ${jsonSizeInMiB.toString(2)} KiB -> ${gzipSizeInMiB.toString(2)} KiB")
        file.writeText(gzip)
    }

    companion object {
        fun saveEpisodes(episodes: List<Episode>) {
            if (episodes.isNotEmpty()) {
                val database = Database()
                Logger.info(LOADING_DATABASE)
                val db = database.load()
                val episodesToAdd = episodes.filter { db.episodes.none { epDb -> it.hash == epDb.hash } }
                Logger.config("Episodes to add: ${episodesToAdd.size}")

                if (episodesToAdd.isNotEmpty()) {
                    Logger.info("Adding episodes to database...")
                    db.episodes.addAll(episodesToAdd)
                    database.save(db)
                    Logger.info("Adding episodes to database done.")
                }
            }
        }

        fun saveNews(news: List<News>) {
            if (news.isNotEmpty()) {
                val database = Database()
                Logger.info(LOADING_DATABASE)
                val db = database.load()
                val newsToAdd = news.filter { db.news.none { nDb -> it.hash == nDb.hash } }
                Logger.config("News to add: ${newsToAdd.size}")

                if (newsToAdd.isNotEmpty()) {
                    Logger.info("Adding news to database...")
                    db.news.addAll(newsToAdd)
                    database.save(db)
                    Logger.info("Adding news to database done.")
                }
            }
        }

        fun saveMangas(mangas: List<Manga>) {
            if (mangas.isNotEmpty()) {
                val database = Database()
                Logger.info(LOADING_DATABASE)
                val db = database.load()
                val mangasToAdd = mangas.filter { db.mangas.none { nDb -> it.hash == nDb.hash } }
                Logger.config("Mangas to add: ${mangasToAdd.size}")

                if (mangasToAdd.isNotEmpty()) {
                    Logger.info("Adding mangas to database...")
                    db.mangas.addAll(mangasToAdd)
                    database.save(db)
                    Logger.info("Adding mangas to database done.")
                }
            }
        }
    }
}
