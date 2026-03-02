import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "vecdash"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  implementation(project(":libs:common-core"))

  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-webmvc")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
}

tasks.named<BootJar>("bootJar") {
  enabled = true
}

tasks.named<Jar>("jar") {
  enabled = false
}
