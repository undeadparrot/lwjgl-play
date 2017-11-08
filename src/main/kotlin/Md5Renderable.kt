import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class Md5Renderable(val model:Md5Loader, val animator:Md5AnimLoader, val shaderProgram:ShaderProgram, val shaderProgramForShadows:ShaderProgram) : IRenderable {

    override fun renderNormals(debugger: LineDebugRenderable, frame: Int) {
        val joints = animator.getAllJointsForFrame(frame)
        for (mesh in model.meshes) {
            for (vert in animator.getOrderedVerticesWithNormalsFromTris(mesh, joints, model.bindposeJoints, frame)) {
                val pos = vert.pos
                val normal = vert.normal
                debugger.addLine(
                        pos,
                        Vector3f(pos).sub(Vector3f(normal).mul(0.2f))
                )
            }
        }
    }

    var va: Int =0
    var vb: Int =0
    private var vertices: FloatBuffer
    val floatsPerVert = 8

    init {

        val floatSize = 4
        va = GL30.glGenVertexArrays()
        glBindVertexArray(va)
        glUseProgram(shaderProgram.programId)
        //        val vertvalues = floatArrayOf(-1.0f, 0.0f, 0.0f, +1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
        vb = GL15.glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        val position = GL20.glGetAttribLocation(shaderProgram.programId, "position")
        val normal = GL20.glGetAttribLocation(shaderProgram.programId, "normal")
        val textureCoords = GL20.glGetAttribLocation(shaderProgram.programId, "uv")
        glEnableVertexAttribArray(position)
        glEnableVertexAttribArray(normal)
        glEnableVertexAttribArray(textureCoords)
        glVertexAttribPointer(position, 3, GL_FLOAT, false, floatsPerVert*floatSize, 0)
        glVertexAttribPointer(normal, 3, GL_FLOAT, true, floatsPerVert*floatSize, (3*floatSize).toLong())
        glVertexAttribPointer(textureCoords, 2, GL_FLOAT, false, floatsPerVert*floatSize, (3*2*floatSize).toLong())

        vertices = MemoryUtil.memAllocFloat(model.meshes.sumBy { it.numtris * 3 * floatsPerVert  } )

    }

    override fun render(camera: ICamera, textureManager: TextureManager, lamp: Lamp, frame: Int) {
        bindProgramAndUniforms(shaderProgram, camera, lamp)

        bufferData(frame)
        drawMeshes(textureManager)


    }

    override fun renderShadows(camera: ICamera, textureManager: TextureManager, frame: Int) {
        bindProgramAndUniforms(shaderProgramForShadows, camera,null )

        bufferData(frame)
        drawMeshes(textureManager=null)


    }

    private fun drawMeshes(textureManager: TextureManager?) {
        var vertsOffset = 0
        model.meshes.forEach { mesh ->
            if(textureManager!==null) {
                val textureName = mesh.shader
                GL13.glActiveTexture(GL13.GL_TEXTURE0)
                textureManager.bindByName(textureName)
            }
            val numVerts = mesh.numtris * 3
            glDrawArrays(GL_TRIANGLES, vertsOffset, numVerts)
            vertsOffset += numVerts
        }
    }

    private fun bufferData(frame: Int) {
        val joints = animator.getAllJointsForFrame(frame)
        //        val joints = model.bindposeJoints
        val vertvalues = model.meshes.flatMap { m ->
            animator.getOrderedVerticesWithNormalsFromTris(m, joints, bindposeJoints = model.bindposeJoints, frameIndex = frame).flatMap { v ->
                listOf(v.pos.x, v.pos.y, v.pos.z, v.normal.x, v.normal.y, v.normal.z, v.uv.x, v.uv.y)
            }
        }.toFloatArray()
        vertices.put(vertvalues).flip()
        glBindVertexArray(va)
        glBindBuffer(GL_ARRAY_BUFFER, vb)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
    }

    private fun bindProgramAndUniforms(program:ShaderProgram, camera:ICamera, lamp: Lamp?) {
        glUseProgram(program.programId)

        val perspectiveMatrix = camera.getProjectionMatrix()
        val viewMatrix =  camera.getViewMatrix()

        //this will be reused for each matrix
        val fb = BufferUtils.createFloatBuffer(16)

        val projectionMatrixUniform = glGetUniformLocation(program.programId, "projectionMatrix")
        Matrix4f().mul(perspectiveMatrix).mul(viewMatrix).get(fb)
        glUniformMatrix4fv(projectionMatrixUniform, false, fb)

        val textureUniform = glGetUniformLocation(program.programId, "texImage")
        glUniform1i(textureUniform, 0)//GL_TEXTURE0

        if (lamp != null) {

            val lightViewProjMatrixUniform = glGetUniformLocation(program.programId, "lightViewProjMatrix")
            Matrix4f().mul(lamp.getProjectionMatrix()).mul(lamp.getViewMatrix()).get(fb)
            glUniformMatrix4fv(lightViewProjMatrixUniform, false, fb)

            GL13.glActiveTexture(GL13.GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D,lamp.texture.textureId)
            val shadowTextureUniform = glGetUniformLocation(program.programId, "shadowImage")
            glUniform1i(shadowTextureUniform, 1)//GL_TEXTURE1
        }



    }

    override fun cleanup() {
        glBindVertexArray(0)
        glDeleteBuffers(vb)
        glDeleteVertexArrays(va)
    }



}
