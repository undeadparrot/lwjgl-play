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
    override fun renderNormals(debugger: LineDebugRenderable) {}

    val va : Int
    val vb : Int
    val shaderProgram : ShaderProgram
    val matrixBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16)
    val data = mutableListOf<Float>()
    constructor() {

        this.shaderProgram = ShaderProgram.fromFilename("basic.v.glsl", "debug.f.glsl")

        this.va = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(this.va)
        this.vb = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,vb)

        val position = GL20.glGetAttribLocation(shaderProgram.program,"position")
        if(position < 0){throw Exception("Failed to get position attribute for debug shader")}
        GL20.glEnableVertexAttribArray(position)
        glVertexAttribPointer(position,3,GL_FLOAT,false,0,0)

    }
    override fun cleanup() {
        GL30.glBindVertexArray(0)
        GL15.glDeleteBuffers(vb)
        GL30.glDeleteVertexArrays(va)
    }
    fun addLine(a:Vector3f,b:Vector3f){
        data.add(a.x)
        data.add(a.y)
        data.add(a.z)
        data.add(b.x)
        data.add(b.y)
        data.add(b.z)
    }

    override fun render(viewMatrix: Matrix4f, perspectiveMatrix: Matrix4f) {

        glBindVertexArray(va)
        glBindBuffer(GL_ARRAY_BUFFER,vb)
        glUseProgram(shaderProgram.program)

        val projectionMatrixUniform = GL20.glGetUniformLocation(shaderProgram.program, "projectionMatrix")
        Matrix4f().mul(perspectiveMatrix).mul(viewMatrix).get(matrixBuffer)
        glUniformMatrix4fv(projectionMatrixUniform,false,matrixBuffer)

        bufferData()
        glDrawArrays(GL_LINES,0,data.size/3)
    }

    private fun bufferData() {
        glBindVertexArray(va)
        glBindBuffer(GL_ARRAY_BUFFER,vb)
        val dataMalloc = MemoryUtil.memAllocFloat(data.size)
        dataMalloc.put(data.toFloatArray()).flip()
        glBufferData(GL_ARRAY_BUFFER, dataMalloc, GL_STATIC_DRAW)
    }

    fun clear() {
        data.clear()
    }
}