import org.joml.Matrix4f

interface IRenderable {
    fun cleanup()
    fun render(viewMatrix: Matrix4f, perspectiveMatrix: Matrix4f)
    fun renderNormals(debugger: LineDebugRenderable)
}