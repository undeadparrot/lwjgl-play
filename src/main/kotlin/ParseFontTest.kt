import com.google.gson.Gson
import java.io.File

data class Font(val face: String, val image: String, val scaleW: Int, val scaleH: Int, val chars: Map<Int, FontChar>) {
    fun stringToVertsP3T2(s: String, x: Float = 0f, y: Float = 0f, z: Float = 0f): List<Float> {
        var xOffset = 0f
        val scale = 0.02f
        return s.toCharArray().flatMap { letter ->
            val char = chars.getOrDefault(letter.toInt(), chars.values.first())
            val x1 = x + xOffset
            val x2 = x1 + char.width.toFloat() * scale
            val y1 = y + 0f
            val y2 = y1 + char.height.toFloat() * scale
            val u1 = char.x.toFloat() / scaleW
            val v1 = char.y.toFloat() / scaleH
            val u2 = u1 + char.width.toFloat() / scaleW
            val v2 = v1 + char.height.toFloat() / scaleH
            xOffset += char.xadvance.toFloat() * scale
            listOf(
                    x1, y2, z, u1, v1, //topleft
                    x2, y2, z, u2, v1, //topright
                    x2, y1, z, u2, v2, //botright
                    x2, y1, z, u2, v2, //botright
                    x1, y1, z, u1, v2, //botleft
                    x1, y2, z, u1, v1  //topleft

            )
        }
    }

    companion object {
        fun loadFromJsonFile(filename: String) = Gson().fromJson<Font>(File(filename).readText(), Font::class.java)
        fun loadFromBmFontFile(filename: String): Font {
            val patternCommon = Regex("" +
                    "common\\s+" +
                    ".*" +
                    "scaleW=(\\d+)\\s+" +
                    "scaleH=(\\d+)\\s+" +
                    ".*")
            val patternPage = Regex("" +
                    "page\\s+" +
                    ".*" +
                    "file=\"(.+)\"" +
                    ".*")
            val pattern = Regex("" +
                    "char\\s+" +
                    "id=(\\d+)\\s+" +
                    "x=(-?\\d+)\\s+" +
                    "y=(-?\\d+)\\s+" +
                    "width=(-?\\d+)\\s+" +
                    "height=(-?\\d+)\\s+" +
                    "xoffset=(-?\\d+)\\s+" +
                    "yoffset=(-?\\d+)\\s+" +
                    "xadvance=(-?\\d+)\\s+" +
                    ".*")
            var chars = mutableMapOf<Int, FontChar>()
            var scaleW = 0
            var scaleH = 0
            var file = ""
            File(filename).forEachLine { line ->
                when (line.substring(0, 4)) {
                    "comm" -> {

                        val match = patternCommon.matchEntire(line)
                        if (match !== null) {
                            val matches = match.groups
                            scaleW = matches[1]!!.value.toInt()
                            scaleH = matches[2]!!.value.toInt()
                        }
                    }
                    "page" -> {

                        val match = patternPage.matchEntire(line)
                        if (match !== null) {
                            val matches = match.groups
                            file = matches[1]!!.value
                        }
                    }
                    "char" -> {

                        val match = pattern.matchEntire(line)
                        if (match !== null) {
                            val matches = match.groups
                            val char = FontChar(
                                    id = matches[1]!!.value.toInt(),
                                    x = matches[2]!!.value.toInt(),
                                    y = matches[3]!!.value.toInt(),
                                    width = matches[4]!!.value.toInt(),
                                    height = matches[5]!!.value.toInt(),
                                    xoffset = matches[6]!!.value.toInt(),
                                    yoffset = matches[7]!!.value.toInt(),
                                    xadvance = matches[8]!!.value.toInt()
                            )
                            chars.set(char.id, char)
                        }
                    }
                }
            }
            return Font("Font", file, scaleW, scaleH, chars)
        }
    }
}

data class FontChar(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int, val xoffset: Int, val yoffset: Int, val xadvance: Int)

fun main(args: Array<String>) {
    var obj = Font.loadFromJsonFile("font1xml.json")
    Font.loadFromBmFontFile("distanceFont.fnt")
    obj.stringToVertsP3T2("Kotlin")
}

