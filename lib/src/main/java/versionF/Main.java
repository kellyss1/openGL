package versionF;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

	private long window;
	private List<ModelInstance> modelInstances = new ArrayList<>();
	private int shaderProgram;
	private int shadowShaderProgram;
	private float pitch = 0.0f, yaw = 0.0f;
	Camera camera = new Camera(3.7f, 6.0f, -40.0f, -30.0f);

	public static void main(String[] args) throws IOException {
		new Main().run();
	}

	private void run() throws IOException {
		init();
		loop();
		glfwDestroyWindow(window);
		glfwTerminate();
	}

	private void init() throws IOException {
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		window = glfwCreateWindow(1280, 720, "Modelo 3D - OBJ", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);

		GL.createCapabilities();
		glEnable(GL_DEPTH_TEST);

		float zOffset = -14.0f;
		try {
			int numObjects = 20; // Número de objetos
			float radius = 27.0f; // Radio de la semicircunferencia
			float step = (float) Math.PI / (numObjects - 1); // Paso de ángulo para distribuir los objetos

			for (int i = 0; i < numObjects; i++) {
				String modelPath = String.format("src/main/resources/estatuas/estatua%dRD.obj", i + 1);
				Model model = OBJLoader.loadModel(modelPath);

				float angle = i * step; // Calcular el ángulo
				float x = radius * (float) Math.cos(angle); // Calcular la posición en x
				float z = radius * (float) Math.sin(angle) + zOffset; // Calcular la posición en z con desplazamiento

				// Crear una nueva instancia y añadirla a la lista
				modelInstances.add(new ModelInstance(model, x, 2.3f, z));
			}

			// Agregar la instancia central
			String centralModelPath = "src/main/resources/estatuas/estatua21RD.obj";
			Model centralModel = OBJLoader.loadModel(centralModelPath);
			modelInstances.add(new ModelInstance(centralModel, 0.0f, 2.3f, zOffset));

		} catch (IOException e) {
			e.printStackTrace(); // Manejar excepciones en caso de fallo en la carga del modelo
		}

		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
				glfwSetWindowShouldClose(window, true);
			}
		});

		// Cargar y compilar shaders
		shaderProgram = createShaderProgram("src/main/resources/shaders1/modelVertexShader.glsl",
				"src/main/resources/shaders1/modelFragmentShader.glsl");
		shadowShaderProgram = createShaderProgram("src/main/resources/shaders1/shadowVertexShader.glsl",
				"src/main/resources/shaders1/shadowFragmentShader.glsl");

		// Inicializar iluminación (si es necesario)
		initLighting();
	}

	private int createShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
		int vertexShader = loadShader(vertexShaderPath, GL_VERTEX_SHADER);
		int fragmentShader = loadShader(fragmentShaderPath, GL_FRAGMENT_SHADER);

		int program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);

		if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
			throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(program));
		}

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);

		return program;
	}

	private int loadShader(String filePath, int type) {
		StringBuilder shaderSource = new StringBuilder();

		try {
			Files.lines(Paths.get(filePath)).forEach(line -> shaderSource.append(line).append("\n"));
		} catch (IOException e) {
			throw new RuntimeException("Failed to load shader file: " + filePath, e);
		}

		int shader = glCreateShader(type);
		glShaderSource(shader, shaderSource.toString());
		glCompileShader(shader);

		if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
			throw new RuntimeException("Shader compilation failed: " + glGetShaderInfoLog(shader));
		}

		return shader;
	}

	private void loop() {
		glClearColor(0f, 0f, 0f, 0.0f);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		float aspectRatio = (float) 1280 / 720;
		float fov = 40.0f;
		float nearPlane = 0.1f;
		float farPlane = 1000.0f;
		float y_scale = (float) (1 / Math.tan(Math.toRadians(fov / 2)));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = farPlane - nearPlane;

		FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
		projectionMatrix.put(0, x_scale);
		projectionMatrix.put(5, y_scale);
		projectionMatrix.put(10, -((farPlane + nearPlane) / frustum_length));
		projectionMatrix.put(11, -1);
		projectionMatrix.put(14, -((2 * nearPlane * farPlane) / frustum_length));
		projectionMatrix.put(15, 0);
		glLoadMatrixf(projectionMatrix);

		long lastTime = System.currentTimeMillis();

		while (!glfwWindowShouldClose(window)) {
			long currentTime = System.currentTimeMillis();
			float deltaTime = (currentTime - lastTime) / 1000.0f;
			lastTime = currentTime;

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

			updateCamera(deltaTime);
			drawPlane();

			// Renderizar los modelos con el shader de sombras
			renderModels(shadowShaderProgram);

			glfwSwapBuffers(window);
			glfwPollEvents();
		}
	}

	private void updateCamera(float deltaTime) {
		camera.update(deltaTime);
	}

	private void drawPlane() {
		glPushMatrix();

		// Configurar material para el plano
		FloatBuffer planeMaterialAmbient = BufferUtils.createFloatBuffer(4).put(new float[] { 0.0f, 0.5f, 0.0f, 1.0f }); // Color
																															// verde
																															// oscuro
		planeMaterialAmbient.flip();
		glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, planeMaterialAmbient);

		glBegin(GL_QUADS);
		glVertex3f(-30.0f, 0.0f, -20.0f); // Esquina inferior izquierda
		glVertex3f(30.0f, 0.0f, -20.0f); // Esquina inferior derecha
		glVertex3f(30.0f, 0.0f, 20.0f); // Esquina superior derecha
		glVertex3f(-30.0f, 0.0f, 20.0f); // Esquina superior izquierda
		glEnd();

		glPopMatrix();
	}

	private Matrix4f lightSpaceMatrix;
	private void setupShadowMatrix() {
	    // Crear matrices para la luz
	    Matrix4f lightProjection = new Matrix4f().ortho(-10, 10, -10, 10, 0.1f, 100f);
	    Matrix4f lightView = new Matrix4f().lookAt(new Vector3f(0, 10, 10), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
	    lightSpaceMatrix = new Matrix4f().mul(lightProjection).mul(lightView);

	    // Obtener la ubicación del uniforme en el shader de sombras
	    int lightSpaceMatrixLocation = glGetUniformLocation(shadowShaderProgram, "lightSpaceMatrix");

	    // Activar el shader de sombras
	    glUseProgram(shadowShaderProgram);

	    // Enviar la matriz al shader
	    glUniformMatrix4fv(lightSpaceMatrixLocation, false, lightSpaceMatrix.get(new float[16]));
	}

	private void renderModels(int shadowShaderProgram) {
	    setupModelMaterial();
		setupShadowMatrix(); // Configurar la matriz de sombra

	    for (ModelInstance instance : modelInstances) {
	        instance.renderShadow(shadowShaderProgram); // Renderizar sombra
	        instance.render(); // Renderizar el modelo
	    }
	}

	private void initLighting() {
	    glEnable(GL_LIGHTING);
	    glEnable(GL_LIGHT0);

	    // Aumentar la intensidad de la luz ambiental
	    FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
	    ambientLight.put(new float[] { 0.3f, 0.3f, 0.3f, 1.0f }).flip();
	    glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);

	    // Aumentar la intensidad de la luz difusa
	    FloatBuffer diffuseLight = BufferUtils.createFloatBuffer(4);
	    diffuseLight.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
	    glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuseLight);

	    // Aumentar la intensidad de la luz especular
	    FloatBuffer specularLight = BufferUtils.createFloatBuffer(4);
	    specularLight.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
	    glLightfv(GL_LIGHT0, GL_SPECULAR, specularLight);

	    // Cambiar la posición de la luz al centro (o una posición significativa)
	    FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
	    lightPosition.put(new float[] { 1.0f, 1.0f, 1.0f, 0.0f }).flip(); // Posición de la luz en el espacio
	    glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
	}


	private void setupModelMaterial() {
	    // Color ambiental y difuso del material (más sólido)
	    FloatBuffer modelMaterialAmbientAndDiffuse = BufferUtils.createFloatBuffer(4)
	            .put(new float[] { 0.8f, 0.8f, 0.8f, 1.0f }); // Color gris claro
	    modelMaterialAmbientAndDiffuse.flip();
	    glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, modelMaterialAmbientAndDiffuse);

	    // Color especular del material (brillo)
	    FloatBuffer modelMaterialSpecular = BufferUtils.createFloatBuffer(4)
	            .put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }); // Color blanco para el brillo
	    modelMaterialSpecular.flip();
	    glMaterialfv(GL_FRONT, GL_SPECULAR, modelMaterialSpecular);

	    // Brillo del material (controla el tamaño del reflejo especular)
	    glMaterialf(GL_FRONT, GL_SHININESS, 50.0f); // Mayor valor significa un brillo más concentrado
	}


	private class ModelInstance {
		Model model;
		float x, y, z;

		ModelInstance(Model model, float x, float y, float z) {
			this.model = model;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void render() {
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, z); // Aplicar la transformación de posición
			model.render(); // Renderizar el modelo
			GL11.glPopMatrix();
		}

		public void renderShadow(int shaderProgram) {
		    GL20.glUseProgram(shaderProgram); // Usar el shader de sombras

		    // Obtener la ubicación del uniforme en el shader de sombras
		    int modelMatrixLocation = glGetUniformLocation(shaderProgram, "modelMatrix");

		    // Crear y configurar la matriz del modelo
		    Matrix4f modelMatrix = new Matrix4f().translate(x, 0.0f, z);

		    // Enviar la matriz del modelo al shader
		    glUniformMatrix4fv(modelMatrixLocation, false, modelMatrix.get(new float[16]));

		    GL30.glBindVertexArray(model.getVaoId()); // Vincular VAO
		    GL20.glEnableVertexAttribArray(0); // Habilitar el atributo de vértice

		    GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0); // Dibujar sombra

		    GL20.glDisableVertexAttribArray(0); // Deshabilitar atributo de vértice
		    GL30.glBindVertexArray(0); // Desvincular VAO
		    GL20.glUseProgram(0); // Desactivar el shader de sombras
		}



	}
}