package fr.jais.scraper.insta

import com.google.gson.Gson
import com.google.gson.JsonArray
import java.awt.*
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

private fun BufferedImage.roundedCorners(radius: Int): BufferedImage {
    val w = width
    val h = height
    val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

    val g2 = output.createGraphics()
    g2.composite = AlphaComposite.Src
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.color = Color.WHITE
    g2.fill(RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), radius.toFloat(), radius.toFloat()))
    g2.composite = AlphaComposite.SrcAtop
    g2.drawImage(this, 0, 0, null)
    g2.dispose()

    return output
}

fun BufferedImage.opacity(opacity: Float): BufferedImage {
    val w = width
    val h = height
    val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

    val g2 = output.createGraphics()
    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
    g2.drawImage(this, 0, 0, null)
    g2.dispose()

    return output
}

fun BufferedImage.resize(width: Int, height: Int): BufferedImage {
    val tmp = getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH)
    val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2 = resized.createGraphics()
    g2.drawImage(tmp, 0, 0, null)
    g2.dispose()
    return resized
}

fun main() {
    val response = URL("https://beta-api.ziedelth.fr/episodes/country/fr/page/1/limit/9").readText()
    val jsonArray = Gson().fromJson(response, JsonArray::class.java)

    jsonArray.forEachIndexed { index, jsonElement ->
        val jObject = jsonElement.asJsonObject

        //    val animeName = "Reborn as a Vending Machine, I Now Wander the Dungeon"
        val animeName = jObject["anime"].asJsonObject["name"].asString
//    val animeImage = "https://www.crunchyroll.com/imgsrv/display/thumbnail/480x720/catalog/crunchyroll/1dde6005c282ca55ea49c3e68cb81882.jpe"
        val animeImage = jObject["anime"].asJsonObject["image"].asString
        val description = "L'épisode ${jObject["number"]} est enfin disponible, ne ratez pas ça ! Bon visionnage à tous"

        val backgroundImage = ImageIO.read(URL("https://cdn.discordapp.com/attachments/1093774447636385883/1095284174883147877/Ziedelth_solo_1girl_adult_beautiful_shy_yellow_hair_smooth_hair_fd121b3f-3739-4dbe-b1d3-fec13fff64fd.png")).opacity(0.1f)

        // Create image by 1080x1350 size
        val image = BufferedImage(1080, 1350, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw background
        graphics.color = Color(0xF6F6F6)
        graphics.fillRect(0, 0, image.width, image.height)

        graphics.color = Color(0xffa500)
        val margin = 0
        val border = 10
        graphics.fillRoundRect(margin, margin, image.width - margin * 2, image.height - margin * 2, 50, 50)
        graphics.color = Color.WHITE
        graphics.fillRoundRect(margin + border, margin + border, image.width - margin * 2 - border * 2, image.height - margin * 2 - border * 2, 50, 50)
        graphics.drawImage(backgroundImage.resize(image.width - margin * 2 - border * 2, image.height - margin * 2 - border * 2), margin + border, margin + border, null)

        // Draw anime image at the center
        val animeImageB = ImageIO.read(URL(animeImage)).resize(480, 720)
        val x = (image.width - animeImageB.width) / 2
        val y = ((image.height - animeImageB.height) / 1.35).toInt()
        // Draw rounded image
        graphics.color = Color.WHITE
        graphics.fillRoundRect(x - 10, y - 10, animeImageB.width + 20, animeImageB.height + 20, 50, 50)
        graphics.drawImage(animeImageB.roundedCorners(50), x, y, null)

        // Draw description top center
        graphics.color = Color(0xffa500)
        graphics.font = Font(graphics.font.name, Font.BOLD, 60)
        drawTruncateString(animeName, graphics, image, 125, 70)

        // Draw description bottom center
        graphics.color = Color.BLACK
        graphics.font = Font(graphics.font.name, Font.PLAIN, 45)
        drawTruncateString(description, graphics, image, 350)

        val date = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE).format(Date())
        // Print date bottom right with 20px margin
        graphics.drawString(date, image.width - graphics.fontMetrics.stringWidth(date) - 50, image.height - 50)

        graphics.dispose()
        ImageIO.write(image, "png", File("image-$index.png"))
    }
}

private fun drawTruncateString(string: String, graphics: Graphics2D, image: BufferedImage, y: Int, lineSpacing: Int = 50) {
    val lines = mutableListOf<String>()
    var line = ""

    string.trim().split(" ").forEach {
        if (graphics.fontMetrics.stringWidth(line + it) > image.width - 100) {
            lines.add(line)
            line = ""
        }
        line += "$it "
    }

    lines.add(line)

    lines.forEachIndexed { index, s ->
        graphics.drawString(s, (image.width - graphics.fontMetrics.stringWidth(s)) / 2, y + index * lineSpacing)
    }
}