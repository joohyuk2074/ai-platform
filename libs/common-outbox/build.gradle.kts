import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  `java-library`
}

description = "common-outbox"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  api("org.springframework.boot:spring-boot-starter-data-jpa")
  api("org.springframework.kafka:spring-kafka")

  implementation("com.fasterxml.jackson.core:jackson-databind")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
}

tasks.named<BootJar>("bootJar") {
  enabled = false
}

tasks.named<Jar>("jar") {
  enabled = true
}
