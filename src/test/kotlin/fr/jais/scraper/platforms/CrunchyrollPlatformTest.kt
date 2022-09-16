package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.utils.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.expect

internal class CrunchyrollPlatformTest {
    private val scraper = Scraper()
    private val country = FranceCountry()
    private val platform = CrunchyrollPlatform(scraper)

    private fun testEpisode(): JsonObject? {
        val gzip = Resource.get("crunchyroll_episode.txt")
        return Gson().fromJson(Decoder.fromGzip(gzip!!), JsonObject::class.java)
    }

    private fun testXml() = Decoder.fromGzip(Resource.get("crunchyroll_xml.txt")!!)

    private fun testCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(2022, Calendar.SEPTEMBER, 13, 23, 59, 0)
        return calendar
    }

    private fun testISOTimestamp(): String = "Tue, 13 Sep 2022 18:00:00 GMT"

    @Test
    fun toISODate() {
        expect("2022-09-13") { platform.toISODate(testCalendar()) }
    }

    @Test
    fun fromISODate() {
        val isoTimestamp = CalendarConverter.fromGMTLine(testISOTimestamp())
        isoTimestamp?.timeZone = TimeZone.getTimeZone("UTC")

        expect(2022) { isoTimestamp?.get(Calendar.YEAR) }
        expect(Calendar.SEPTEMBER) { isoTimestamp?.get(Calendar.MONTH) }
        expect(13) { isoTimestamp?.get(Calendar.DAY_OF_MONTH) }
        expect(18) { isoTimestamp?.get(Calendar.HOUR_OF_DAY) }
        expect(0) { isoTimestamp?.get(Calendar.MINUTE) }
        expect(0) { isoTimestamp?.get(Calendar.SECOND) }

        expect("2022-09-13T18:00:00Z") { isoTimestamp?.toISO8601() }
    }

    @Test
    fun xmlToJson() {
        val json = platform.xmlToJson(testXml())
        expect(50) { json?.size }
    }

    @Test
    fun xmlToJsonWithFilter() {
        val json = platform.xmlToJsonWithFilter(country, testCalendar(), testXml())
        expect(1) { json?.size }
    }

    @Test
    fun convertAnime() {
        val anime = platform.converter.convertAnime(country, testEpisode() ?: return)
        expect("Dropkick on My Devil!") { anime.name }
        expect("Passionnée par les sciences occultes et vêtue façon lolita gothique, Yurine Hanazono a invoqué Jashin, une démone au corps de serpent venue tout droit des enfers. Depuis, elle doit cohabiter avec elle dans son appartement de Tokyo. Si Yurine venait à mourir, Jashin pourrait enfin retourner dans son monde. Aussi, cette dernière complote sans cesse pour se débarrasser de la jeune fille... Pendant ce temps débarque Pino, un ancien subordonné des anges Pekola et Poporon. Pekola est ravie de croire que Dieu a finalement envoyé quelqu'un pour la ramener chez elle...") { anime.description }
        expect("https://img1.ak.crunchyroll.com/i/spire1/53ae728bf97a3b0018f7e2e5a4a6a7961659221370_full.jpg") { anime.image }
    }

    @Test
    fun convertEpisode() {
        val episode = platform.converter.convertEpisode(country, testEpisode() ?: return)
        expect(1) { episode.season }
        expect(11) { episode.number }
        expect(EpisodeType.EPISODE) { episode.episodeType }
        expect(LangType.SUBTITLES) { episode.langType }
        expect("Le centre de don du sang de Jinbôchô !") { episode.title }
        expect("https://www.crunchyroll.com/fr/dropkick-on-my-devil/episode-11-the-jinbocho-blood-donation-center-852549") { episode.url }
        expect("https://img1.ak.crunchyroll.com/i/spire4-tmb/f62d016d4cc44025d616d55d958a84101663084376_full.jpg") { episode.image }
        expect(1419) { episode.duration }
    }
}