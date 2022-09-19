package fr.jais.scraper.utils

object Resource {
    fun get(name: String) = javaClass.classLoader.getResourceAsStream(name)?.bufferedReader().use { it?.readText() }
}
