// Запускаемый Spring Boot сервис.
plugins {
    id("ubi.java-conventions")
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.tngtech.archunit:archunit-junit5")
}

springBoot {
    // build-info.properties -> /actuator/info покажет версию и время сборки
    buildInfo()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName = "${project.name}.jar"
}

// Обычный (не fat) jar сервису не нужен и только путает при сборке образа
tasks.named<Jar>("jar") {
    enabled = false
}
