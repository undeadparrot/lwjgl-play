import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class TextDebugRenderable : IRenderable {
    override fun renderShadows(camera: ICamera, textureManager: TextureManager, frame: Int) {}

    override fun renderNormals(debugger: LineDebugRenderable, frame: Int) {}

    val va: Int
    val vb: Int
    val shaderProgram: ShaderProgram
    val matrixBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16)
    val data = mutableListOf<Float>()
    val dataMalloc: FloatBuffer

    private val max_size: Int = 50000

    private val font: Font

    constructor() {

        this.shaderProgram = ShaderProgram.fromFilename("debugFont.v.glsl", "debugFont.f.glsl")

        this.va = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(this.va)
        this.vb = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vb)

        val position = GL20.glGetAttribLocation(shaderProgram.programId, "position")
        if (position < 0) {
            throw Exception("Failed to get position attribute for debug shader")
        }
        GL20.glEnableVertexAttribArray(position)
        GL20.glVertexAttribPointer(position, 3, GL11.GL_FLOAT, false, 4 * 5, 0)

        val uv = GL20.glGetAttribLocation(shaderProgram.programId, "uv")
        if (uv < 0) {
            throw Exception("Failed to get uv attribute for debug shader")
        }
        GL20.glEnableVertexAttribArray(uv)
        GL20.glVertexAttribPointer(uv, 2, GL11.GL_FLOAT, false, 4 * 5, 4 * 3)

        dataMalloc = MemoryUtil.memAllocFloat(max_size)

        font = Font.loadFromBmFontFile("distanceFont.fnt")


    }

    override fun cleanup() {
        GL30.glBindVertexArray(0)
        GL15.glDeleteBuffers(vb)
        GL30.glDeleteVertexArrays(va)
    }

    val COL_RED = (0xFF0101).toFloat()
    val COL_GREEN = (0x01FF01).toFloat()
    val COL_BLUE = (0x0101FF).toFloat()
    fun addLine(text: String, x: Float, y: Float, z: Float) {
        data.addAll(this.font.stringToVertsP3T2(text, x, y, z))
    }

    override fun render(camera: ICamera, textureManager: TextureManager, lamp: Lamp, frame: Int) {

        ARBVertexArrayObject.glBindVertexArray(va)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vb)
        GL20.glUseProgram(shaderProgram.programId)
        val textureUniform = GL20.glGetUniformLocation(shaderProgram.programId, "texImage")
        GL20.glUniform1i(textureUniform, 0)//GL_TEXTURE0
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        if (textureManager !== null) {
            val textureName = font.image
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            textureManager.bindByName(textureName)
        }

        val projectionMatrixUniform = GL20.glGetUniformLocation(shaderProgram.programId, "projectionMatrix")
        Matrix4f().mul(camera.getProjectionMatrix()).mul(camera.getViewMatrix()).get(matrixBuffer)
        GL20.glUniformMatrix4fv(projectionMatrixUniform, false, matrixBuffer)

        bufferData()
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, data.size / 5)//xyzuv

        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun bufferData() {
        ARBVertexArrayObject.glBindVertexArray(va)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vb)
        dataMalloc.put(data.toFloatArray(), 0, data.size).flip()
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataMalloc, GL15.GL_STATIC_DRAW)
    }

    fun clear() {
        data.clear()
    }
}