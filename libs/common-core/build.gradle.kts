import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  `java-library`
}

description = "common-core"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  // Jackson for JSON serialization/deserialization (Passport)
  api("com.fasterxml.jackson.core:jackson-annotations")
}

tasks.named<BootJar>("bootJar") {
  enabled = false
}

tasks.named<Jar>("jar") {
  enabled = true
}
