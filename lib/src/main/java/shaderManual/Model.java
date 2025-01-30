package shaderManual;

import java.util.List;

public class Model {
    public List<float[]> vertices;
    public List<float[]> textures;
    public List<float[]> normals;
    public List<int[]> faces;

    public Model(List<float[]> vertices, List<float[]> textures, List<float[]> normals, List<int[]> faces) {
        this.vertices = vertices;
        this.textures = textures;
        this.normals = normals;
        this.faces = faces;
    }
    
    
}