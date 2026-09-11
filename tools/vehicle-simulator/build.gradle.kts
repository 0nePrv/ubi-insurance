// Симулятор парка машин: N устройств шлют телеметрию в telematics по gRPC.
// REST-ручки управляют сценариями для демо: «агрессивный водитель», «авария».
plugins {
    id("ubi.kotlin-spring-app")
}

dependencies {
    implementation(platform(projects.platform.main))
    implementation(projects.contracts)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-grpc-client")
}
