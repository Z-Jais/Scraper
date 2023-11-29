package fr.jais.scraper.bugs

import fr.jais.scraper.Scraper
import fr.jais.scraper.countries.FranceCountry
import fr.jais.scraper.platforms.CrunchyrollPlatform
import org.junit.jupiter.api.Test

internal class CrunchyrollEpisodeWithNoImagePreviewIsNotScrap {
    private val scraper = Scraper()
    private val country = FranceCountry()
    private val platform = CrunchyrollPlatform(scraper)

    @Test
    fun test() {
        val xmlEpisode = """
            <rss xmlns:media="http://search.yahoo.com/mrss/" xmlns:crunchyroll="http://www.crunchyroll.com/rss" version="2.0">
<channel>
<item>
	<title>Je me fais isekai pour la deuxième fois... Ça commence à faire beaucoup. - Épisode 12 - Combat final pour la deuxième fois</title>
	<link>http://www.crunchyroll.com/summoned-to-another-world-for-a-second-time/episode-12-fighting-the-last-battle-for-a-second-time-895522</link>
	<guid isPermalink="true">http://www.crunchyroll.com/media-895522</guid>
	<description><img src="https://www.crunchyroll.com/i/coming_soon_beta_thumb.jpg" /><br />Tôma est à présent tout seul, mais ses pouvoirs sont extraordinaires, et Setsu et ses alliés vont devoir tout donner pour l'affronter.</description>
	<category>Anime</category>
	<media:category scheme="http://gdata.youtube.com/schemas/2007/categories.cat" label="Anime">Movies_Anime_animation</media:category>
	<crunchyroll:mediaId>895522</crunchyroll:mediaId>
	<pubDate>Sat, 24 Jun 2023 18:45:00 GMT</pubDate>
	<crunchyroll:freePubDate>Tue, 19 Jan 2038 00:27:28 GMT</crunchyroll:freePubDate>
	<crunchyroll:premiumPubDate>Sat, 24 Jun 2023 18:45:00 GMT</crunchyroll:premiumPubDate>
	<crunchyroll:endPubDate>Mon, 30 Nov 9998 08:00:00 GMT</crunchyroll:endPubDate>
	<crunchyroll:premiumEndPubDate>Mon, 30 Nov 9998 08:00:00 GMT</crunchyroll:premiumEndPubDate>
	<crunchyroll:freeEndPubDate>Mon, 30 Nov 9998 08:00:00 GMT</crunchyroll:freeEndPubDate>
	<crunchyroll:seriesTitle>Je me fais isekai pour la deuxième fois... Ça commence à faire beaucoup.</crunchyroll:seriesTitle>
	<crunchyroll:episodeTitle>Combat final pour la deuxième fois</crunchyroll:episodeTitle>
	<crunchyroll:episodeNumber>12</crunchyroll:episodeNumber>
	<crunchyroll:duration>1411</crunchyroll:duration>
	<crunchyroll:publisher>ABC Asahi</crunchyroll:publisher>
	<crunchyroll:subtitleLanguages>en - us,es - la,es - es,fr - fr,pt - br,it - it,de - de,ru - ru</crunchyroll:subtitleLanguages>
	<media:content type="video/mp4" medium="video" duration="1411"/>
	<media:restriction relationship="allow" type="country">ax al dz as ad ao ai aq ag ar am aw au at az bs bh bd bb by be bz bj bm bt bo bq ba bw bv br io bg bf bi cm ca cv ky cf td cl cx cc co km cg cd ck cr ci hr cu cw cy cz dk dj dm do ec eg sv gq er ee et fk fo fi fr gf tf ga gm ge de gh gi gr gl gd gp gu gt gg gn gw gy ht hm va hn hu is in ir iq ie im il it jm je jo ke ki kw lv lb ls lr ly li lt lu mk mg mw mv ml mt mh mq mr mu yt mx fm md mc me ms ma mz na np nl an nc nz ni ne ng nu nf mp no om pk ps pa py pe pn pl pt pr qa re ro ru rw bl sh kn lc mf pm vc sm st sa sn rs sc sl sx sk si so za gs ss es lk sd sr sj sz se ch sy tj tz tg tk tt tn tr tm tc ug ua ae gb us um uy uz ve vg vi wf eh ye zm zw</media:restriction>
	<media:credit role="distribution company">Crunchyroll LLC</media:credit>
	<media:rating scheme="urn:simple">nonadult</media:rating>
	<media:keywords>summoned to another world for a second time, fantasy, action, comedy, fantasy, action, comedy</media:keywords>
	<crunchyroll:modifiedDate>Sun, 25 Jun 2023 01:44:32 GMT</crunchyroll:modifiedDate>
</item>
  <item>
   <title>Fire Force (Telugu Dub) - Épisode 6 - La Promesse de Hibana</title>
   <link>http://www.crunchyroll.com/fire-force/episode-6-the-spark-of-promise-915324</link>
   <guid isPermalink="true">http://www.crunchyroll.com/media-915324</guid>
   <description>&lt;img src=&quot;https://img1.ak.crunchyroll.com/i/spire2-tmb/80a644e1c5fcc69691e36b815d9023431565945523_thumb.jpg&quot;  /&gt;&lt;br /&gt;Shinra affronte la princesse Hibana, capitaine de la 5e brigade de la Fire Force.</description>
   <enclosure url="https://img1.ak.crunchyroll.com/i/spire2-tmb/80a644e1c5fcc69691e36b815d9023431565945523_thumb.jpg" type="image/jpeg" length="4941"/>
   <category>Anime</category>
   <media:category scheme="http://gdata.youtube.com/schemas/2007/categories.cat" label="Anime">Movies_Anime_animation</media:category>
   <crunchyroll:mediaId>915324</crunchyroll:mediaId>
   <pubDate>Wed, 29 Nov 2023 06:30:00 GMT</pubDate>
   <crunchyroll:freePubDate>Tue, 19 Jan 2038 00:27:28 GMT</crunchyroll:freePubDate>
   <crunchyroll:premiumPubDate>Wed, 29 Nov 2023 06:30:00 GMT</crunchyroll:premiumPubDate>
   <crunchyroll:endPubDate>Mon, 30 Nov 9998 08:00:00 GMT</crunchyroll:endPubDate>
   <crunchyroll:premiumEndPubDate>Mon, 30 Nov 9998 08:00:00 GMT</crunchyroll:premiumEndPubDate>
   <crunchyroll:freeEndPubDate>Mon, 30 Nov 9998 08:00:00 GMT</crunchyroll:freeEndPubDate>
   <crunchyroll:seriesTitle>Fire Force</crunchyroll:seriesTitle>
   <crunchyroll:episodeTitle>La Promesse de Hibana</crunchyroll:episodeTitle>
   <crunchyroll:episodeNumber>6</crunchyroll:episodeNumber>
   <crunchyroll:duration>1437</crunchyroll:duration>
   <crunchyroll:publisher>Kodansha Ltd.</crunchyroll:publisher>
   <media:content type="video/mp4" medium="video" duration="1437"/>
   <media:restriction relationship="allow" type="country">af ax al dz as ad ao ai aq ag ar am aw au at az bs bh bd bb by be bz bj bm bt bo bq ba bw bv br io bn bg bf bi kh cm ca cv ky cf td cl cx cc co km cg cd ck cr ci hr cu cw cy cz dk dj dm do tp ec eg sv gq er ee et fk fo fj fi fr gf pf tf ga gm ge de gh gi gr gl gd gp gu gt gg gn gw gy ht hm va hn hu is in id ir iq ie im il it jm je jo kz ke ki kp kr kw kg la lv lb ls lr ly li lt lu mk mg mw mv ml mt mh mq mr mu yt mx fm md mc mn me ms ma mz mm na nr np nl an nc nz ni ne ng nu nf mp no om pk pw ps pa pg py pe ph pn pl pt pr qa re ro ru rw bl sh kn lc mf pm vc ws sm st sa sn rs sc sl sx sk si sb so za gs ss es lk sd sr sj sz se ch sy tj tz th tl tg tk to tt tn tr tm tc tv ug ua ae gb us um uy uz vu ve vn vg vi wf eh ye zm zw</media:restriction>
   <media:credit role="distribution company">Crunchyroll LLC</media:credit>
   <media:rating scheme="urn:simple">nonadult</media:rating>
   <media:thumbnail url="https://img1.ak.crunchyroll.com/i/spire2-tmb/80a644e1c5fcc69691e36b815d9023431565945523_full.jpg" width="640" height="360"/>
   <media:thumbnail url="https://img1.ak.crunchyroll.com/i/spire2-tmb/80a644e1c5fcc69691e36b815d9023431565945523_large.jpg" width="200" height="112"/>
   <media:thumbnail url="https://img1.ak.crunchyroll.com/i/spire2-tmb/80a644e1c5fcc69691e36b815d9023431565945523_thumb.jpg" width="160" height="90"/>
   <media:thumbnail url="https://img1.ak.crunchyroll.com/i/spire2-tmb/80a644e1c5fcc69691e36b815d9023431565945523_medium.jpg" width="100" height="56"/>
   <media:thumbnail url="https://img1.ak.crunchyroll.com/i/spire2-tmb/80a644e1c5fcc69691e36b815d9023431565945523_small.jpg" width="50" height="28"/>
   <media:keywords>action, shonen, supernatural, atsushi Ōkubo, manga, fire fighting, fantasy, 2020, jump, sequel, enen no shouboutai: ni no shou, deutsche synchro</media:keywords>
   <crunchyroll:modifiedDate>Wed, 29 Nov 2023 08:39:36 GMT</crunchyroll:modifiedDate>
  </item>
</channel>
</rss>
        """.trimIndent()

        platform.simulcasts[country] =
            mutableSetOf("Je me fais isekai pour la deuxième fois... Ça commence à faire beaucoup.".lowercase())
        val jsonEpisode = platform.xmlToJson(xmlEpisode)
        println(jsonEpisode.get(0).asJsonObject)
        println(jsonEpisode.get(1).asJsonObject)
        platform.converter.convertEpisode(country, jsonEpisode.get(0).asJsonObject, emptyList())
    }
}