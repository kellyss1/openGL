#version 330 core

in vec3 FragPos;  // Posición del fragmento en el espacio del mundo
in vec3 Normal;   // Normal del fragmento

out vec4 FragColor;  // Color final del fragmento

uniform vec3 lightPos;  // Posición de la luz
uniform vec3 viewPos;   // Posición de la cámara

void main()
{
    // Configuración de iluminación
    vec3 ambient = 0.2 * vec3(1.0, 1.0, 1.0);  // Luz ambiental
    vec3 lightDir = normalize(lightPos - FragPos);  // Dirección de la luz
    vec3 norm = normalize(Normal);  // Normal del fragmento
    float diff = max(dot(norm, lightDir), 0.0);  // Componente difusa de la luz
    vec3 diffuse = diff * vec3(1.0, 1.0, 1.0);  // Luz difusa

    vec3 viewDir = normalize(viewPos - FragPos);  // Dirección de la vista
    vec3 reflectDir = reflect(-lightDir, norm);  // Dirección reflejada
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);  // Componente especular
    vec3 specular = 0.5 * spec * vec3(1.0, 1.0, 1.0);  // Luz especular

    vec3 result = ambient + diffuse + specular;  // Color final
    FragColor = vec4(result, 1.0);
}
