// Spring Boot сервис на Kotlin (rating, vehicle-simulator).
plugins {
    id("ubi.spring-boot-app")
    id("org.jetbrains.kotlin.jvm")
    // Делает классы с @Component/@Configuration/@Transactional open: иначе CGLIB-прокси не создать
    id("org.jetbrains.kotlin.plugin.spring")
}

kotlin {
    compilerOptions {
        // Nullability-аннотации Spring становятся частью системы типов Kotlin
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Boot 4 = Jackson 3, у которого новые координаты tools.jackson.*
    implementation("tools.jackson.module:jackson-module-kotlin")
}
