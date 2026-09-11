// Телематика: приём потока (gRPC), поездки и эпизоды (Kafka Streams), скоринг водителя.
// Кандидат на выделение ingest в отдельный лёгкий сервис на Ktor или Quarkus, когда появится нагрузка.
plugins {
    id("ubi.spring-boot-app")
}

dependencies {
    implementation(platform(projects.platform.main))
    implementation(projects.contracts)
    implementation(projects.libs.messaging)

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.springframework.boot:spring-boot-starter-grpc-server")
    // Этап 3: ClickHouse JDBC и ONNX Runtime для модели скоринга

    testImplementation(projects.libs.testSupport)
}
