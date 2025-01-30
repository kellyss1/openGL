#version 330 core

in vec4 FragPosLightSpace;

out vec4 FragColor;

uniform sampler2D shadowMap;
uniform vec3 lightPos;

float ShadowCalculation(vec4 fragPosLightSpace)
{
    // Coord de sombra y depth
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;  // Convertir a [0,1] range
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;

    // Cálculo de la sombra
    float shadow = currentDepth > closestDepth ? 0.5 : 1.0;  // Ajustar según sea necesario
    return shadow;
}

void main()
{
    float shadow = ShadowCalculation(FragPosLightSpace);
    FragColor = vec4(vec3(1.0) * shadow, 1.0);  // Color de sombra aplicado
}
