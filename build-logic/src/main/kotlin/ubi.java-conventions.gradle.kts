// Базовая конвенция для любого JVM-модуля.
// ВАЖНО: платформу (BOM) она не подключает. Каждый модуль сам объявляет,
// на какой платформе он живёт: platform(projects.platform.main) или platform(projects.platform.gateway).
plugins {
    java
}

group = "dev.ubi"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // -parameters нужен Spring для имён параметров без @RequestParam("name") и т.п.
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all,-processing,-serial"))
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter")
    // С Gradle 9 launcher нужно объявлять явно
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // Mockito и ByteBuddy подключаются как агенты: без флага JDK 21+ пишет предупреждения
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
