// Внутренняя библиотека (libs/*, contracts). Всегда живёт на основной платформе.
plugins {
    id("ubi.java-conventions")
    `java-library`
}

dependencies {
    api(platform(project(":platform:main")))
    // annotationProcessor не наследует зависимости implementation/api, платформу подключаем явно
    annotationProcessor(platform(project(":platform:main")))
}
