package fr.jais.scraper.platforms

import fr.jais.scraper.Scraper
import org.junit.jupiter.api.Test

import kotlin.test.expect

internal class CrunchyrollPlatformTest {
    private val scraper = Scraper()
    private val platform = CrunchyrollPlatform(scraper)

    @Test
    fun getSimulcastCode() {
        expect(platform.getSimulcastCode("Printemps 2023")) { "spring-2023" }
        expect(platform.getSimulcastCode("Été 2023")) { "summer-2023" }
        expect(platform.getSimulcastCode("Automne 2023")) { "fall-2023" }
        expect(platform.getSimulcastCode("Hiver 2023")) { "winter-2023" }
        expect(platform.getSimulcastCode("Printemps 2024")) { "spring-2024" }
        expect(platform.getSimulcastCode("Été 2024")) { "summer-2024" }
        expect(platform.getSimulcastCode("Automne 2024")) { "fall-2024" }
        expect(platform.getSimulcastCode("Hiver 2024")) { "winter-2024" }
    }

    @Test
    fun getPreviousSimulcastCode() {
        expect(platform.getPreviousSimulcastCode("spring-2023")) { "winter-2023" }
        expect(platform.getPreviousSimulcastCode("winter-2023")) { "fall-2022" }
        expect(platform.getPreviousSimulcastCode("fall-2022")) { "summer-2022" }
        expect(platform.getPreviousSimulcastCode("summer-2022")) { "spring-2022" }
        expect(platform.getPreviousSimulcastCode("spring-2024")) { "winter-2024" }
        expect(platform.getPreviousSimulcastCode("winter-2024")) { "fall-2023" }
        expect(platform.getPreviousSimulcastCode("fall-2023")) { "summer-2023" }
        expect(platform.getPreviousSimulcastCode("summer-2023")) { "spring-2023" }
    }
}