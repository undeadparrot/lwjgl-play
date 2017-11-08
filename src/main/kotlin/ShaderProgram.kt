import org.lwjgl.opengl.GL20
import java.io.File

class ShaderProgram(val fragmentShader:Int, val vertexShader:Int = 0, val programId:Int = 0) {


    companion object {
        fun fromSource(vertexSource: String, fragmentSource: String): ShaderProgram {

            val fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
            GL20.glShaderSource(fragmentShader, fragmentSource)
            GL20.glCompileShader(fragmentShader)
            if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == 0) {
                throw Exception("Error compiling Shader code: " + GL20.glGetShaderInfoLog(fragmentShader, 1024))
            }
            val vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
            GL20.glShaderSource(vertexShader, vertexSource.trimIndent())
            GL20.glCompileShader(vertexShader)
            if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == 0) {
                throw Exception("Error compiling Shader code: " + GL20.glGetShaderInfoLog(vertexShader, 1024))
            }
            val program = GL20.glCreateProgram()
            GL20.glAttachShader(program, fragmentShader)
            GL20.glAttachShader(program, vertexShader)
            GL20.glLinkProgram(program)
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
                throw Exception("Error linking programId: " + GL20.glGetProgramInfoLog(program))
            }
            return ShaderProgram(vertexShader=vertexShader,fragmentShader=fragmentShader, programId = program)
        }
        fun fromFilename(vertexSource:String,fragmentSource:String): ShaderProgram {
            return fromSource(vertexSource=File(vertexSource).readText(),fragmentSource=File(fragmentSource).readText())
        }
    }

    init{

    }
    fun cleanup() {
        println("Deleting shader")
        GL20.glDeleteProgram(programId)
        GL20.glDeleteShader(vertexShader)
        GL20.glDeleteShader(fragmentShader)
    }

}