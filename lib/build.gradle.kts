plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.lwjgl:lwjgl:3.3.1")
    implementation ("org.lwjgl:lwjgl-glfw:3.3.1")
    implementation ("org.lwjgl:lwjgl-glu:3.3.1")
    implementation ("org.lwjgl:lwjgl-opengl:3.3.1")
    implementation("org.joml:joml:1.10.5")
    
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.6.0")
	
    implementation("org.lwjgl:lwjgl-stb:3.3.1")
    implementation("org.lwjgl:lwjgl-jemalloc:3.3.1")

    runtimeOnly("org.lwjgl:lwjgl:3.3.1:natives-windows")
    
    runtimeOnly ("org.lwjgl:lwjgl:3.3.1:natives-windows")
    runtimeOnly ("org.lwjgl:lwjgl-glfw:3.3.1:natives-windows")
    runtimeOnly ("org.lwjgl:lwjgl-opengl:3.3.1:natives-windows")
    
    //iMonkeyEngine
    
    implementation("org.jmonkeyengine:jme3-core:3.3.2-stable")
    implementation("org.jmonkeyengine:jme3-desktop:3.3.2-stable")
    implementation("org.jmonkeyengine:jme3-lwjgl:3.3.2-stable")
    implementation("org.jmonkeyengine:jme3-lwjgl3:3.3.2-stable")
    implementation("org.jmonkeyengine:jme3-effects:3.3.2-stable")
    implementation("org.jmonkeyengine:jme3-jogg:3.3.2-stable")
    implementation("org.jmonkeyengine:jme3-core:3.6.1-stable")
  
	
}
