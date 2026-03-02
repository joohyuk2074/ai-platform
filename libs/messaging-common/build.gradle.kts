import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  `java-library`
}

description = "messaging-common"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  implementation(project(":libs:common-core"))

  // Spring Kafka
  implementation("org.springframework.kafka:spring-kafka")

  // Jackson for serialization
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

  // Lombok
  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
}

tasks.named<BootJar>("bootJar") {
  enabled = false
}

tasks.named<Jar>("jar") {
  enabled = true
}
