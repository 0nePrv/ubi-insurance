plugins {
    `kotlin-dsl`
}

dependencies {
    // Плагины, которые применяются внутри конвенций. Версия задаётся здесь один раз,
    // поэтому в модулях они подключаются как id("org.springframework.boot") без версии.
    implementation(libs.spring.boot.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.allopen)
}
