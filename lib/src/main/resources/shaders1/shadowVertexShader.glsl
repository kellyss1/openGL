#version 330 core

layout(location = 0) in vec3 position; // Posición del vértice

uniform mat4 lightSpaceMatrix; // Matriz de proyección y vista de la luz
uniform mat4 modelMatrix; // Matriz del modelo

out vec4 FragPosLightSpace; // Posición del fragmento en el espacio de la luz

void main() {
    // Calcular la posición del vértice en el espacio de la luz
    FragPosLightSpace = lightSpaceMatrix * modelMatrix * vec4(position, 1.0);

    // La posición final del vértice en la pantalla es innecesaria para la sombra
    gl_Position = FragPosLightSpace; 
}
