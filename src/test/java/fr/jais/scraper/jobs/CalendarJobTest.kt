package fr.jais.scraper.jobs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CalendarJobTest {
    private val calendarJob = CalendarJob()

    @Test
    fun getHashtag() {
        val hashtag1 = calendarJob.getHashtag("A Playthrough of a Certain Dude's VRMMO Life")
        assertEquals("APlaythroughOfACertainDudesVRMMOLife", hashtag1)
        val hashtag2 = calendarJob.getHashtag("B-Project Passion*Love Call")
        assertEquals("BProjectPassionLoveCall", hashtag2)
        val hashtag3 = calendarJob.getHashtag("Dead Mount Death Play Cour 2")
        assertEquals("DeadMountDeathPlay", hashtag3)
        val hashtag4 = calendarJob.getHashtag("I’m in Love with the Villainess")
        assertEquals("ImInLoveWithTheVillainess", hashtag4)
        val hashtag5 = calendarJob.getHashtag("Kawagoe Boys Sing")
        assertEquals("KawagoeBoysSing", hashtag5)
        val hashtag6 = calendarJob.getHashtag("Migi & Dali")
        assertEquals("MigiDali", hashtag6)
        val hashtag7 = calendarJob.getHashtag("Ron Kamonohashi: Deranged Detective")
        assertEquals("RonKamonohashiDerangedDetective", hashtag7)
        val hashtag8 = calendarJob.getHashtag("SHY")
        assertEquals("SHY", hashtag8)
        val hashtag9 = calendarJob.getHashtag("Stardust Telepath")
        assertEquals("StardustTelepath", hashtag9)
        val hashtag10 = calendarJob.getHashtag("The Demon Sword Master of Excalibur Academy")
        assertEquals("TheDemonSwordMasterOfExcaliburAcademy", hashtag10)
        val hashtag11 = calendarJob.getHashtag("HYPNOSISMIC -Division Rap Battle- Rhyme Anima PLUS")
        assertEquals("HYPNOSISMICRhymeAnimaPLUS", hashtag11)
        val hashtag12 = calendarJob.getHashtag("Dr. STONE NEW WORLD Cour 2")
        assertEquals("DrSTONENEWWORLD", hashtag12)
        val hashtag13 = calendarJob.getHashtag("The Eminence in Shadow - Saison 2")
        assertEquals("TheEminenceInShadow", hashtag13)
        val hashtag14 = calendarJob.getHashtag("16bit Sensation: Another Layer")
        assertEquals("16bitSensationAnotherLayer", hashtag14)
        val hashtag15 = calendarJob.getHashtag("Firefighter Daigo: Rescuer in Orange")
        assertEquals("FirefighterDaigo", hashtag15)
        val hashtag16 = calendarJob.getHashtag("Captain Tsubasa Saison 2 Junior Youth Arc")
        assertEquals("CaptainTsubasaJuniorYouthArc", hashtag16)
        val hashtag17 = calendarJob.getHashtag("Frieren: Beyond Journey’s End")
        assertEquals("Frieren", hashtag17)
        val hashtag18 = calendarJob.getHashtag("The Ancient Magus Bride saison 2 - Cour 2")
        assertEquals("TheAncientMagusBride", hashtag18)
        val hashtag19 = calendarJob.getHashtag("SPY × FAMILY Saison 2")
        assertEquals("SPYFAMILY", hashtag19)
    }
}