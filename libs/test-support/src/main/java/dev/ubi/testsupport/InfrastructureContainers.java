package dev.ubi.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Подключение в интеграционном тесте сервиса:
 * <pre>{@code
 * @SpringBootTest
 * @Import(InfrastructureContainers.class)
 * class PolicyIntegrationTest { ... }
 * }</pre>
 * {@link ServiceConnection} сам пробрасывает URL, логин и пароль в datasource и bootstrap-servers Kafka.
 * Версии образов совпадают с infra/local/compose.yaml.
 */
@TestConfiguration(proxyBeanMethods = false)
public class InfrastructureContainers {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        // Тот же образ, что и локально: Postgres 17 + расширение pgvector
        var image = DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres");
        return new PostgreSQLContainer(image);
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafka() {
        return new KafkaContainer("apache/kafka:4.2.1");
    }
}
