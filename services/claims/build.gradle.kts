// Убытки: FNOL, сага-оркестратор урегулирования, LLM-триаж (Spring AI, этап 4).
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
    // Этап 4: org.springframework.ai:spring-ai-starter-model-* (провайдер LLM)
    //         и org.springframework.ai:spring-ai-starter-vector-store-pgvector (RAG по условиям полиса).
    //         Версии уже управляются Spring AI BOM в :platform:main.

    testImplementation(projects.libs.testSupport)
}
