package shaderManual;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Camera {
    private float cameraPositionX;
    private float cameraY;
    private float cameraPositionZ;
    private float cameraSpeed;
    private int edgeDirection;

    public Camera(float initialX, float initialY, float initialZ, float speed) {
        this.cameraPositionX = initialX;
        this.cameraY = initialY;
        this.cameraPositionZ = initialZ;
        this.cameraSpeed = speed;
        this.edgeDirection = 0;
    }

    public void updateCamera(float deltaTime) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Calcular el desplazamiento basado en la velocidad y el tiempo transcurrido
        float movement = cameraSpeed * deltaTime;

        // Determinar el movimiento de la cámara basado en la dirección actual
        switch (edgeDirection) {
            case 0: // Moverse hacia la derecha
                cameraPositionX += movement;
                if (cameraPositionX >= 40.0f) { // Alcanza el nuevo borde derecho
                    cameraPositionX = 40.0f;
                    edgeDirection = 1; // Cambiar dirección hacia arriba
                }
                break;
            case 1: // Moverse hacia arriba
                cameraPositionZ += movement;
                if (cameraPositionZ >= 30.0f) { // Alcanza el nuevo borde superior
                    cameraPositionZ = 30.0f;
                    edgeDirection = 2; // Cambiar dirección hacia la izquierda
                }
                break;
            case 2: // Moverse hacia la izquierda
                cameraPositionX -= movement;
                if (cameraPositionX <= -40.0f) { // Alcanza el nuevo borde izquierdo
                    cameraPositionX = -40.0f;
                    edgeDirection = 3; // Cambiar dirección hacia abajo
                }
                break;
            case 3: // Moverse hacia abajo
                cameraPositionZ -= movement;
                if (cameraPositionZ <= -30.0f) { // Alcanza el nuevo borde inferior
                    cameraPositionZ = -30.0f;
                    edgeDirection = 0; // Cambiar dirección hacia la derecha
                }
                break;
        }

        // Mantener la cámara mirando hacia el centro del plano
        lookAt(cameraPositionX, cameraY, cameraPositionZ, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    private void lookAt(float eyeX, float eyeY, float eyeZ,
                        float centerX, float centerY, float centerZ,
                        float upX, float upY, float upZ) {
        // Calcular los vectores dirección de la cámara
        float[] forward = {centerX - eyeX, centerY - eyeY, centerZ - eyeZ};
        float[] up = {upX, upY, upZ};

        // Normalizar el vector forward
        float fLength = (float) Math.sqrt(forward[0] * forward[0] + forward[1] * forward[1] + forward[2] * forward[2]);
        forward[0] /= fLength;
        forward[1] /= fLength;
        forward[2] /= fLength;

        // Calcular el vector side (derecha)
        float[] side = new float[3];
        side[0] = forward[1] * up[2] - forward[2] * up[1];
        side[1] = forward[2] * up[0] - forward[0] * up[2];
        side[2] = forward[0] * up[1] - forward[1] * up[0];

        // Normalizar el vector side
        float sLength = (float) Math.sqrt(side[0] * side[0] + side[1] * side[1] + side[2] * side[2]);
        side[0] /= sLength;
        side[1] /= sLength;
        side[2] /= sLength;

        // Recalcular el vector up
        up[0] = side[1] * forward[2] - side[2] * forward[1];
        up[1] = side[2] * forward[0] - side[0] * forward[2];
        up[2] = side[0] * forward[1] - side[1] * forward[0];

        // Crear la matriz de vista
        FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
        matrix.put(new float[]{
                side[0], up[0], -forward[0], 0.0f,
                side[1], up[1], -forward[1], 0.0f,
                side[2], up[2], -forward[2], 0.0f,
                0.0f,    0.0f,   0.0f,    1.0f
        }).flip();

        // Aplicar la transformación de vista
        glMultMatrixf(matrix);

        // Trasladar la cámara a la posición deseada
        glTranslatef(-eyeX, -eyeY, -eyeZ);
    }

    // Getters y setters para la posición de la cámara y otras propiedades

    public float getCameraPositionX() {
        return cameraPositionX;
    }

    public void setCameraPositionX(float cameraPositionX) {
        this.cameraPositionX = cameraPositionX;
    }

    public float getCameraY() {
        return cameraY;
    }

    public void setCameraY(float cameraY) {
        this.cameraY = cameraY;
    }

    public float getCameraPositionZ() {
        return cameraPositionZ;
    }

    public void setCameraPositionZ(float cameraPositionZ) {
        this.cameraPositionZ = cameraPositionZ;
    }

    public float getCameraSpeed() {
        return cameraSpeed;
    }

    public void setCameraSpeed(float cameraSpeed) {
        this.cameraSpeed = cameraSpeed;
    }

    public int getEdgeDirection() {
        return edgeDirection;
    }

    public void setEdgeDirection(int edgeDirection) {
        this.edgeDirection = edgeDirection;
    }
}
