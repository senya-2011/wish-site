plugins {
    id("java-library")
    id("com.github.davidmc24.gradle.plugin.avro")
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api("org.apache.avro:avro:1.11.3")
}

avro {
    isCreateSetters = false
    outputCharacterEncoding = "UTF-8"
    fieldVisibility = "PRIVATE"
}

sourceSets.named("main") {
    java.srcDir(layout.buildDirectory.dir("generated/main/avro"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(tasks.named("generateAvroJava"))
}

tasks.named<Jar>("jar") {
    dependsOn(tasks.named("generateAvroJava"))
}

