package com.shader;

import org.lwjgl.opengl.GL11;

public class Model {
    private float[] vertices;
    private float[] normals;
    private int[] indices;

    // Constructor para inicializar los datos del modelo
    public Model(float[] vertices, float[] normals, int[] indices) {
        this.vertices = vertices;
        this.normals = normals;
        this.indices = indices;
    }

    // Método para renderizar el modelo
    public void render() {
        GL11.glBegin(GL11.GL_TRIANGLES);
        
        for (int i = 0; i < indices.length; i++) {
            int vertexIndex = indices[i];
            GL11.glNormal3f(normals[vertexIndex * 3], normals[vertexIndex * 3 + 1], normals[vertexIndex * 3 + 2]);
            GL11.glVertex3f(vertices[vertexIndex * 3], vertices[vertexIndex * 3 + 1], vertices[vertexIndex * 3 + 2]);
        }
        
        GL11.glEnd();
    }
    
 // Method to render the shadow of the model
    public void renderShadow() {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING); // Disable lighting for the shadow rendering
        
        // Draw shadow as a 2D projection on the plane
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i = 0; i < indices.length; i++) {
            int vertexIndex = indices[i];
            // For a shadow, use only x and z coordinates, set y to 0
            GL11.glVertex3f(vertices[vertexIndex * 3], 0.0f, vertices[vertexIndex * 3 + 2]);
        }
        
        GL11.glEnd();
        GL11.glEnable(GL11.GL_LIGHTING); // Re-enable lighting
        GL11.glPopMatrix();
    }

    // Getters y setters (si es necesario)
}