import fr.jais.scraper.entities.Anime
import fr.jais.scraper.entities.Country
import fr.jais.scraper.entities.Episode
import fr.jais.scraper.entities.Platform
import fr.jais.scraper.utils.API
import fr.jais.scraper.utils.EpisodeType
import fr.jais.scraper.utils.LangType

fun main() {
    API.saveEpisodes(
        listOf(
            Episode(
                platform = Platform(
                    "Disney+",
                    "https://www.disneyplus.com/",
                    "https://jais.ziedelth.fr/attachments/platforms/disneyplus.png",
                ),
                anime = Anime(
                    country = Country("fr", "France"),
                    name = "Undead Unluck",
                    image = "https://jais.ziedelth.fr/attachments/undead-unluck-visuel.jpg",
                    description = "Andy, un Négateur doté de la capacité « Undead », cherche depuis longtemps une personne pouvant lui offrir une « vraie mort ». Fûko Izumo apporte le malheur à quiconque la touche en raison de sa capacité « Unluck ». Tous deux décident de rejoindre l'Union, une organisation qui a pour but de contrôler le monde et de le protéger des phénomènes inexpliqués. Ainsi ils explorent les mystères du monde tout en cherchant « la plus belle des morts qui soient ».",
                ),
                releaseDate = "2023-12-20T08:00:00Z",
                season = 1,
                number = 2,
                episodeType = EpisodeType.EPISODE,
                langType = LangType.SUBTITLES,
                id = 0,
                title = "L'Union",
                url = "https://www.disneyplus.com/fr-fr/video/d2ec3225-f1bf-4434-8037-54a2e80ff322",
                image = "https://jais.ziedelth.fr/attachments/undead_unluck_2.png",
                duration = 1440,
            ).apply {
                hash = "DISN-6yBbjezPBwNW-2-VOSTFR"
            }
        )
    )
}