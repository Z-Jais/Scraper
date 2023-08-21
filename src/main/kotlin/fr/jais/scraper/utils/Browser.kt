package fr.jais.scraper.utils

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Browser(type: BrowserType = BrowserType.CHROME, val url: String) {
    enum class BrowserType {
        CHROME,
        FIREFOX,
    }

    private var browser: com.microsoft.playwright.Browser? = null
    private var context: BrowserContext? = null
    private var page: Page? = null
    private val launchOptions = com.microsoft.playwright.BrowserType.LaunchOptions().setHeadless(true)

    init {
        Logger.config("Browser type: ${type.name}")
        Logger.info("Launching browser...")
        browser = when (type) {
            BrowserType.CHROME -> Const.chromium.launch(launchOptions)
            BrowserType.FIREFOX -> Const.firefox.launch(launchOptions)
        }

        Logger.info("Creating context...")
        context = browser?.newContext()
        Logger.info("Creating page...")
        page = context?.newPage()
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
        context?.close()
        browser?.close()
    }
}
