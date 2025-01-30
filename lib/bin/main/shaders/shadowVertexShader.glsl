#version 330 core

layout(location = 0) in vec3 aPos;

out vec4 FragPosLightSpace;

uniform mat4 model;
uniform mat4 lightSpaceMatrix;  // Matriz de proyección y vista de la luz

void main()
{
    vec4 worldPosition = model * vec4(aPos, 1.0);
    FragPosLightSpace = lightSpaceMatrix * worldPosition;
    gl_Position = worldPosition;  // Solo transformamos para el espacio de la luz
}
