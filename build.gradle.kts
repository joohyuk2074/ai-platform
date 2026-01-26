plugins {
  java
  id("org.springframework.boot") version "4.0.2" apply false
  id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "me.joohyuk"
version = "0.0.1-SNAPSHOT"
description = "ai-platform"

subprojects {
  apply(plugin = "java")
  apply(plugin = "org.springframework.boot")
  apply(plugin = "io.spring.dependency-management")

  group = "me.joohyuk"
  version = "0.0.1-SNAPSHOT"

  java {
    toolchain {
      languageVersion = JavaLanguageVersion.of(25)
    }
  }

  repositories {
    mavenCentral()
  }

  dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}
