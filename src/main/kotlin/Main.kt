import org.lwjgl.glfw.*
import org.lwjgl.opengl.*

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil.*
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer


//val obj = ObjLoader("cow2.obj")
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
val Z_FAR = 2000f

val meshName = "bob_lamp"//"scarry"
val anim = Md5AnimLoader("${meshName}.md5anim")
val bob = Md5Loader("${meshName}.md5mesh")

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
    val manipulator = TrackballManipulator(0, 0)
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
        manipulator.onKeyCallback(key, scancode, action, mods)
    })
    glfwSetMouseButtonCallback(window, { _, button, action, mods -> manipulator.onMouseButtonCallback(button, action) })
    glfwSetCursorPosCallback(window, { _, xpos, ypos -> manipulator.onMousePosCallback(xpos, ypos) })
    glfwSetScrollCallback(window, { _, xoffset, yoffset -> manipulator.onScroll(xoffset, yoffset) })
    glfwMakeContextCurrent(window)

    glfwShowWindow(window)

    println("Running")

    val capabilities = GL.createCapabilities()
    glClearColor(0.1f, 0.3f, 0.3f, 1f)
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_TEXTURE_2D)
    var shaderProg: ShaderProgram? = null
    var shadowShaderProg: ShaderProgram? = null

//    val gui = Gui(gamestate)
    val renderables = mutableListOf<IRenderable>()
    val debugger: LineDebugRenderable
    val text: TextDebugRenderable
//    gui.isVisible = true
    try {

        shaderProg = ShaderProgram.fromFilename(vertexSource = "md5-diffuse-shadowed.v.glsl", fragmentSource = "md5-diffuse-shadowed.f.glsl")
        shadowShaderProg = ShaderProgram.fromFilename(vertexSource = "tri-pos-normal.v.glsl", fragmentSource = "shadow.f.glsl")
        renderables.add(Md5Renderable(bob,anim,shaderProgram = shaderProg, shaderProgramForShadows = shadowShaderProg))
        debugger = LineDebugRenderable()
        renderables.add(debugger)
        text = TextDebugRenderable()
        renderables.add(text)

        val lamp = Lamp()



        val textureman = TextureManager()

        val nvg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_STENCIL_STROKES)
        if (nvg == null) {
            throw Exception("Failed to init NanoVG")
        }
        val color = NVGColor.create()

        var time = glfwGetTime()
        var frame = 0.0
        while (!glfwWindowShouldClose(window) && gamestate.isRunning) {

            val deltaTime = glfwGetTime() - time
            frame += deltaTime*24f
            if (frame > 119) {
                frame = 0.01
            }
            time = glfwGetTime()

            manipulator.onUpdate(deltaTime.toFloat())

            debugger.clear()
            debugger.addLine(Vector3f(0F, 0F, 0F), Vector3f(0f, 100f, 0f), debugger.COL_GREEN)
            debugger.addLine(Vector3f(0F, 0F, 0F), Vector3f(100f, 0f, 0f), debugger.COL_RED)
            debugger.addLine(Vector3f(0F, 0F, 0F), Vector3f(0f, 0f, 100f), debugger.COL_BLUE)

            text.clear()
            text.addLine("Kotlin", 0f, 0f, 0f)
            text.addLine("x", 9f, 0f, 0f)
            text.addLine("y", 0f, 9f, 0f)
            text.addLine("z", 0f, 0f, 09f)
//            renderables.forEach { x -> x.renderNormals(debugger, frame=frame.toInt()) }

            lamp.renderPass(
                    textureManager = textureman,
                    renderables = renderables,
                    frame = frame.toInt()
            )

            glEnable(GL_STENCIL)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
            GL11.glViewport(0,0,WIDTH,HEIGHT)
            renderables.forEach { x -> x.render(manipulator, textureman,lamp=lamp,frame=frame.toInt()) }

            NanoVG.nvgBeginFrame(nvg,WIDTH,HEIGHT,1.0f)
            nvgRect(nvg,10f,10f,20f,20f)
            nvgFillColor(nvg,NanoVG.nvgRGBA(0xBA.toByte(), 0xBA.toByte(), 0xEE.toByte(), 0xFF.toByte(),color))
            nvgFill(nvg)
            NanoVG.nvgEndFrame(nvg)
            glEnable(GL_DEPTH_TEST)
//            glEnable(GL_STENCIL_TEST);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
//            glEnable(GL_CULL_FACE);
//            glCullFace(GL_BACK);


//            glDisable(GL_STENCIL)

            glfwSwapBuffers(window) // swap the color buffers
            glfwPollEvents()
        }

    } finally {
            shaderProg?.cleanup()
        shadowShaderProg?.cleanup()
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
    glfwWindowHint(GLFW_SAMPLES,2)
    val window = glfwCreateWindow(WIDTH, HEIGHT, "Gentle and Smol", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to open a window")
    }
    return window
}

