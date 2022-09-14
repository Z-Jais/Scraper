package fr.jais.scraper.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.utils.EpisodeType
import fr.jais.scraper.utils.LangType
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.expect

internal class AnimationDigitalNetworkPlatformTest {
    private val scraper = Scraper()
    private val country = FranceCountry()
    private val platform = AnimationDigitalNetworkPlatform(scraper)

    private fun testEpisode(): JsonObject? {
        val json =
            "{\"id\":19803,\"title\":\"Overlord IV - Épisode 11\",\"name\":\"Des pièges bien agencés\",\"number\":\"Épisode 11\",\"shortNumber\":\"11\",\"season\":\"4\",\"reference\":\"overlord_tv4_0011\",\"type\":\"EPS\",\"order\":11,\"image\":\"https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/eps11_320x180.jpg\",\"image2x\":\"https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/eps11_640x360.jpg\",\"summary\":\"Au palais royal de Re-Estize, le roi Ramposa III réalise avec dépit que le silence de son armée signifie sans doute la mort de son second fils. Nazarick est aux portes la capitale, et l'invasion commencera sous peu. S'il existe quelque part des combattants suffisemment puissants pour affronter Ainz Ooal Gown, c'est maintenant qu'ils doivent se montrer !\",\"releaseDate\":\"2022-09-13T14:00:00Z\",\"duration\":1420,\"url\":\"https://animationdigitalnetwork.fr/video/overlord-saison-4/19803-episode-11-des-pieges-bien-agences\",\"urlPath\":\"/video/overlord-saison-4/19803-episode-11-des-pieges-bien-agences\",\"embeddedUrl\":\"https://animationdigitalnetwork.fr/embedded/overlord-saison-4/19803\",\"languages\":[\"vostf\"],\"qualities\":[\"fhd\",\"hd\",\"sd\",\"mobile\"],\"rating\":5,\"ratingsCount\":80,\"commentsCount\":61,\"available\":true,\"download\":false,\"free\":false,\"freeWithAds\":false,\"show\":{\"id\":909,\"title\":\"Overlord IV\",\"type\":\"EPS\",\"originalTitle\":\" オーバーロードIV\",\"shortTitle\":\"Overlord IV\",\"reference\":\"overlord_tv4\",\"age\":\"12+\",\"languages\":[\"vostf\"],\"summary\":\"Ainz Ooal Gown, ayant assis sa domination, a pour projet de fonder un royaume où toutes les races pourraient cohabiter en harmonie. Cependant, cette montée en puissance est mal perçue par les autres dirigeants qui surveillent de près l’évolution de Nazarick. Ainz Ooal Gown parviendra-t-il à maintenir son autorité, en dépit des complots fomentés envers sa nation ?\",\"image\":\"https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/affiche_175x250.jpg\",\"image2x\":\"https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/affiche_350x500.jpg\",\"imageHorizontal\":\"https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/license_320x180.jpg\",\"imageHorizontal2x\":\"https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/license_640x360.jpg\",\"url\":\"https://animationdigitalnetwork.fr/video/overlord-saison-4\",\"urlPath\":\"/video/overlord-saison-4\",\"episodeCount\":11,\"genres\":[\"Dark-Fantasy\",\"Médieval-Fantastique\",\"Isekai\",\"Guerre\",\"Jeu\",\"Monde virtuel\",\"Politique\",\"Animation japonaise\",\"Action\",\"Aventure\",\"Seinen\"],\"copyright\":\"©Kugane Maruyama, PUBLISHED BY KADOWAWA CORPORATION/OVERLORD4PARTNERS\",\"rating\":4.9,\"ratingsCount\":1610,\"commentsCount\":61,\"qualities\":[\"fhd\",\"hd\",\"sd\"],\"simulcast\":true,\"free\":true,\"available\":true,\"download\":false,\"basedOn\":\"Light Novel\",\"tagline\":\"Retrouvez la suite des aventures de Ainz Ooal Gown dans cette quatrième saison !\",\"firstReleaseYear\":\"2022\",\"productionStudio\":\"Madhouse\",\"countryOfOrigin\":\"Japon\",\"productionTeam\":[{\"role\":\"Réalisateur\",\"name\":\"Naoyuki Ito\"},{\"role\":\"Auteur\",\"name\":\"Kugane Maruyama\"},{\"role\":\"Scénariste\",\"name\":\"Yukie Sugawara\"},{\"role\":\"Character designer original\",\"name\":\"so-bin\"},{\"role\":\"Character designer\",\"name\":\"Satoshi Tasaki\"}],\"nextVideoReleaseDate\":\"2022-09-20T14:00:00Z\"}}"
        return Gson().fromJson(json, JsonObject::class.java)
    }

    private fun testCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(2022, Calendar.SEPTEMBER, 13)
        return calendar
    }

    private fun testISOTimestamp(): String = "2022-09-13T14:00:00Z"

    @Test
    fun toISODate() {
        expect("2022-09-13") { platform.toISODate(testCalendar()) }
    }

    @Test
    fun fromISODate() {
        val isoTimestamp = platform.fromISOTimestamp(testISOTimestamp())
        expect(2022) { isoTimestamp?.get(Calendar.YEAR) }
        expect(Calendar.SEPTEMBER) { isoTimestamp?.get(Calendar.MONTH) }
        expect(13) { isoTimestamp?.get(Calendar.DAY_OF_MONTH) }
    }

    @Test
    fun getAPIContent() {
        expect(14) { platform.getAPIContent(country, testCalendar())?.size }
    }

    @Test
    fun convertAnime() {
        val anime = platform.convertAnime(country, testEpisode() ?: return)
        expect("Overlord IV") { anime?.name }
        expect("Ainz Ooal Gown, ayant assis sa domination, a pour projet de fonder un royaume où toutes les races pourraient cohabiter en harmonie. Cependant, cette montée en puissance est mal perçue par les autres dirigeants qui surveillent de près l’évolution de Nazarick. Ainz Ooal Gown parviendra-t-il à maintenir son autorité, en dépit des complots fomentés envers sa nation ?") { anime?.description }
        expect("https://image.animationdigitalnetwork.fr/license/overlord/tv4/web/affiche_350x500.jpg") { anime?.image }
    }

    @Test
    fun convertEpisode() {
        val episode = platform.convertEpisode(country, testEpisode() ?: return)
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