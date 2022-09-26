package fr.jais.scraper.utils

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import fr.jais.scraper.playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Browser(type: BrowserType = BrowserType.CHROME, val url: String) {
    enum class BrowserType {
        CHROME,
        FIREFOX,
    }


    private var browser: com.microsoft.playwright.Browser? = null
    private var context: BrowserContext? = null
    var page: Page? = null

    init {
        Logger.config("Browser type: ${type.name}")
        Logger.info("Launching browser...")
        browser = when (type) {
            BrowserType.CHROME -> playwright?.chromium()?.launch()
            BrowserType.FIREFOX -> playwright?.firefox()?.launch()
        }

        Logger.info("Creating context...")
        context = browser?.newContext()
        Logger.info("Creating page...")
        page = context?.newPage()
        Logger.config("URL: $url")
        Logger.info("Navigating...")
        page?.navigate(url)
        Logger.info("Waiting for load...")
        page?.waitForLoadState()
    }

    fun launch(): Document {
        val content = page?.content()
        Logger.info("Closing browser...")
        close()

        Logger.info("Parsing content...")
        return Jsoup.parse(content ?: throw Exception("Content is null"))
    }

    fun close() {
        page?.close()
        context?.close()
        browser?.close()
    }
}
