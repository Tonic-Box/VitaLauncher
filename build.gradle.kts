plugins {
    id("java")
    kotlin("jvm") version "1.9.24"
}

group = "com.tonic.launcher"
version = "1.0-SNAPSHOT"

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

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}