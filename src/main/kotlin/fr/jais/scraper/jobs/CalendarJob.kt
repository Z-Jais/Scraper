package fr.jais.scraper.jobs

import com.mortennobel.imagescaling.ResampleOp
import fr.jais.scraper.utils.*
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import java.util.logging.Level
import javax.imageio.ImageIO

class CalendarJob : Job {
    private val maxEpisodesPerImage = 7

    override fun execute(p0: JobExecutionContext?) {
        Logger.info("Starting calendar job...")

        try {
            val episodes = getEpisodes()

            if (episodes.isEmpty()) {
                return
            }

            val folder = File("data/calendar")

            if (!folder.exists()) {
                Logger.config("Creating calendar folder...")
                folder.mkdirs()
            }

            Logger.config("Getting calendar font...")
            val font = File(folder, "Rubik.ttf")
            Logger.config("Getting calendar background image...")
            val backgroundImage =
                ImageIO.read(URL("https://cdn.discordapp.com/attachments/1093774447636385883/1095284174883147877/Ziedelth_solo_1girl_adult_beautiful_shy_yellow_hair_smooth_hair_fd121b3f-3739-4dbe-b1d3-fec13fff64fd.png"))
                    .opacity(0.1f)
            Logger.config("Getting calendar Crunchyroll image...")
            val crunchyrollImage = ImageIO.read(File(folder, "crunchyroll.png")).invert()
            Logger.config("Getting calendar ADN image...")
            val adnImage = ImageIO.read(File(folder, "animation_digital_network.png")).invert()
            Logger.config("Getting calendar Netflix image...")
            val netflixImage = ImageIO.read(File(folder, "netflix.png")).invert()

            val day = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRANCE).lowercase()
            val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"))

            var string: String
            var epochs = 0
            var take = 7

            Logger.info("Building text...")

            do {
                string = "🎯 | Votre planning #anime pour ce $day $date :\n"

                episodes.shuffled().take(take).forEach {
                    string += "\n#${
                        it.first.name.split(":", ",").first().capitalizeWords().onlyLettersAndDigits()
                    } EP${it.second.split(" ")[1]}"
                }

                string += """

Bonne journée ! 😊"""

                epochs++

                if (epochs % 10 == 0) {
                    take--
                    Logger.warning("$epochs has passed to attempting build the text, reducing take to $take")
                }
            } while (string.length > 250)

            Logger.info(string)

            val images = episodes.chunked(maxEpisodesPerImage).map { chunked ->
                imageToBase64(generateImage(font, chunked, backgroundImage, adnImage, crunchyrollImage, netflixImage))
            }

            API.saveCalendar(string, images)
        } catch (e: Exception) {
            Logger.log(Level.SEVERE, "Error with the calendar", e)
            return
        }

        Logger.info("The calendar is released!")
    }

    private fun imageToBase64(image: BufferedImage): String {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    private fun generateImage(
        font: File?,
        episodes: List<Pair<Anime, String>>,
        backgroundImage: BufferedImage,
        adnImage: BufferedImage,
        crunchyrollImage: BufferedImage,
        netflixImage: BufferedImage
    ): BufferedImage {
        val width = 600
        val height = 720
        val horizontalMargin = 50
        val animeHeight = 65
        val verticallyPadding = 20
        val horizontalPadding = 10

        val imageSize = animeHeight - verticallyPadding
        val xImage = horizontalMargin + horizontalPadding
        val xText = xImage + imageSize + horizontalPadding
        val textBoxWidth = width - xText - horizontalPadding - horizontalMargin

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = bufferedImage.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.font = Font.createFont(Font.TRUETYPE_FONT, font).deriveFont(20f)

        // Draw background
        graphics.color = Color(0xF6F6F6)
        graphics.fillRect(0, 0, width, height)

        graphics.color = Color(0xffa500)
        val margin = 0
        val border = 10
        graphics.fillRoundRect(margin, margin, width - margin * 2, height - margin * 2, 50, 50)
        graphics.color = Color.WHITE
        graphics.fillRoundRect(
            margin + border,
            margin + border,
            width - margin * 2 - border * 2,
            height - margin * 2 - border * 2,
            50,
            50
        )
        graphics.drawImage(
            backgroundImage.resize(width - margin * 2 - border * 2, height - margin * 2 - border * 2),
            margin + border,
            margin + border,
            null
        )

        graphics.color = Color.BLACK
        graphics.font = graphics.font.deriveFont(Font.PLAIN, 25f)

        val dayY = 25
        // Draw a rect start at horizontalMargin and with width of width - horizontalMargin * 2
        graphics.fillRoundRectShadow(horizontalMargin, dayY, width - horizontalMargin * 2, 50, 20, 20)
        // Draw an orange rect in the middle of the rect
        graphics.color = Color(0xffa500)
        graphics.fillRoundRectShadow(width / 2 - 30, dayY - 5, 60, 60, 10, 10)
        // Draw the current day in the middle of the orange circle
        graphics.color = Color.WHITE
        graphics.font = graphics.font.deriveFont(Font.BOLD, 30f)

        val currentDayString = Calendar.getInstance()[Calendar.DAY_OF_MONTH].toString()
        val textWidth = graphics.fontMetrics.stringWidth(currentDayString)
        graphics.drawString(currentDayString, (width - textWidth) / 2, dayY + 35)

        graphics.color = Color.WHITE
        graphics.font = graphics.font.deriveFont(Font.PLAIN, 20f)
        // Print the last 4 days (like 24, 23) at left of the orange circle and on the same line
        (4 downTo 1).forEach { dDay ->
            val day = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -dDay) }
            graphics.drawString(
                day[Calendar.DAY_OF_MONTH].toString(),
                horizontalMargin + 25 + (4 - dDay) * 50,
                dayY + 33
            )
        }
        // Print the next 4 days (like 26, 27) at right of the orange circle and on the same line
        (1..4).forEach { dDay ->
            val day = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, dDay) }
            graphics.drawString(
                day[Calendar.DAY_OF_MONTH].toString(),
                width - horizontalMargin - 50 - (4 - dDay) * 50,
                dayY + 33
            )
        }

        episodes.forEachIndexed { index, (anime, episode) ->
            val y = 70 + animeHeight + index * (animeHeight + verticallyPadding)

            graphics.color = Color(0xf2ebdc)
            graphics.fillRoundRectShadow(
                horizontalMargin,
                y - (animeHeight / 2),
                width - horizontalMargin * 2,
                animeHeight,
                10,
                10
            )

            if (anime.licences.size > 1) {
                // Split each licence image in 2
                // One half for the first licence half image
                // One half for the second licence half image
                val firstLicenceImageCut = adnImage.getSubimage(0, 0, adnImage.width / 2, adnImage.height)
                val secondLicenceImageCut =
                    crunchyrollImage.getSubimage(adnImage.width / 2, 0, adnImage.width / 2, adnImage.height)

                graphics.drawImage(
                    firstLicenceImageCut,
                    xImage,
                    y - (animeHeight / 2) + 10,
                    imageSize / 2,
                    imageSize,
                    null
                )
                graphics.drawImage(
                    secondLicenceImageCut,
                    xImage + (imageSize / 2) + 5,
                    y - (animeHeight / 2) + 10,
                    imageSize / 2,
                    imageSize,
                    null
                )
            } else {
                val image = if (anime.licences.contains("Animation Digital Network")) {
                    adnImage
                } else if (anime.licences.contains("Crunchyroll")) {
                    crunchyrollImage
                } else {
                    netflixImage
                }

                graphics.drawImage(
                    ResampleOp(imageSize, imageSize).filter(image, null),
                    xImage,
                    y - (animeHeight / 2) + 10,
                    imageSize,
                    imageSize,
                    null
                )
            }

            graphics.color = Color.BLACK
            graphics.font = graphics.font.deriveFont(Font.BOLD, 20f)

            // If the name is too long, we obfuscate it
            var textSize = anime.name.length

            while (graphics.fontMetrics.stringWidth(anime.name.substring(0, textSize) + "...") > textBoxWidth) {
                textSize--
            }

            if (textSize < anime.name.length) {
                graphics.drawString(anime.name.substring(0, textSize) + "...", xText, y)
            } else {
                graphics.drawString(anime.name, xText, y)
            }

            graphics.font = graphics.font.deriveFont(Font.PLAIN, 15f)

            val stringBuilder = StringBuilder(anime.licences.joinToString(", "))
            stringBuilder.append(" - ")

            if (anime.season > 1) {
                stringBuilder.append("Saison ${anime.season} - ")
            }

            stringBuilder.append(episode)
            graphics.drawString(stringBuilder.toString(), xText, y + 20)
        }

        return bufferedImage
    }

    private fun getEpisodes(): List<Pair<Anime, String>> {
        val content = Browser("${Const.calendarBaseUrl}/calendrier_diffusion.html").launch()

        val todayCalendar = content.select("table.calendrier_diffusion")
            .find { true == it.getElementsByTag("th").text().contains("Aujourd'hui", true) }
            ?: throw Exception("No anime today")

        val episodes = todayCalendar.getElementsByTag("td").mapNotNull {
            val animeElement = it.getElementsByTag("a") ?: return@mapNotNull null

            var name = animeElement.text().trim().replace(Const.multipleSpaceRegex, " ")
            val url = "${Const.calendarBaseUrl}${animeElement.attr("href")}"

            val season = if (name.contains("Saison", true)) {
                val number = name.split("Saison", ignoreCase = true)[1].trim().split(" ")[0].toInt()
                name =
                    name.replace("Saison $number", "", ignoreCase = true).trim().replace(Const.multipleSpaceRegex, " ")
                number
            } else {
                1
            }

            val episode = it.select(".calendrier_episode").text().trim().replace(Const.multipleSpaceRegex, " ")
            Anime(name, url, season) to episode
        }.filter { (anime, _) ->
            val subcontent = Browser(anime.url).launch()

            val infos = subcontent.select(".info_fiche > div")
            val licenceElement = infos.find { it.text().contains("Licence VOD", true) }

            if (licenceElement == null) {
                println("No licence for ${anime.name}")
                return@filter false
            }

            val licencePlatform =
                licenceElement.text().split(":")[1].trim().replace(Const.multipleSpaceRegex, " ").split(",")
                    .map { it.trim() }
            anime.licences.addAll(licencePlatform)
            licencePlatform.contains("Animation Digital Network") || licencePlatform.contains("Crunchyroll") || licencePlatform.contains(
                "Netflix"
            )
        }

        return episodes
    }
}

data class Anime(
    val name: String,
    val url: String,
    val season: Int = 1,
    val licences: MutableList<String> = mutableListOf(),
)
