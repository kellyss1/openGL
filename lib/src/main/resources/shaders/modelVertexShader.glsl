#version 330 core

layout(location = 0) in vec3 aPos;  // Posición del vértice
layout(location = 1) in vec3 aNormal;  // Normal del vértice

out vec3 FragPos;  // Posición del fragmento en el espacio del mundo
out vec3 Normal;   // Normal del fragmento

uniform mat4 model;   // Matriz de modelo
uniform mat4 view;    // Matriz de vista
uniform mat4 projection;  // Matriz de proyección

void main()
{
    FragPos = vec3(model * vec4(aPos, 1.0));  // Posición del fragmento en el espacio del mundo
    Normal = mat3(transpose(inverse(model))) * aNormal;  // Normal en el espacio del mundo
    gl_Position = projection * view * model * vec4(aPos, 1.0);  // Posición final del vértice
}
