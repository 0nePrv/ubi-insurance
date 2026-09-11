// Edge: маршрутизация, проверка JWT, rate limiting. Живёт на своей платформе (Boot 4.0 + Spring Cloud),
// см. docs/adr/0003. Реактивный стек здесь осознанно: gateway — чистый I/O без бизнес-логики.
plugins {
    id("ubi.spring-boot-app")
}

dependencies {
    implementation(platform(projects.platform.gateway))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    // RequestRateLimiter в WebFlux-гейтвее работает через Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
}
