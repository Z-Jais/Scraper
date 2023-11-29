package fr.jais.scraper.utils

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Browser(val url: String, type: BrowserType = BrowserType.FIREFOX) {
    enum class BrowserType {
        CHROME,
        FIREFOX,
    }

    private val playwright: Playwright = Playwright.create()
    private var browser: com.microsoft.playwright.Browser? = null
    private var page: Page? = null
    private val launchOptions = com.microsoft.playwright.BrowserType.LaunchOptions().setHeadless(true)

    init {
        Logger.config("Browser type: ${type.name}")
        Logger.info("Launching browser...")
        browser = when (type) {
            BrowserType.CHROME -> playwright.chromium().launch(launchOptions)
            BrowserType.FIREFOX -> playwright.firefox().launch(launchOptions)
        }

        Logger.info("Creating page...")
        page = browser?.newPage()
        page?.setDefaultTimeout(60_000.0)
        page?.setDefaultNavigationTimeout(60_000.0)
        Logger.config("URL: $url")

        try {
            Logger.info("Navigating...")
            page?.navigate(url)
            Logger.info("Waiting for load...")
            page?.waitForLoadState()
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    fun launch(): Document {
        val content = page?.content()
        close()

        Logger.info("Parsing content...")
        return Jsoup.parse(content ?: throw Exception("Content is null"))
    }

    fun launchAndWaitForSelector(selector: String): Document {
        try {
            page?.waitForSelector(selector)
        } catch (e: Exception) {
            close()
            throw e
        }

        val content = page?.content()
        close()

        Logger.info("Parsing content...")
        return Jsoup.parse(content ?: throw Exception("Content is null"))
    }

    private fun close() {
        Logger.info("Closing browser...")
        page?.close()
        browser?.close()
        playwright.close()
    }
}
