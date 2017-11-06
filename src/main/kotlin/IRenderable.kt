import org.joml.Matrix4f

interface IRenderable {
    fun cleanup()
    fun render(viewMatrix: Matrix4f, perspectiveMatrix: Matrix4f, frame: Int = 0)
    fun renderNormals(debugger: LineDebugRenderable, frame: Int = 0)
}