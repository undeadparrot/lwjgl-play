import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.system.MemoryUtil

class Md5Renderable(val model:Md5Loader, val animator:Md5AnimLoader, val shaderProgram:ShaderProgram) : IRenderable {
    override fun renderNormals(debugger: LineDebugRenderable) {
        for (mesh in model.meshes) {
            for ((i, vert) in mesh.verts.withIndex()) {
                val pos = mesh.getVertexPosition(model.bindposeJoints, i)
                debugger.addLine(
                        pos,
                        Vector3f(pos).sub(Vector3f(vert.bindposeNormal).mul(0.5f))
                )
            }
        }
    }

    var va: Int =0
    var vb: Int =0
    init {


        va = GL30.glGenVertexArrays()
        glBindVertexArray(va)
        glUseProgram(shaderProgram.program)
        //        val vertvalues = floatArrayOf(-1.0f, 0.0f, 0.0f, +1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
        vb = GL15.glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        val position = GL20.glGetAttribLocation(shaderProgram.program, "position")
        glEnableVertexAttribArray(position)
        glVertexAttribPointer(position, 3, GL_FLOAT, false, 0, 0)

    }
    override fun render(viewMatrix:Matrix4f,perspectiveMatrix: Matrix4f){
        val projectionMatrixUniform = glGetUniformLocation(shaderProgram.program, "projectionMatrix")
        val fb = BufferUtils.createFloatBuffer(16)
        Matrix4f().mul(perspectiveMatrix).mul(viewMatrix).get(fb)


//        val joints = animator.getAllJointsForFrame(0)
        val joints = model.bindposeJoints
        val vertvalues = model.meshes.flatMap { m ->
            m.getOrderedVerticesFromTris(joints).flatMap { v3 ->
                listOf(v3.x, v3.y, v3.z)
            }
        }.toFloatArray()
        val vertices = MemoryUtil.memAllocFloat(vertvalues.size)
        vertices.put(vertvalues).flip()
        glUseProgram(shaderProgram.program)
        GL15.glBufferData(GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW)
        glUniformMatrix4fv(projectionMatrixUniform, false, fb)
        GL11.glDrawArrays(primitive, 0, vertvalues.size / 3)

    }

    override fun cleanup() {
        glBindVertexArray(0)
        glDeleteBuffers(vb)
        glDeleteVertexArrays(va)
    }



}
