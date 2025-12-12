plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("kapt")

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("checkstyle")
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("io.minio:minio:8.5.13")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(project(":api-lib"))
    implementation(project(":event-contracts"))
    implementation("org.springframework.kafka:spring-kafka")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.apache.commons:commons-pool2")
    implementation("org.liquibase:liquibase-core")

    implementation("org.mapstruct:mapstruct:1.6.3")
    kapt("org.mapstruct:mapstruct-processor:1.6.3")

    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-web")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("io.confluent:kafka-avro-serializer:7.6.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
    }
}

checkstyle {
    toolVersion = "10.15.0"
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
    maxWarnings = 0
    isIgnoreFailures = false
}

tasks.named("check") {
    dependsOn("checkstyleMain", "checkstyleTest")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

