 package com.shader;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

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

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
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

        shaderProgram = createShaderProgram("src/main/resources/shaders/modelVertexShader.glsl", "src/main/resources/shaders/modelFragmentShader.glsl");
        shadowShaderProgram = createShaderProgram("src/main/resources/shaders/shadowVertexShader.glsl", "src/main/resources/shaders/shadowFragmentShader.glsl");

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
            renderModels();

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
        FloatBuffer planeMaterialAmbient = BufferUtils.createFloatBuffer(4)
                .put(new float[] { 0.0f, 0.5f, 0.0f, 1.0f }); // Color verde oscuro
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



 // Método para configurar las matrices de sombra
    private void setupShadowMatrix() {
        // La matriz de proyección de sombra
        FloatBuffer shadowMatrix = BufferUtils.createFloatBuffer(16);
        // Configura una matriz de sombra simplificada; este es un ejemplo básico
        shadowMatrix.put(new float[]{
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        }).flip();
        
        // Enviar la matriz de sombra al shader
        int shadowMatrixLocation = glGetUniformLocation(shaderProgram, "shadowMatrix");
        glUniformMatrix4fv(shadowMatrixLocation, false, shadowMatrix);
    }

    // Actualizar renderModels para aplicar sombras
    private void renderModels() {
        setupShadowMatrix(); // Configurar la matriz de sombra
        
        for (ModelInstance instance : modelInstances) {
            // Configurar material por defecto para los modelos
            FloatBuffer modelMaterialAmbient = BufferUtils.createFloatBuffer(4)
                    .put(new float[]{1.0f, 1.0f, 1.0f, 1.0f}); // Color blanco
            modelMaterialAmbient.flip();
            glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, modelMaterialAmbient);

            // Renderizar sombra
            instance.renderShadow();

            // Renderizar el modelo
            instance.render();
        }
    }

    


    private void initLighting() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);

        // Reducir la intensidad de la luz ambiental
        FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
        ambientLight.put(new float[]{0.1f, 0.1f, 0.1f, 1.0f}).flip();
        glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);

        // Reducir la intensidad de la luz difusa
        FloatBuffer diffuseLight = BufferUtils.createFloatBuffer(4);
        diffuseLight.put(new float[]{0.5f, 0.5f, 0.5f, 1.0f}).flip();
        glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuseLight);

        // Cambiar la posición de la luz al centro (0, 0, 0)
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(new float[]{0.0f, 0.0f, 0.0f, 1.0f}).flip();
        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
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
            // Aplica la transformación de posición
            glPushMatrix();
            glTranslatef(x,y,z);
            
            // Renderiza el modelo
            model.render();
            
            glPopMatrix();
        }
        
        public void renderShadow() {
            // Renderiza la sombra proyectada en el plano
            glPushMatrix();
            glTranslatef(x, 0.0f, z);
            
            // Renderiza el modelo en 2D para la sombra
            model.renderShadow();
            
            glPopMatrix();
        }
        
    }
}