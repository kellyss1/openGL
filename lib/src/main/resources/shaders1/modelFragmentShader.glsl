#version 330 core

in vec3 fragColor; // Color del fragmento

out vec4 finalColor; // Color final de salida del fragmento

void main() {
    finalColor = vec4(fragColor, 1.0); // El color del fragmento con alpha 1.0
}
