import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "datahub"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

dependencies {
  implementation(project(":libs:common-core"))
  implementation(project(":libs:common-infrastructure"))
  implementation(project(":libs:common-outbox"))
  implementation(project(":libs:common-saga"))
  implementation(project(":libs:messaging-common"))

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  implementation("org.springframework.kafka:spring-kafka")
  testImplementation("org.springframework.kafka:spring-kafka-test")

  implementation("com.fasterxml.jackson.core:jackson-databind")

  // JSON 형식 로깅을 위한 Logstash 인코더 (프로덕션 환경 로그 수집용)
  implementation("net.logstash.logback:logstash-logback-encoder:8.0")

  runtimeOnly("com.mysql:mysql-connector-j")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
}

tasks.named<BootJar>("bootJar") {
  enabled = true
}

tasks.named<Jar>("jar") {
  enabled = false
}
