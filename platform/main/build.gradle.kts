// Основная платформа: Spring Boot 4.1 + Spring AI. На ней живут все сервисы, кроме gateway.
plugins {
    `java-platform`
}

javaPlatform {
    // Разрешает импортировать другие BOM внутрь этой платформы
    allowDependencies()

}

dependencies {
    api(platform(libs.spring.boot.bom))
    api(platform(libs.spring.ai.bom))

    constraints {
        api(libs.archunit.junit5)
    }
}
