package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.countries.ICountry
import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Manga
import fr.jais.scraper.utils.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.logging.Level

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
            page.locator("//*[@id=\"main\"]/form[1]/button").click()
            page.waitForLoadState()
            val elements = page.querySelectorAll("tr")?.filter { it.getAttribute("id") != null }
            val checkDate = calendar.toFrenchDate().replace("-", "/")

            val mangas = elements?.filter {
                val dateOut = it.querySelector(".date_out")?.textContent()?.replace("\n", " ")?.trim()
                    ?: throw Exception("No date found")
                dateOut == checkDate
            }?.mapNotNull {
                val dateOut = it.querySelector(".date_out")?.textContent()?.replace("\n", " ")?.trim()
                    ?: throw Exception("No date found")
                val titleElement = it.querySelector(".title") ?: throw Exception("No title found")
                val title = titleElement.textContent()?.replace("\n", " ")?.trim() ?: throw Exception("No title found")
                val link = titleElement.querySelector("a")?.getAttribute("href")?.replace("\n", " ")?.trim()
                    ?: throw Exception("No link found")
                val editor = it.querySelectorAll("td")?.lastOrNull()?.textContent()?.replace("\n", " ")?.trim()
                    ?: throw Exception("No editor found")
                val image = it.querySelector("img")?.getAttribute("src")?.replace("\n", " ")?.trim() ?: throw Exception(
                    "No image found"
                )

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
            } ?: throw Exception("No elements found")

            browser.close()

            val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            newFixedThreadPool.invokeAll(
                mangas.map {
                    Callable {
                        val content = Browser(Browser.BrowserType.CHROME, it.url).launch()
                        val baseName =
                            content.selectXpath("//*[@id=\"breadcrumb\"]/span[4]/a").text().replace("\n", " ").trim()
                        val ref = it.anime.name.replace(baseName, "").trim()
                        it.ref = ref
                        val ean = content.select("[itemprop=\"isbn\"]").text().replace("\n", " ").trim().toLongOrNull()
                        it.ean = ean
                        val age =
                            content.select("#agenumber").text().replace("\n", " ").replace("+", "").trim().toIntOrNull()
                        it.age = age
                        val price = content.select("#prixnumber").text().replace("\n", " ").replace("€", "").trim()
                            .toDoubleOrNull()
                        it.price = price
                    }
                }
            )

            newFixedThreadPool.shutdown()

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
