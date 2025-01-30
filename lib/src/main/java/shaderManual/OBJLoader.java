package shaderManual;

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
        List<float[]> textures = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            if (tokens[0].equals("v")) {
                vertices.add(new float[] {
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                });
            } else if (tokens[0].equals("vt")) {
                textures.add(new float[] {
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2])
                });
            } else if (tokens[0].equals("vn")) {
                normals.add(new float[] {
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                });
            } else if (tokens[0].equals("f")) {
                faces.add(new int[] {
                    Integer.parseInt(tokens[1].split("/")[0]),
                    Integer.parseInt(tokens[2].split("/")[0]),
                    Integer.parseInt(tokens[3].split("/")[0])
                });
            }
        }
        reader.close();

        return new Model(vertices, textures, normals, faces);
    }
}