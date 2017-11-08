import org.joml.Matrix4f

interface ICamera {
    fun getViewMatrix(): Matrix4f
    fun getProjectionMatrix(): Matrix4f
}