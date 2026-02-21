import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  `java-library`
}

description = "common-infrastructure"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  implementation(project(":libs:common-core"))
  implementation("org.springframework.boot:spring-boot-autoconfigure")

  // Jackson for ObjectMapper
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
}

tasks.named<BootJar>("bootJar") {
  enabled = false
}

tasks.named<Jar>("jar") {
  enabled = true
}
