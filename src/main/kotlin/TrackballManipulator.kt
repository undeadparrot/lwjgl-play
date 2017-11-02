import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_PRESS

class TrackballManipulator(var mousex: Int, var mousey: Int) : IManipulator {
    var isMouseLDown = false
    var isMouseRDown = false
    var acceleration = Vector2f()
    var deceleration:Float = 2F
    var velocity = Vector2f()
    var rotation = Vector2f()
    var distance = 5F

    override fun onKeyCallback(key:Int, scancode:Int, action:Int, mods:Int){

    }

    override fun onMouseButtonCallback(button: Int, action: Int) {
        when(button){
            GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                isMouseLDown = action==GLFW_PRESS
            }
            GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                isMouseRDown = action==GLFW_PRESS
            }
        }
    }

    override fun onMousePosCallback(xpos: Double, ypos: Double) {
        val oldx=this.mousex
        val oldy=this.mousey
        val offsetx = xpos-oldx
        val offsety = ypos-oldy
        this.mousex= xpos.toInt()
        this.mousey= ypos.toInt()

        if(isMouseLDown){
            velocity.add((offsetx/15f).toFloat(),(offsety/15f).toFloat())
        }
    }

    override fun onUpdate(deltaTime: Float){
        rotation.add(velocity.x*deltaTime,velocity.y*deltaTime)
        velocity.mul(1F-deceleration*deltaTime,1F-deceleration*deltaTime)
    }

    override fun getViewMatrix(): Matrix4f {
        val heightOffset = -distance/3F
        return Matrix4f()
                .translate(0F,heightOffset,-distance)
                .scale(0.2F)
                .rotateXYZ(Vector3f(rotation.y,rotation.x,0F))
                .translate(0F,0F,0F)
    }

    override fun onScroll(xoffset: Double, yoffset: Double) {
        distance += yoffset.toFloat()
    }

}