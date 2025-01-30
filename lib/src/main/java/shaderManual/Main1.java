package shaderManual;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.io.IOException;

import org.joml.Matrix4f;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main1 {

    private long window;
    private List<ModelInstance> modelInstances = new ArrayList<>();
    private float pitch = 0.0f, yaw = 0.0f;
    private Camera camera =new Camera(15.0f, 8.0f, -35.0f, 5.0f);


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
        
        float zOffset = -14.0f; // Desplazamiento hacia atrás en el plano Z

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
        initLighting();
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
            float deltaTime = (currentTime - lastTime) / 1000.0f; // Calcular deltaTime en segundos
            lastTime = currentTime;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            camera.updateCamera(deltaTime);
            drawPlane(); // Dibujar el plano
            drawModelsWithShadows(deltaTime); // Dibujar modelos con sombras

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }




    private void drawPlane() {
        glPushMatrix();

        // Configurar material para el rectángulo
        FloatBuffer rectangleMaterialAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 0.5f, 0.0f, 1.0f}); // Color verde oscuro
        rectangleMaterialAmbient.flip();
        glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, rectangleMaterialAmbient);

        glBegin(GL_QUADS);
        glVertex3f(-30.0f, 0.0f, -20.0f); // Esquina inferior izquierda (ajustada)
        glVertex3f(30.0f, 0.0f, -20.0f);  // Esquina inferior derecha (ajustada)
        glVertex3f(30.0f, 0.0f, 20.0f);   // Esquina superior derecha (ajustada)
        glVertex3f(-30.0f, 0.0f, 20.0f);  // Esquina superior izquierda (ajustada)
        glEnd();

        glPopMatrix();
    }




    
    private void renderModel(ModelInstance instance) {
        glPushMatrix();
        glTranslatef(instance.x, instance.y, instance.z);

        // Rotar cada modelo alrededor de su propio eje Y
        glRotatef(instance.rotationAngle, 0.0f, 1.0f, 0.0f);

        glBegin(GL_TRIANGLES);
        for (int[] face : instance.model.faces) {
            for (int vertexIndex : face) {
                float[] vertex = instance.model.vertices.get(vertexIndex - 1);
                glVertex3f(vertex[0], vertex[1], vertex[2]);
            }
        }
        glEnd();

        glPopMatrix();
    }


    

    private void setupShadowMatrix(float[] planeNormal, float planeD) {
        float[] shadowMatrix = new float[16];
        
        shadowMatrix[0] = 1.0f - planeNormal[0] * planeNormal[0];
        shadowMatrix[1] = -planeNormal[0] * planeNormal[1];
        shadowMatrix[2] = -planeNormal[0] * planeNormal[2];
        shadowMatrix[3] = 0.0f;

        shadowMatrix[4] = -planeNormal[1] * planeNormal[0];
        shadowMatrix[5] = 1.0f - planeNormal[1] * planeNormal[1];
        shadowMatrix[6] = -planeNormal[1] * planeNormal[2];
        shadowMatrix[7] = 0.0f;

        shadowMatrix[8] = -planeNormal[2] * planeNormal[0];
        shadowMatrix[9] = -planeNormal[2] * planeNormal[1];
        shadowMatrix[10] = 1.0f - planeNormal[2] * planeNormal[2];
        shadowMatrix[11] = 0.0f;

        shadowMatrix[12] = -planeD * planeNormal[0];
        shadowMatrix[13] = -planeD * planeNormal[1];
        shadowMatrix[14] = -planeD * planeNormal[2];
        shadowMatrix[15] = 1.0f - planeD * (planeNormal[0] + planeNormal[1] + planeNormal[2]);

        FloatBuffer shadowMatrixBuffer = BufferUtils.createFloatBuffer(16);
        shadowMatrixBuffer.put(shadowMatrix);
        shadowMatrixBuffer.flip();

        glPushMatrix();
        glMultMatrixf(shadowMatrixBuffer);
    }



    private void drawShadow(ModelInstance instance, float[] planeNormal, float planeD) {
        glPushMatrix();

        // Configura la sombra en el plano
        setupShadowMatrix(planeNormal, planeD); // Configura la matriz de proyección de sombras

        // Posiciona la sombra bajo el objeto rotado
        glTranslatef(instance.x, 0.0f, instance.z); // Ajusta para dibujar la sombra en el plano
        glRotatef(instance.rotationAngle, 0.0f, 1.0f, 0.0f); // Aplica la rotación del objeto

        renderShadow(); // Renderiza la sombra

        glPopMatrix();
    }



    private void drawModelsWithShadows(float deltaTime) {
        float[] planeNormal = {0.0f, 1.0f, 0.0f}; // Normal del plano
        float planeD = 0.0f; // Distancia del plano desde el origen

        float rotationSpeed = 50.0f; // Velocidad de rotación en grados por segundo

        // Configura la iluminación una vez
        initLighting();

        // Primero, dibuja las sombras
        for (ModelInstance instance : modelInstances) {
            instance.rotationAngle += rotationSpeed * deltaTime; // Incrementar el ángulo de rotación
            if (instance.rotationAngle >= 360.0f) {
                instance.rotationAngle -= 360.0f; // Mantener el ángulo entre 0 y 360 grados
            }
            drawShadow(instance, planeNormal, planeD);
        }

        // Luego, dibuja los modelos encima de las sombras
        drawModels();
    }




    private void renderShadow() {
        // Configura el color y el material para la sombra
        glPushAttrib(GL_COLOR_BUFFER_BIT | GL_ENABLE_BIT);
        glDisable(GL_LIGHTING); // Apaga la iluminación para las sombras

        // Color de la sombra (negro)
        glColor3f(0.0f, 0.0f, 0.0f);
        
        // Aquí se dibuja un plano como una sombra básica. Puedes personalizar este método
        // para reflejar más fielmente la forma del modelo.
        // Dibuja un rectángulo en el plano (esto es solo un ejemplo)
        glBegin(GL_QUADS);
        glVertex3f(-1.2f, 0.0f, -1.2f); // Esquina inferior izquierda
        glVertex3f(1.2f, 0.0f, -1.2f);  // Esquina inferior derecha
        glVertex3f(1.2f, 0.0f, 1.2f);   // Esquina superior derecha
        glVertex3f(-1.2f, 0.0f, 1.2f);  // Esquina superior izquierda
        glEnd();



        glPopAttrib();
    }

    
    

    private void drawModels() {
        glPushMatrix();

        // Configurar material para los modelos
        FloatBuffer modelMaterialAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.5f, 0.5f, 0.5f, 1.0f}); // Gris medio
        modelMaterialAmbient.flip();
        glMaterialfv(GL_FRONT, GL_AMBIENT, modelMaterialAmbient);

        FloatBuffer modelMaterialDiffuse = BufferUtils.createFloatBuffer(4).put(new float[]{0.6f, 0.6f, 0.6f, 1.0f}); // Gris claro
        modelMaterialDiffuse.flip();
        glMaterialfv(GL_FRONT, GL_DIFFUSE, modelMaterialDiffuse);

        FloatBuffer modelMaterialSpecular = BufferUtils.createFloatBuffer(4).put(new float[]{0.9f, 0.9f, 0.9f, 1.0f}); // Blanco
        modelMaterialSpecular.flip();
        glMaterialfv(GL_FRONT, GL_SPECULAR, modelMaterialSpecular);

        glMaterialf(GL_FRONT, GL_SHININESS, 100.0f); // Más brillo

        for (ModelInstance instance : modelInstances) {
            renderModel(instance);
        }

        glPopMatrix();
    }



    


    private void initLighting() {
        glEnable(GL_LIGHTING);  // Habilita el sistema de iluminación
        glEnable(GL_LIGHT0);    // Habilita la luz 0

        // Configurar luz direccional desde arriba
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 10.0f, 0.0f, 1.0f}); // Luz direccional desde arriba
        lightPosition.flip();
        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);

        // Configurar luz difusa
        FloatBuffer diffuseLight = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f}); // Luz blanca difusa
        diffuseLight.flip();
        glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuseLight);

        // Configurar luz ambiental
        FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4).put(new float[]{0.3f, 0.3f, 0.3f, 1.0f}); // Luz ambiental más fuerte
        ambientLight.flip();
        glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);

        // Configurar material para los objetos
        FloatBuffer materialAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.8f, 0.8f, 0.8f, 1.0f}); // Gris claro
        materialAmbient.flip();
        glMaterialfv(GL_FRONT, GL_AMBIENT, materialAmbient);

        FloatBuffer materialDiffuse = BufferUtils.createFloatBuffer(4).put(new float[]{0.8f, 0.8f, 0.8f, 1.0f}); // Gris claro
        materialDiffuse.flip();
        glMaterialfv(GL_FRONT, GL_DIFFUSE, materialDiffuse);

        glMaterialf(GL_FRONT, GL_SHININESS, 50.0f); // Brillo del material
    }

    



    public static void main(String[] args) {
        new Main1().run();
    }
    
}

class ModelInstance {
    Model model;
    float x, y, z;
    float rotationAngle = 0.0f; // Ángulo de rotación inicial

    public ModelInstance(Model model, float x, float y, float z) {
        this.model = model;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}


