plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.spring") version "2.0.21" apply false
    kotlin("plugin.jpa") version "2.0.21" apply false
    kotlin("kapt") version "2.0.21" apply false

    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.openapi.generator") version "7.6.0" apply false
    id("com.github.davidmc24.gradle.plugin.avro") version "1.8.0" apply false
}


allprojects {
    group = "com.dabwish"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    if (name != "event-contracts") {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
    }
    pluginManager.apply("io.spring.dependency-management")

    pluginManager.withPlugin("java") {
        extensions.configure(JavaPluginExtension::class.java) {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }
}

