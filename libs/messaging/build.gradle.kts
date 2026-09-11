// Технический стартер: transactional outbox (публикация) и inbox (идемпотентный приём).
// Устроен как Spring Boot starter: автоконфигурация + @ConfigurationProperties.
plugins {
    id("ubi.java-library")
}

dependencies {
    api("org.springframework.kafka:spring-kafka")
    api("org.springframework:spring-jdbc")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(projects.libs.testSupport)
}
