package fr.jais.scraper.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.Gson
import com.microsoft.playwright.Playwright
import java.net.http.HttpClient

object Const {
    val gson = Gson()
    val objectMapper = ObjectMapper()
    val xmlMapper = XmlMapper()
    val httpClient = HttpClient.newHttpClient()
    val playwright: Playwright = Playwright.create()
    val chromium = playwright.chromium()
    val firefox = playwright.firefox()
}
