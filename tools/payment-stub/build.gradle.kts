// Имитация внешнего платёжного провайдера: принимает платёж и присылает webhook в billing.
// Умеет «ронять» и дублировать webhook — чтобы inbox в billing работал по делу.
plugins {
    id("ubi.spring-boot-app")
}

dependencies {
    implementation(platform(projects.platform.main))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
}
