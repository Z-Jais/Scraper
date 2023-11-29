package fr.jais.scraper.utils

import com.google.gson.Gson

object Const {
    val gson = Gson()
    val multipleSpaceRegex = "\\s+".toRegex()
    const val calendarBaseUrl = "https://anime.icotaku.com"
    val apiUrl = System.getenv("API_URL") ?: "http://localhost:8080/"
    val secureKey: String? = System.getenv("SECURE_KEY")
}
