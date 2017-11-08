import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

class FlyManipulator(var mousex: Int, var mousey: Int) : IManipulator {

    var isMouseLDown = false
    var isMouseRDown = false
    var deceleration:Float = 9F
    var rotationalVelocity = Vector2f()
    var translationalVelocity = Vector3f()
    var position = Vector3f(0F,0F,-5F)
    var orientation = Quaternionf().lookAlong(Vector3f(0F,0F,1F),Vector3f(0F,1F,0F))

    override fun onKeyCallback(key:Int, scancode:Int, action:Int, mods:Int){
        when(key){
            GLFW.GLFW_KEY_W->{
                translationalVelocity.add(0f,0f,2f)
            }
            GLFW.GLFW_KEY_S->{
                translationalVelocity.add(0f,0f,-2f)
            }
        }
    }

    override fun onMouseButtonCallback(button: Int, action: Int) {
        when(button){
            GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                isMouseLDown = action== GLFW.GLFW_PRESS
            }
            GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                isMouseRDown = action== GLFW.GLFW_PRESS
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
            rotationalVelocity.add((offsetx/15f).toFloat(),(offsety/15f).toFloat())
        }
    }

    override fun onUpdate(deltaTime: Float){
        orientation.rotate(rotationalVelocity.x*deltaTime, 0F, rotationalVelocity.y*deltaTime)
        rotationalVelocity.mul(1F-deceleration*deltaTime,1F-deceleration*deltaTime)
        position.add(translationalVelocity.x*deltaTime,translationalVelocity.y*deltaTime,translationalVelocity.z*deltaTime )
        translationalVelocity.mul(1F-deceleration*deltaTime,1F-deceleration*deltaTime,1F-deceleration*deltaTime)
    }
    override fun getProjectionMatrix(): Matrix4f {
        return Matrix4f().perspective(Math.toRadians(45.0).toFloat(), 1F, 0.01f, 100.0f)
    }

    override fun getViewMatrix(): Matrix4f {
        return Matrix4f()
                .translate(0F,0F,0F)
                .scale(0.2F)
                .rotate(orientation)
                .translate(position)
    }

    override fun onScroll(xoffset: Double, yoffset: Double) {
        position.add(0F,yoffset.toFloat(),0F)
    }

}