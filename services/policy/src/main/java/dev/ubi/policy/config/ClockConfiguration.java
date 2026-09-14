package dev.ubi.policy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class ClockConfiguration {

    /**
     * UTC, а не системный пояс: сервер может стоять где угодно, а recordedAt должен
     * означать одно и то же. Часовой пояс, где он нужен по делу, живёт в Term.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
