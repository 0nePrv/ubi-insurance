import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    // Конвенции сборки живут в отдельной included build, а не в buildSrc:
    // изменение build-logic не инвалидирует конфигурацию всех проектов разом.
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Автоматически скачивает нужный JDK по toolchain, если локально его нет
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // Репозитории объявляются только здесь. Модуль, который попробует объявить свой, уронит сборку.
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

// Даёт типобезопасные ссылки вида projects.libs.messaging вместо project(":libs:messaging")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ubi-insurance"

include(
    // Платформы (BOM): единая точка управления версиями
    ":platform:main",
    ":platform:gateway",

    // Контракты: protobuf для gRPC и событий Kafka
    ":contracts",

    // Технические библиотеки без доменной логики
    ":libs:messaging",
    ":libs:test-support",

    // Сервисы
    ":services:gateway",
    ":services:policy",
    ":services:rating",
    ":services:telematics",
    ":services:claims",
    ":services:billing",

    // Вспомогательные приложения для локального запуска и демо
    ":tools:vehicle-simulator",
    ":tools:payment-stub",
)
