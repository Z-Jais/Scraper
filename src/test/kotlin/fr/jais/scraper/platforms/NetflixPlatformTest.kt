package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.utils.Decoder
import fr.jais.scraper.utils.Resource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class NetflixPlatformTest {
    private val scraper = Scraper()
    private val country = FranceCountry()
    private val platform = NetflixPlatform(scraper)

    private fun testEpisode(): Document? {
        // Get netflix.txt from test resources
        val gzip = Resource.get("netflix.txt")
        return Jsoup.parse(Decoder.fromGzip(gzip!!))
    }

    @Test
    fun convertToNetflixEpisodes() {
        val episodes = platform.convertToNetflixEpisodes(testEpisode()!!)
        expect(24) { episodes.size }
    }
}