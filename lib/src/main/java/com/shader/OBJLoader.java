
package com.shader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJLoader {
	
    public static Model loadModel(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        List<float[]> vertices = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            if (tokens[0].equals("v")) {
                vertices.add(new float[]{
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                });
            } else if (tokens[0].equals("vn")) {
                normals.add(new float[]{
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                });
            } else if (tokens[0].equals("f")) {
                for (int i = 1; i < tokens.length; i++) {
                    String[] vertexData = tokens[i].split("/");
                    faces.add(new int[]{
                        Integer.parseInt(vertexData[0]) - 1 // Indices in OBJ files are 1-based
                    });
                }
            }
        }
        reader.close();

        // Convert lists to arrays
        float[] verticesArray = new float[vertices.size() * 3];
        float[] normalsArray = new float[normals.size() * 3];
        int[] indicesArray = new int[faces.size() * 3];

        int i = 0;
        for (float[] vertex : vertices) {
            verticesArray[i++] = vertex[0];
            verticesArray[i++] = vertex[1];
            verticesArray[i++] = vertex[2];
        }

        i = 0;
        for (float[] normal : normals) {
            normalsArray[i++] = normal[0];
            normalsArray[i++] = normal[1];
            normalsArray[i++] = normal[2];
        }

        i = 0;
        for (int[] face : faces) {
            indicesArray[i++] = face[0];
        }

        return new Model(verticesArray, normalsArray, indicesArray);
    }
}