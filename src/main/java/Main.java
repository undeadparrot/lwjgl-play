
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    public static void main(String[] args){
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit() ) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        int WIDTH = 300;
        int HEIGHT = 300;

        long window = glfwCreateWindow(WIDTH, HEIGHT, "Gentle and Smol",NULL,NULL );
        if (window == NULL){
            throw new RuntimeException("Failed to open a window");
        }

        glfwSetKeyCallback(window,( _window,  key,  scancode,  action,  mods)->{
            if(key == GLFW_KEY_ESCAPE){
                System.out.println("Naat");
            }
        });

        System.out.println("Running");
    }
}
