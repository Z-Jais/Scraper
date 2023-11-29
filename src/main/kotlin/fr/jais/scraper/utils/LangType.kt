package fr.jais.scraper.utils

enum class LangType(vararg data: String) {
    UNKNOWN,
    SUBTITLES("vostfr", "vostf"),
    VOICE("vf", "french dub"),
    ;

    val data: List<String> = data.toList()

    companion object {
        fun fromString(string: String): LangType {
            return entries.firstOrNull { it.data.contains(string.lowercase()) } ?: UNKNOWN
        }
    }
}
