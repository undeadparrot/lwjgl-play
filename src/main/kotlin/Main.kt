import org.lwjgl.glfw.*
import org.lwjgl.opengl.*

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.system.MemoryUtil.*
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils


val obj = ObjLoader("cow2.obj")
//val bob = Md5Loader("player.md5mesh")
//val anim = Md5AnimLoader("walk1.md5anim")
//val bindposeJoints = bob.bindposeJoints
//val vertvalues = obj.getOrderedVerts().toFloatArray()

//val anim = Md5AnimLoader("bob_lamp.md5anim")
//val vertvalues = anim.getDebugVertices(0).toFloatArray()

//val bob = Md5Loader("bob_lamp.md5mesh")
//val bindposeJoints = anim.getAllJointsForFrame(0)
//val vertvalues = bob.meshes.flatMap {
//        m->m.getOrderedVerticesFromTris(bindposeJoints).flatMap {
//        v3-> listOf(v3.x,v3.y,v3.z)
//    }
//}.toFloatArray()

val primitive = GL_TRIANGLES

val FOV: Float = (Math.toRadians(60.0).toFloat())
val Z_NEAR = 0.01f
val Z_FAR = 1000f

val anim = Md5AnimLoader("bob_lamp.md5anim")
val bob = Md5Loader("bob_lamp.md5mesh")

fun main(args: Array<String>) {
    val gamestate = GameState()
    GLFWErrorCallback.createPrint(System.err).set()
    val WIDTH = 800
    val HEIGHT = 800
    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }
    val window: Long
    window = setupWindow(WIDTH, HEIGHT)
    val trackball = TrackballManipulator(0, 0)
    glfwSetKeyCallback(window, { _window, key, scancode, action, mods ->
        if (key == GLFW_KEY_ESCAPE) {
            gamestate.isRunning = false
        }
        when (key) {
            GLFW_KEY_W -> gamestate.camPosition.z -= 0.1f
            GLFW_KEY_A -> gamestate.camPosition.x -= 0.1f
            GLFW_KEY_S -> gamestate.camPosition.z += 0.1f
            GLFW_KEY_D -> gamestate.camPosition.x += 0.1f
            GLFW_KEY_F -> gamestate.camPosition.y -= 0.1f
            GLFW_KEY_R -> gamestate.camPosition.y += 0.1f
        }
        trackball.onKeyCallback(key, scancode, action, mods)
    })
    glfwSetMouseButtonCallback(window, { _, button, action, mods -> trackball.onMouseButtonCallback(button, action) })
    glfwSetCursorPosCallback(window, { _, xpos, ypos -> trackball.onMousePosCallback(xpos, ypos) })
    glfwSetScrollCallback(window, { _, xoffset, yoffset -> trackball.onScroll(xoffset, yoffset) })
    glfwMakeContextCurrent(window)

    glfwShowWindow(window)

    println("Running")

    GL.createCapabilities()
    glClearColor(0.1f, 0.3f, 0.3f, 1f)
    glEnable(GL_DEPTH_TEST)
    var shaderProg: ShaderProgram? = null
    val gui = Gui(gamestate)
    val renderables = mutableListOf<IRenderable>()
    val debugger: LineDebugRenderable
//    gui.isVisible = true
    try {

        shaderProg = ShaderProgram.fromFilename(vertexSource = "basic.v.glsl", fragmentSource = "basic.f.glsl")
        renderables.add(Md5Renderable(bob,anim,shaderProg))
        debugger = LineDebugRenderable()
        renderables.add(debugger)

        var time = glfwGetTime()
        var frame = 0.0
        while (!glfwWindowShouldClose(window) && gamestate.isRunning) {

            val deltaTime = glfwGetTime() - time
            frame += 0.1
            if (frame > 119) {
                frame = 0.01
            }
            time = glfwGetTime()
            val aspectRatio = (WIDTH / HEIGHT).toFloat()

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            val mView = trackball.getViewMatrix()
            trackball.onUpdate(deltaTime.toFloat())
            val mPerspective = Matrix4f().perspective(Math.toRadians(45.0).toFloat(), 1.0f, 0.01f, 100.0f)

            debugger.clear()
            debugger.addLine(Vector3f(0F,0F,0F), Vector3f(0f,100f,0f))
            debugger.addLine(Vector3f(0F,0F,0F),Vector3f(100f,0f,0f))
            debugger.addLine(Vector3f(0F,0F,0F),Vector3f(0f,0f,100f))
            renderables[0].renderNormals(debugger)
            renderables.forEach { x-> x.render(mView,mPerspective) }

            glfwSwapBuffers(window) // swap the color buffers
            glfwPollEvents()
        }

    } finally {
        shaderProg?.cleanup()
        renderables.forEach { x -> x.cleanup();renderables.remove(x) }
        glfwTerminate()
        glfwSetErrorCallback(null).free()
    }
    System.exit(0)
}

private fun setupWindow(WIDTH: Int, HEIGHT: Int): Long {
    glfwDefaultWindowHints() // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE) // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GL_TRUE) // the window will be resizable
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
    val window = glfwCreateWindow(WIDTH, HEIGHT, "Gentle and Smol", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to open a window")
    }
    return window
}

