import org.joml.Matrix4f

interface IManipulator : ICamera {
    fun onKeyCallback(key: Int, scancode: Int, action: Int, mods: Int)
    fun onMouseButtonCallback(button: Int, action: Int)
    fun onMousePosCallback(xpos: Double, ypos: Double)
    fun onUpdate(deltaTime: Float)
    fun onScroll(xoffset: Double, yoffset: Double)
}