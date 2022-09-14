package fr.jais.scraper.platforms

abstract class IPlatform(val name: String, val url: String, image: String) {
    abstract fun getEpisodes()
    abstract fun getNews()
    abstract fun getMangas()
}