import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "common-core"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
}

tasks.named<BootJar>("bootJar") {
  enabled = false
}

tasks.named<Jar>("jar") {
  enabled = true
}
