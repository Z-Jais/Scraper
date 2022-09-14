package fr.jais.scraper.utils

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Browser(val type: BrowserType = BrowserType.CHROME, val url: String) {
    enum class BrowserType {
        CHROME,
        FIREFOX,
    }

    var screenshot: ByteArray? = null

    fun launch(): Document {
        Logger.info("Creating playwright...")
        val playwright = Playwright.create()

        Logger.config("Browser type: ${type.name}")
        Logger.info("Launching browser...")
        val browser = when (type) {
            BrowserType.CHROME -> playwright.chromium().launch()
            BrowserType.FIREFOX -> playwright.firefox().launch()
        }

        Logger.info("Creating context...")
        val context = browser.newContext()
        Logger.info("Creating page...")
        val page = context.newPage()
        Logger.config("URL: $url")
        Logger.info("Navigating...")
        page.navigate(url)
        Logger.info("Waiting for load...")
        page.waitForLoadState()
        val content = page.content()
        screenshot = page.screenshot(Page.ScreenshotOptions().setFullPage(true))
        Logger.info("Closing browser...")
        page.close()
        context.close()
        browser.close()
        playwright.close()

        Logger.info("Parsing content...")
        return Jsoup.parse(content)
    }
}