import de.matthiasmann.twl.utils.PNGDecoder
import org.joml.Vector2f
import org.lwjgl.BufferUtils.createByteBuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30.glGenerateMipmap
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.file.Files

class TextureManager {
    val store = mutableMapOf<String,Texture>()
    val default:Texture
    init {
        default = loadTextureFromFile("default.png")
    }
    fun bindByName(filename: String){
        val texture = store.getOrPut(filename,{
            try {
                loadTextureFromFile(filename)
            } catch (e: Exception) {
                println("Couldn't load ${filename}")
                default
            }
        })
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture.textureId)
    }
    fun loadTextureFromFile(filename: String): Texture {
        val fileStream: FileInputStream
        if(File(filename).exists()){
            println("Trying to load ${filename}")
            fileStream = FileInputStream(filename)
        }else if(File(filename+".png").exists()){
            println("Trying to load ${filename}.png")
            fileStream = FileInputStream(filename+".png")
        }
        else{
            throw Exception("Cannot find file ${filename}")
        }

        val decoder = PNGDecoder(fileStream)
        val width = decoder.width
        val height = decoder.height
        val bytebuf = createByteBuffer(width*height*4)
        decoder.decode(bytebuf,width*4, PNGDecoder.Format.RGBA)
        bytebuf.flip()

        val textureId = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bytebuf)
//        glGenerateMipmap(GL11.GL_TEXTURE_2D);
        val texture = Texture(textureId = textureId, filename=filename,size= Vector2f(width.toFloat(), height.toFloat()))
        store[filename] = texture
        return texture
    }

}

data class Texture(val textureId: Int, val filename: String, val size: Vector2f)
