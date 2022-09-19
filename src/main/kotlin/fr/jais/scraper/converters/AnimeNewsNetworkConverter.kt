package fr.jais.scraper.converters

import com.google.gson.JsonObject
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.News
import fr.jais.scraper.exceptions.news.NewsDescriptionNotFoundException
import fr.jais.scraper.exceptions.news.NewsReleaseDateNotFoundException
import fr.jais.scraper.exceptions.news.NewsTitleNotFoundException
import fr.jais.scraper.exceptions.news.NewsUrlNotFoundException
import fr.jais.scraper.platforms.AnimeNewsNetworkPlatform
import fr.jais.scraper.utils.*
import org.jsoup.Jsoup

class AnimeNewsNetworkConverter(private val platform: AnimeNewsNetworkPlatform) {
    fun convertNews(checkedCountry: ICountry, jsonObject: JsonObject): News {
        Logger.config("Convert news from $jsonObject")

        Logger.info("Get release date...")
        val releaseDate = CalendarConverter.fromGMTLine(jsonObject.get("pubDate")?.asString())
            ?: throw NewsReleaseDateNotFoundException("No release date found")
        Logger.config("Release date: ${releaseDate.toISO8601()}")

        Logger.info("Get title...")
        val title = jsonObject.get("title")?.asString() ?: throw NewsTitleNotFoundException("No title found")
        Logger.config("Title: $title")

        Logger.info("Get description...")
        val description = Jsoup.parse(
            jsonObject.get("title")?.asString() ?: throw NewsDescriptionNotFoundException("No description found")
        ).text()
        Logger.config("description: $description")

        Logger.info("Get url...")
        val url = jsonObject.get("link")?.asString()?.toHTTPS() ?: throw NewsUrlNotFoundException("No url found")
        Logger.config("Url: $url")

        return News(
            platform.getPlatform(),
            checkedCountry.getCountry(),
            releaseDate.toISO8601(),
            title,
            description,
            url
        )
    }
}
