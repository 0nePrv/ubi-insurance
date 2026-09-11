// Биллинг: счета, платежи, выплаты, леджер с двойной записью (идеи Ledgerflow).
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

    testImplementation(projects.libs.testSupport)
}
