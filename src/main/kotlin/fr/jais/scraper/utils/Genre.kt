package fr.jais.scraper.utils

enum class Genre(val identifiers: Array<String>) {
    ACTION(arrayOf("action")),
    ADVENTURE(arrayOf("adventure", "aventure")),
    COMEDY(arrayOf("comedy", "comédie")),
    CYBERPUNK(arrayOf("cyberpunk")),
    DEMONS(arrayOf("demons", "démon", "démons")),
    DETECTIVE(arrayOf("detective", "détective")),
    DRAMA(arrayOf("drama", "drame")),
    ECCHI(arrayOf("ecchi", "fan service", "fanservice", "fan-service", "fan_service")),
    FANTASY(arrayOf("fantasy", "fantaisie", "fantastique")),
    GAME(arrayOf("game", "jeu")),
    GASTRONOMY(arrayOf("gastronomy", "gastronomie", "cooking", "cuisine")),
    HAREM(arrayOf("harem")),
    HENTAI(arrayOf("hentai")),
    HEROIC_FANTASY(arrayOf("heroic_fantasy", "heroic fantasy", "heroic-fantasy")),
    HISTORICAL(arrayOf("historical", "historique", "histoire")),
    HORROR(arrayOf("horror", "horreur")),
    HUMOR(arrayOf("humor", "humour")),
    IDOLS(arrayOf("idols", "idoles", "idole", "idol")),
    ISEKAI(arrayOf("isekai")),
    JOSEI(arrayOf("josei", "jôsei")),
    KIDS(arrayOf("kids", "enfants", "enfant")),
    MAGIC(arrayOf("magic", "magie")),
    MAGICAL_GIRL(arrayOf("magical_girl", "magical girl", "magical-girl", "magical_girls", "magical girls", "magical-girls")),
    MARTIAL_ARTS(arrayOf("martial_arts", "martial arts", "martial-arts", "arts martiaux", "art martiaux", "art martial")),
    MECHA(arrayOf("mecha")),
    MILITARY(arrayOf("military", "militaire")),
    MUSIC(arrayOf("music", "musique")),
    MYSTERY(arrayOf("mystery", "mystère", "mystere")),
    NOSTALGIA(arrayOf("nostalgia", "nostalgie", "nostalgic", "nostalgique")),
    PARODY(arrayOf("parody", "parodie")),
    PIRATES(arrayOf("pirates", "pirate")),
    POLICE(arrayOf("police", "policier")),
    POST_APOCALYPTIC(arrayOf("post_apocalyptic", "post apocalyptic", "post-apocalyptic", "post-apo", "post apo", "post-apo", "post_apocalyptique", "post apocalyptique", "post-apocalyptique")),
    PSYCHOLOGY(arrayOf("psychology", "psychological", "psychologique", "psychologiques")),
    REVERSE_HAREM(arrayOf("reverse_harem", "reverse harem", "reverse-harem")),
    ROMANCE(arrayOf("romance", "romantique")),
    SCHOOL(arrayOf("school", "école", "ecole", "scolaire", "vie scolaire", "animation japonaise sur l'école")),
    SCI_FI(arrayOf("sci_fi", "sci fi", "sci-fi", "science fiction", "science-fiction", "science_fiction", "science-fi")),
    SEINEN(arrayOf("seinen")),
    SHOUJO(arrayOf("shojo", "shoujo", "shôjo")),
    SHOUNEN(arrayOf("shonen", "shounen", "shônen")),
    SHOUJO_AI(arrayOf("shoujo_ai", "shoujo ai", "shoujo-ai", "shôjo ai", "shôjo-ai")),
    SHOUNEN_AI(arrayOf("shounen_ai", "shounen ai", "shounen-ai", "shônen ai", "shônen-ai")),
    SLICE_OF_LIFE(arrayOf("slice_of_life", "slice of life", "slice-of-life", "tranche de vie", "tranches de vie")),
    SPACE(arrayOf("space", "espace")),
    SPORTS(arrayOf("sports", "sport")),
    STEAMPUNK(arrayOf("steampunk")),
    SUPERNATURAL(arrayOf("supernatural", "surnaturel")),
    SUPER_POWER(arrayOf("super_power", "super power", "super-power", "super-powers", "super powers", "super-powers", "super-pouvoirs", "super pouvoirs", "super-pouvoir", "super pouvoir")),
    THRILLER(arrayOf("thriller")),
    TRAGEDY(arrayOf("tragedy", "tragédie", "tragedie")),
    VAMPIRE(arrayOf("vampire")),
    VIOLENCE(arrayOf("violence")),
    WAR(arrayOf("war", "guerre")),
    YAOI(arrayOf("yaoi")),
    YURI(arrayOf("yuri")),
    ;

    companion object {
        fun fromString(genre: String): Genre? {
            return values().firstOrNull { genre.lowercase() in it.identifiers }
        }

        fun fromArray(genres: List<String>): List<Genre> {
            return genres.mapNotNull { fromString(it) }.distinct()
        }
    }
}