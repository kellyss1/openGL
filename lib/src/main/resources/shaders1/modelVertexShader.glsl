#version 330 core

layout(location = 0) in vec3 position; // Posición del vértice
layout(location = 1) in vec3 normal;   // Normal del vértice
layout(location = 2) in vec3 color;    // Color del vértice (nuevo atributo)

uniform mat4 modelMatrix; // Matriz del modelo
uniform mat4 projectionMatrix; // Matriz de proyección

out vec3 fragPos;        // Posición del fragmento en el espacio del modelo
out vec3 fragNormal;    // Normal del fragmento
out vec3 fragColor;     // Color del fragmento

void main() {
    fragPos = vec3(modelMatrix * vec4(position, 1.0)); // Posición del fragmento en el espacio del modelo
    fragNormal = normal;                            // Normal del fragmento
    fragColor = color;                             // Color del fragmento

    // Calcular la posición final del fragmento en la pantalla
    gl_Position = projectionMatrix * vec4(fragPos, 1.0);
}
