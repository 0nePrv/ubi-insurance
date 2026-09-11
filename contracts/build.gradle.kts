import com.google.protobuf.gradle.id

// Конфигурация protobuf нужна ровно одному модулю, поэтому она здесь, а не в build-logic.
// Конвенция ради одного потребителя — лишняя абстракция.
plugins {
    id("ubi.java-library")
    alias(libs.plugins.protobuf)
}

dependencies {
    // api, а не implementation: сгенерированные классы торчат в публичных сигнатурах сервисов
    api("com.google.protobuf:protobuf-java")
    api("io.grpc:grpc-protobuf")
    api("io.grpc:grpc-stub")
    // Сгенерированный gRPC-код ссылается на @javax.annotation.Generated, которого нет в JDK 9+
    compileOnly(libs.grpc.annotations.api)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.asProvider().get()}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                id("grpc") { }
            }
        }
    }
}
