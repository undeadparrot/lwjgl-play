import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30
import java.nio.ByteBuffer

class Lamp:ICamera {
    val width: Int = 1024
    val height: Int = 1024
    val texture: Texture
    val framebufferId: Int

    init {
        val textureId = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)

        var nullBuffer: ByteBuffer? = null
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, nullBuffer)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        texture = Texture(textureId = textureId, filename="SHADOW_TEXTURE",size= Vector2f(width.toFloat(), height.toFloat()))

        framebufferId = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId)

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, texture.textureId, 0)
        GL11.glDrawBuffer(GL11.GL_NONE)
        GL11.glReadBuffer(GL11.GL_NONE)

        val status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)
        if(status != GL30.GL_FRAMEBUFFER_COMPLETE){
            throw Exception("Could not create framebuffer for shadow map, status: $status")
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, GL11.GL_NONE)

    }
    override fun getViewMatrix(): Matrix4f {

        return Matrix4f()
                .scale(0.2F)
                .mul(Matrix4f().lookAt(0f,5f,5f,0f,0f,0f,0f,1f,0f))
    }
    override fun getProjectionMatrix(): Matrix4f {

        val aspectRatio = (width / height).toFloat()
        return Matrix4f().ortho(-1f, 1f, 1f, -1f,0.01f, 10.0f)

    }
    fun renderPass(textureManager: TextureManager,renderables:Iterable<IRenderable>,frame:Int=0 ){
        GL11.glViewport(0,0,width,height)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId)
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        renderables.forEach { x -> x.renderShadows(this, textureManager,frame=frame) }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, GL11.GL_NONE)
    }

}