plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    kotlin("jvm") version "1.9.24"
}

group = "com.tonic.launcher"
version = "1.9"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("org.apache.commons:commons-configuration2:2.10.1")
    implementation("commons-beanutils:commons-beanutils:1.11.0")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    build {
        finalizedBy("shadowJar")
    }

    jar {
        manifest {
            attributes(mutableMapOf("Main-Class" to "com.tonic.launcher.LauncherMain"))
        }
    }

    shadowJar {
        archiveClassifier.set("shaded")
        isZip64 = true

        manifest {
            attributes(
                "Main-Class" to "com.tonic.launcher.LauncherMain",
                "Implementation-Version" to project.version,
                "Implementation-Title" to "VitaLauncher",
                "Implementation-Vendor" to "Tonic",
                "Multi-Release" to "true"
            )
        }
    }
}

kotlin {
    jvmToolchain(11)
}