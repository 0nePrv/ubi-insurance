// Отдельная платформа для edge: Spring Cloud Gateway пока требует Boot 4.0.x.
// Когда выйдет релиз Spring Cloud под Boot 4.1 — удалить модуль и перевести gateway на :platform:main.
// Подробности: docs/adr/0003-gateway-boot-version-split.md
plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.spring.boot.bom.gateway))
    api(platform(libs.spring.cloud.bom))

    constraints {
        api(libs.archunit.junit5)
    }
}
