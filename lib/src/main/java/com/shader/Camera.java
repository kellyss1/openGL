package com.shader;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Camera {
    private float cameraSpeed;
    private float cameraY;
    private float cameraPositionX;
    private float cameraPositionZ;
    private int edgeDirection;

    public Camera(float cameraSpeed, float cameraY, float cameraPositionX, float cameraPositionZ) {
        this.cameraSpeed = cameraSpeed;
        this.cameraY = cameraY;
        this.cameraPositionX = cameraPositionX;
        this.cameraPositionZ = cameraPositionZ;
        this.edgeDirection = 0;
    }

    public void update(float deltaTime) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        float movement = cameraSpeed * deltaTime;

        switch (edgeDirection) {
            case 0:
                cameraPositionX += movement;
                if (cameraPositionX >= 40.0f) {
                    cameraPositionX = 40.0f;
                    edgeDirection = 1;
                }
                break;
            case 1:
                cameraPositionZ += movement;
                if (cameraPositionZ >= 30.0f) {
                    cameraPositionZ = 30.0f;
                    edgeDirection = 2;
                }
                break;
            case 2:
                cameraPositionX -= movement;
                if (cameraPositionX <= -40.0f) {
                    cameraPositionX = -40.0f;
                    edgeDirection = 3;
                }
                break;
            case 3:
                cameraPositionZ -= movement;
                if (cameraPositionZ <= -30.0f) {
                    cameraPositionZ = -30.0f;
                    edgeDirection = 0;
                }
                break;
        }

        lookAt(cameraPositionX, cameraY, cameraPositionZ, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    private void lookAt(float eyeX, float eyeY, float eyeZ,
                        float centerX, float centerY, float centerZ,
                        float upX, float upY, float upZ) {
        float[] forward = {centerX - eyeX, centerY - eyeY, centerZ - eyeZ};
        normalize(forward);

        float[] up = {upX, upY, upZ};
        normalize(up);

        float[] side = cross(forward, up);
        normalize(side);

        float[] upVector = cross(side, forward);

        FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
        matrix.put(new float[]{
                side[0], upVector[0], -forward[0], 0,
                side[1], upVector[1], -forward[1], 0,
                side[2], upVector[2], -forward[2], 0,
                0, 0, 0, 1
        }).flip();

        glMultMatrixf(matrix);
        glTranslatef(-eyeX, -eyeY, -eyeZ);
    }

    private void normalize(float[] v) {
        float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (length != 0.0f) {
            v[0] /= length;
            v[1] /= length;
            v[2] /= length;
        }
    }

    private float[] cross(float[] a, float[] b) {
        return new float[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    // Getters and setters if needed
}
