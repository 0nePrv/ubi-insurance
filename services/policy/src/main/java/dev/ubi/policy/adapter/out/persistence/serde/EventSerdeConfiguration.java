package dev.ubi.policy.adapter.out.persistence.serde;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class EventSerdeConfiguration {

    /**
     * Отдельный ObjectMapper для event store — НЕ бин Spring, а локальный объект.
     * Иначе он подменил бы автонастроенный маппер, которым Spring сериализует REST-ответы,
     * и представление в API поехало бы вслед за форматом хранения.
     *
     * <p>В Jackson 3 даты по умолчанию пишутся строками ISO-8601 (WRITE_DATES_AS_TIMESTAMPS
     * выключен), поэтому Instant и LocalDate настраивать не нужно — поддержка java.time
     * встроена в databind.
     */
    @Bean
    public PolicyEventSerde policyEventSerde() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new PolicyDomainModule())
                // Старый экземпляр сервиса не должен падать на поле, добавленном новым:
                // при раскатке обе версии какое-то время читают одну таблицу.
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        return new PolicyEventSerde(mapper);
    }
}
