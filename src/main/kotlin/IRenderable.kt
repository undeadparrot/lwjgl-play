import org.joml.Matrix4f

interface IRenderable {
    fun cleanup()
    fun render(camera:ICamera, textureManager: TextureManager, lamp:Lamp, frame: Int=0)
    fun renderShadows(camera:ICamera, textureManager: TextureManager,  frame: Int = 0)
    fun renderNormals(debugger: LineDebugRenderable, frame: Int = 0)
}