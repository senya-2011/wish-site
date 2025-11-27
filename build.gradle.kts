plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.6.0"
}

group = "com.dabwish"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	//Database dependencies
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("org.springframework.data:spring-data-jpa:4.0.0")
    implementation("org.liquibase:liquibase-core:5.0.1")
    implementation("org.springframework.boot:spring-boot-starter-liquibase:4.0.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    //mapper
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    //openapi
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
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

tasks.named("compileKotlin") {
	dependsOn("openApiGenerate")
}
