package versionF;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Model {
    private int vaoId;
    private int vboIdVertices;
    private int vboIdNormals;
    private int vboIdIndices;
    private int vertexCount;

    public Model(float[] vertices, float[] normals, int[] indices) {
        this.vertexCount = indices.length;

        // Crear un nuevo VAO
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // VBO para los vértices
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();
        vboIdVertices = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdVertices);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // VBO para las normales
        FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
        normalsBuffer.put(normals).flip();
        vboIdNormals = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboIdNormals);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalsBuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // VBO para los índices
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices).flip();
        vboIdIndices = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIdIndices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);

        GL30.glBindVertexArray(0);
    }

    public void render() {
        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0); // Habilitar vértices
        GL20.glEnableVertexAttribArray(1); // Habilitar normales

        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }


    public void cleanUp() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        // Eliminar los VBOs
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboIdVertices);
        GL15.glDeleteBuffers(vboIdNormals);
        GL15.glDeleteBuffers(vboIdIndices);

        // Eliminar el VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }

	public int getVaoId() {
		return vaoId;
	}

	public int getVboIdVertices() {
		return vboIdVertices;
	}

	public int getVboIdNormals() {
		return vboIdNormals;
	}

	public int getVboIdIndices() {
		return vboIdIndices;
	}

	public int getVertexCount() {
		return vertexCount;
	}
    
    
}

