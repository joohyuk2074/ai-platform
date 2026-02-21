import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "datarex"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  implementation(project(":libs:common-core"))
  implementation(project(":libs:common-infrastructure"))
  implementation(project(":libs:messaging-common"))
  implementation(project(":libs:common-outbox"))
  implementation(project(":libs:common-saga"))

  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-webmvc")

  implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0"))
  implementation("org.springframework.ai:spring-ai-openai")
  implementation("org.springframework.ai:spring-ai-markdown-document-reader")

  implementation("org.springframework.kafka:spring-kafka")
  testImplementation("org.springframework.kafka:spring-kafka-test")

  compileOnly("org.projectlombok:lombok")

  annotationProcessor("org.projectlombok:lombok")
}

tasks.named<BootJar>("bootJar") {
  enabled = true
}

tasks.named<Jar>("jar") {
  enabled = false
}
