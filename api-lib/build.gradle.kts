plugins {
    kotlin("jvm")
    id("org.openapi.generator")
}

val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("org.springframework:spring-webmvc:6.1.5")
    implementation("org.springframework:spring-context:6.1.5")
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("io.swagger.core.v3:swagger-models:2.2.22")
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(projectDir.resolve("openapi.yml").toURI().toString())
    outputDir.set(openApiOutputDir.get().asFile.absolutePath)
    apiPackage.set("com.dabwish.dabwish.generated.api")
    modelPackage.set("com.dabwish.dabwish.generated.dto")
    typeMappings.put("string+binary", "MultipartFile")
    importMappings.put("MultipartFile", "org.springframework.web.multipart.MultipartFile")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useBeanValidation" to "true",
            "useSpringBoot3" to "true",
            "dateLibrary" to "java8",
            "library" to "spring-boot",
            "reactive" to "false"
        )
    )
}

sourceSets {
    named("main") {
        kotlin.srcDir(openApiOutputDir.map { it.dir("src/main/kotlin").asFile }.get())
        resources.srcDir(openApiOutputDir.map { it.dir("src/main/resources").asFile }.get())
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("openApiGenerate")
}

