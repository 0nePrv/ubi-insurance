// Тарификация: версионированные тарифы на Kotlin DSL + таблицы коэффициентов в БД. Stateless, наружу только gRPC.
plugins {
    id("ubi.kotlin-spring-app")
}

dependencies {
    implementation(platform(projects.platform.main))
    implementation(projects.contracts)
    implementation("org.springframework.boot:spring-boot-starter-webmvc") // actuator и админ-API публикации тарифов
    implementation("org.springframework.boot:spring-boot-starter-grpc-server")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
}
