package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Manga
import fr.jais.scraper.utils.*
import org.jsoup.Jsoup
import java.util.*
import java.util.logging.Level
import kotlin.system.measureTimeMillis

class MangaNewsPlatform(scraper: Scraper) : IPlatform(
    scraper,
    "MangaNews",
    "https://www.manga-news.com/",
    "manga_news.png",
    listOf(FranceCountry::class.java)
) {
    private fun getAgendaManga(checkedCountry: ICountry, calendar: Calendar): List<Manga>? {
        return try {
            val browser = Browser(
                Browser.BrowserType.CHROME,
                "https://www.manga-news.com/index.php/planning?p_year=${calendar.getYear()}&p_month=${calendar.getMonth()}"
            )
            val page = browser.page ?: throw Exception("Cannot get page")
            page.locator("//*[@id=\"main\"]/form[1]/button").click().run { page.waitForLoadState() }
            val document = Jsoup.parse(page.content())
            val checkDate = calendar.toFrenchDate().replace("-", "/")

            val mangas = document.getElementsByAttribute("id").mapNotNull {
                val dateOut = it.selectFirst(".date_out")?.text() ?: return@mapNotNull null
                if (dateOut != checkDate) return@mapNotNull null
                val titleElement = it.selectFirst(".title") ?: return@mapNotNull null
                val title = titleElement.text()
                val link = titleElement.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val editor = it.select("td").lastOrNull()?.text() ?: return@mapNotNull null
                val image = it.selectFirst("img")?.attr("src") ?: return@mapNotNull null

                Manga(
                    getPlatform(),
                    Anime(
                        checkedCountry.getCountry(),
                        title,
                        image
                    ),
                    dateOut,
                    link,
                    image,
                    editor
                )
            }

            mangas.forEachIndexed { index, manga ->
                Logger.config("Manga ${index + 1}/${mangas.size} : ${manga.anime.name}")

                val time = measureTimeMillis {
                    val content = Jsoup.parse(page.navigate(manga.url).run { page.content() })
                    val baseName = content.selectXpath("//*[@id=\"breadcrumb\"]/span[4]/a").text().trim()
                    val ref = manga.anime.name.replace(baseName, "").trim()
                    manga.ref = ref

                    if (manga.anime.name != baseName) {
                        manga.anime.name = baseName
                    }

                    val ean = content.select("[itemprop=\"isbn\"]").text().trim().toLongOrNull()
                    manga.ean = ean
                    val age = content.select("#agenumber").text().replace("+", "").trim().toIntOrNull()
                    manga.age = age
                    val price = content.select("#prixnumber").text().replace("€", "").trim().toDoubleOrNull()
                    manga.price = price
                }

                Logger.config("Estimated remaining time : ${(mangas.size - index - 1) * time / 1000} seconds")
            }

            browser.close()

            mangas.sortedBy { it.anime.name.lowercase() }
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error while getting API content", e)
            null
        }
    }

    override fun getMangas(calendar: Calendar): List<Manga> {
        val countries = scraper.getCountries(this)
        return countries.flatMap { country ->
            Logger.info("Getting mangas for $name in ${country.name}...")

            try {
                getAgendaManga(country, calendar) ?: emptyList()
            } catch (e: Exception) {
                Logger.log(Level.SEVERE, "Error while getting mangas for $name in ${country.name}", e)
                emptyList()
            }
        }
    }
}
