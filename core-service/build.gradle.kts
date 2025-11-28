plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    kotlin("kapt") version "2.0.21"

    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.openapi.generator") version "7.6.0"
}

group = "com.dabwish"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// Переменная для пути генерации
val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")

dependencies {
    // --- Spring Boot Core ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // --- Database ---
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.liquibase:liquibase-core")

    // --- MapStruct (KAPT) ---
    implementation("org.mapstruct:mapstruct:1.6.3")
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    // --- OpenAPI ---
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api")

    // --- Password Hashing ---
    implementation("org.springframework.security:spring-security-crypto:6.3.3")

    // --- DevTools ---
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
    }
}


openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/openapi/openapi.yml")
    outputDir.set(openApiOutputDir.get().asFile.absolutePath)
    apiPackage.set("com.dabwish.dabwish.generated.api")
    modelPackage.set("com.dabwish.dabwish.generated.dto")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useBeanValidation" to "true",
            "useSpringBoot3" to "true",
            "dateLibrary" to "java8"
        )
    )
}

sourceSets {
    named("main") {
        kotlin.srcDir(openApiOutputDir.map { it.dir("src/main/kotlin").asFile }.get())
        resources.srcDir(openApiOutputDir.map { it.dir("src/main/resources").asFile }.get())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("openApiGenerate")
}

tasks.named("processResources") {
    dependsOn("openApiGenerate")
}