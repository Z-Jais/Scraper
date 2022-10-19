package fr.jais.scraper.utils

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

    private val file = File("database.txt")

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

        fun loadNews(): List<News> {
            Logger.info(LOADING_DATABASE)
            val db = Database().load()
            Logger.config("News: ${db.news.size}")
            return db.news
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

        fun saveNews(news: List<News>) {
            if (news.isEmpty()) {
                Logger.warning("No news to save in database")
                return
            }

            val database = Database()
            Logger.info(LOADING_DATABASE)
            val db = database.load()
            val newsToAdd = news.filter { db.news.none { nDb -> it.hash == nDb.hash } }
            Logger.config("News to add: ${newsToAdd.size}")

            if (newsToAdd.isEmpty()) {
                Logger.warning("All news are already check!")
                return
            }

            Logger.info("Adding news to database...")
            db.news.addAll(newsToAdd)
            database.save(db)
            Logger.info("Adding news to database done.")
        }

        fun saveMangas(mangas: List<Manga>) {
            if (mangas.isEmpty()) {
                Logger.warning("No mangas to save in database")
                return
            }

            val database = Database()
            Logger.info(LOADING_DATABASE)
            val db = database.load()
            val mangasToAdd = mangas.filter { db.mangas.none { nDb -> it.hash == nDb.hash } }
            Logger.config("Mangas to add: ${mangasToAdd.size}")

            if (mangasToAdd.isEmpty()) {
                Logger.warning("All mangas are already check!")
                return
            }

            Logger.info("Adding mangas to database...")
            db.mangas.addAll(mangasToAdd)
            database.save(db)
            Logger.info("Adding mangas to database done.")
        }
    }
}
