package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.utils.Decoder
import fr.jais.scraper.utils.EpisodeType
import fr.jais.scraper.utils.LangType
import fr.jais.scraper.utils.Resource
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.expect

internal class AnimationDigitalNetworkPlatformTest {
    private val scraper = Scraper()
    private val country = FranceCountry()
    private val platform = AnimationDigitalNetworkPlatform(scraper)

    private fun testEpisode(): JsonObject? {
        val gzip = Resource.get("animation_digital_network.txt")
        return Gson().fromJson(Decoder.fromGzip(gzip!!), JsonObject::class.java)
    }

    private fun testCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(2022, Calendar.SEPTEMBER, 13, 23, 59, 0)
        return calendar
    }

    private fun testISOTimestamp(): String = "2022-09-13T14:00:00Z"

    @Test
    fun toISODate() {
        expect("2022-09-13") { platform.toISODate(testCalendar()) }
    }

    @Test
    fun fromISODate() {
        val isoTimestamp = platform.converter.fromISOTimestamp(testISOTimestamp())
        isoTimestamp?.timeZone = TimeZone.getTimeZone("UTC")
        expect(2022) { isoTimestamp?.get(Calendar.YEAR) }
        expect(Calendar.SEPTEMBER) { isoTimestamp?.get(Calendar.MONTH) }
        expect(13) { isoTimestamp?.get(Calendar.DAY_OF_MONTH) }
        expect(14) { isoTimestamp?.get(Calendar.HOUR_OF_DAY) }
        expect(0) { isoTimestamp?.get(Calendar.MINUTE) }
        expect(0) { isoTimestamp?.get(Calendar.SECOND) }
    }

    @Test
    fun getAPIContent() {
        expect(14) { platform.getAPIContent(country, testCalendar())?.size }
    }

    @Test
    fun convertAnime() {
        val anime = platform.converter.convertAnime(country, testEpisode() ?: return)
        expect("Overlord IV") { anime?.name }
        expect("Ainz Ooal Gown, ayant assis sa domination, a pour projet de fonder un royaume où toutes les races pourraient cohabiter en harmonie. Cependant, cette montée en puissance est mal perçue par les autres dirigeants qui surveillent de près l’évolution de Nazarick. Ainz Ooal Gown parviendra-t-il à maintenir son autorité, en dépit des complots fomentés envers sa nation ?") { anime?.description }
        expect("https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/affiche_350x500.jpg") { anime?.image }
    }

    @Test
    fun convertEpisode() {
        val episode = platform.converter.convertEpisode(country, testEpisode() ?: return)
        expect(4) { episode.season }
        expect(11) { episode.number }
        expect(EpisodeType.EPISODE) { episode.episodeType }
        expect(LangType.SUBTITLES) { episode.langType }
        expect("Des pièges bien agencés") { episode.title }
        expect("https://animationdigitalnetwork.fr/video/overlord-saison-4/19803-episode-11-des-pieges-bien-agences") { episode.url }
        expect("https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/eps11_640x360.jpg") { episode.image }
        expect(1420) { episode.duration }
    }

    @Test
    fun getEpisodes() {
        val episodes = platform.getEpisodes(testCalendar())
        expect(14) { episodes.size }
    }
}