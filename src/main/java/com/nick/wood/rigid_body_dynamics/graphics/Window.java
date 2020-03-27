package com.nick.wood.rigid_body_dynamics.graphics;

import com.nick.wood.rigid_body_dynamics.Plane;
import com.nick.wood.rigid_body_dynamics.graphics.objects.Camera;
import com.nick.wood.rigid_body_dynamics.graphics.objects.Cube;
import com.nick.wood.rigid_body_dynamics.graphics.objects.GameObject;
import com.nick.wood.rigid_body_dynamics.graphics.math.Matrix4d;
import com.nick.wood.rigid_body_dynamics.graphics.math.Vec3d;
import com.nick.wood.rigid_body_dynamics.graphics.objects.Square;
import com.nick.wood.rigid_body_dynamics.particle_system_dynamics_verbose.Particle;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

	private final Inputs input;
	private final Camera camera;
	// The window handle
	private long window;
	private int WIDTH;
	private int HEIGHT;
	private String title;

	private Shader shader;
	private Renderer renderer;
	private Matrix4d projectionMatrix;
	private double newMouseX, newMouseY;
	private double oldMouseX = 0;
	private double oldMouseY = 0;

	private boolean windowSizeChanged = false;

	HashMap<UUID, GameObject> gameObjects = new HashMap<>();

	public Window(int WIDTH, int HEIGHT, String title, HashMap<UUID, Particle> particles, ArrayList<Plane> planes) {
		this.WIDTH = WIDTH;
		this.HEIGHT = HEIGHT;
		this.title = title;
		this.camera = new Camera(new Vec3d(10.0, -5.0, 50.0), new Vec3d(0.0, 0.0, 0.0), 0.5, 0.1);
		this.input = new Inputs();

		particles.forEach((uuid, particle) -> {
			gameObjects.put(uuid, new GameObject(new Vec3d(particle.getPosition().getX(), particle.getPosition().getZ(), particle.getPosition().getY()), Vec3d.ZERO, new Vec3d(1.0, 1.0, 1.0), new Cube()));
		});

		for (Plane plane : planes) {

			Vec3d rotation = plane.getNormal();

			if (plane.getNormal().equals(Vec3d.Z)) {
				rotation = Vec3d.X;
			}
			else if (plane.getNormal().equals(Vec3d.X)) {
				rotation = Vec3d.Z;
			}

			gameObjects.put(UUID.randomUUID(), new GameObject(new Vec3d(plane.getCenter().getX(), plane.getCenter().getZ(), plane.getCenter().getY()), rotation.scale(90.0), new Vec3d(100.0, 100.0, 100.0), new Square()));
		}

		for (int xPos = -10; xPos < 10; xPos+=2) {
			for (int yPos = -10; yPos < 10; yPos += 2) {
				for (int zPos = -10; zPos < 10; zPos += 2) {
					gameObjects.put(UUID.randomUUID(), new GameObject(new Vec3d(xPos, yPos, zPos), Vec3d.ZERO, new Vec3d(1.0, 1.0, 1.0), new Cube()));
				}
			}
		}

		this.projectionMatrix = Matrix4d.Projection((double)WIDTH/(double)HEIGHT, Math.toRadians(70.0), 0.01, 1000);
	}

	public void destroy() {

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		shader.destroy();

		for (GameObject gameObject : gameObjects.values()) {
			gameObject.getMeshObject().getMesh().destroy();
		}

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	void init() {

		shader = new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl");
		renderer = new Renderer(this);


		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		createCallbacks();

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
					window,
					(vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);


		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		GL11.glEnable(GL_DEPTH_TEST);

		// this locks cursor to center so can always look about
		GLFW.glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

		for (GameObject gameObject : gameObjects.values()) {
			gameObject.getMeshObject().getMesh().create();
		}

		shader.create();
	}

	private void createCallbacks() {
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, input.getKeyboard());
		glfwSetCursorPosCallback(window, input.getMouseMove());
		glfwSetMouseButtonCallback(window, input.getMouseButton());
		glfwSetScrollCallback(window, input.getGlfwScrollCallback());
		glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				WIDTH = width;
				HEIGHT = height;
				windowSizeChanged = true;
			}
		});

	}

	void loop() {

		// user inputs
		if (input.isKeyPressed(GLFW_KEY_ESCAPE)) {
			glfwSetWindowShouldClose(window, true);
		}

		newMouseX = input.getMouseX();
		newMouseY = input.getMouseY();
		double dx = newMouseX - oldMouseX;
		double dy = newMouseY - oldMouseY;
		if (oldMouseX == 0 && oldMouseY == 0) {
			dx = 0.0;
			dy = 0.0;
		}
		oldMouseX = newMouseX;
		oldMouseY = newMouseY;

		camera.rotate(dx, dy);

		if (input.isKeyPressed(GLFW_KEY_A)) {
			camera.left();
		}
		if (input.isKeyPressed(GLFW_KEY_W)) {
			camera.forward();
		}
		if (input.isKeyPressed(GLFW_KEY_D)) {
			camera.right();
		}
		if (input.isKeyPressed(GLFW_KEY_S)) {
			camera.back();
		}
		if (input.isKeyPressed(GLFW_KEY_SPACE)) {
			camera.up();
		}
		if (input.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
			camera.down();
		}
		if (input.isKeyPressed(GLFW_KEY_LEFT)) {
			gameObjects.forEach((uuid, gameObject) -> gameObject.rotateLeft());
		}
		if (input.isKeyPressed(GLFW_KEY_RIGHT)) {
			gameObjects.forEach((uuid, gameObject) -> gameObject.rotateRight());
		}

		if (windowSizeChanged) {
			glViewport(0, 0, WIDTH, HEIGHT);
			windowSizeChanged = false;
		}

		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		// Poll for window events. The key callback above will only be
		// invoked during this call.
		glfwPollEvents();

		gameObjects.forEach((uuid, gameObject) -> gameObject.update());

		for (GameObject gameObject : gameObjects.values()) {
			renderer.renderMesh(gameObject, camera);
		}

		glfwSwapBuffers(window); // swap the color buffers

	}

	public void setTitle(String title) {
		glfwSetWindowTitle(window, title);
	}

	public long getWindow() {
		return window;
	}

	public Matrix4d getProjectionMatrix() {
		return projectionMatrix;
	}

	public Shader getShader() {
		return shader;
	}

	public void updateDrawables(HashMap<UUID, Particle> particles) {

		particles.forEach((uuid, particle) -> {
			gameObjects.get(uuid).setPosition(new Vec3d(particle.getPosition().getX(), particle.getPosition().getZ(), particle.getPosition().getY()));
		});

	}
}