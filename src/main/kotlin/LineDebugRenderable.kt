import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer


class LineDebugRenderable:IRenderable {
    override fun renderShadows(camera:ICamera, textureManager: TextureManager, frame: Int) {

    }

    override fun renderNormals(debugger: LineDebugRenderable, frame: Int) {}

    val va : Int
    val vb : Int
    val shaderProgram : ShaderProgram
    val matrixBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16)
    val data = mutableListOf<Float>()
    val dataMalloc: FloatBuffer

    private val max_size: Int = 50000

    constructor() {

        this.shaderProgram = ShaderProgram.fromFilename("debug.v.glsl", "debug.f.glsl")

        this.va = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(this.va)
        this.vb = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,vb)

        val position = GL20.glGetAttribLocation(shaderProgram.programId,"position")
        if(position < 0){throw Exception("Failed to get position attribute for debug shader")}
        GL20.glEnableVertexAttribArray(position)
        glVertexAttribPointer(position, 3, GL_FLOAT, false, 4 * 4, 0)

        val colour = GL20.glGetAttribLocation(shaderProgram.programId, "colour")
        if (colour < 0) {
            throw Exception("Failed to get colour attribute for debug shader")
        }
        GL20.glEnableVertexAttribArray(colour)
        glVertexAttribPointer(colour, 1, GL_FLOAT, false, 4 * 4, 4 * 3)

        dataMalloc = MemoryUtil.memAllocFloat(max_size)

    }
    override fun cleanup() {
        GL30.glBindVertexArray(0)
        GL15.glDeleteBuffers(vb)
        GL30.glDeleteVertexArrays(va)
    }

    val COL_RED = (0xFF0101).toFloat()
    val COL_GREEN = (0x01FF01).toFloat()
    val COL_BLUE = (0x0101FF).toFloat()
    fun addLine(a: Vector3f, b: Vector3f, colour: Float = COL_RED) {
        data.add(a.x)
        data.add(a.y)
        data.add(a.z)
        data.add(colour)
        data.add(b.x)
        data.add(b.y)
        data.add(b.z)
        data.add(colour)
    }

    override fun render(camera: ICamera, textureManager: TextureManager, lamp: Lamp, frame: Int) {

        glBindVertexArray(va)
        glBindBuffer(GL_ARRAY_BUFFER,vb)
        glUseProgram(shaderProgram.programId)

        val projectionMatrixUniform = GL20.glGetUniformLocation(shaderProgram.programId, "projectionMatrix")
        Matrix4f().mul(camera.getProjectionMatrix()).mul(camera.getViewMatrix()).get(matrixBuffer)
        glUniformMatrix4fv(projectionMatrixUniform,false,matrixBuffer)

        bufferData()
        glDrawArrays(GL_LINES, 0, data.size / 4)//xyzc
    }

    private fun bufferData() {
        glBindVertexArray(va)
        glBindBuffer(GL_ARRAY_BUFFER,vb)
        dataMalloc.put(data.toFloatArray(),0,data.size).flip()
        glBufferData(GL_ARRAY_BUFFER, dataMalloc, GL_STATIC_DRAW)
    }

    fun clear() {
        data.clear()
    }
}