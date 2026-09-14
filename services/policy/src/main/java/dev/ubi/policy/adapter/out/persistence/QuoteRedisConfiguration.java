package dev.ubi.policy.adapter.out.persistence;

import dev.ubi.policy.adapter.out.serde.PolicyDomainModule;
import dev.ubi.policy.domain.Quote;
import dev.ubi.policy.domain.vo.QuoteId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class QuoteRedisConfiguration {

    @Bean
    public RedisOperations<QuoteId, Quote> quoteRedisTemplate(RedisConnectionFactory factory,
                                                              RedisSerializer<QuoteId> quoteIdSerializer) {
        var mapper = JsonMapper.builder()
            .addModule(new PolicyDomainModule())
            .build();
        var template = new RedisTemplate<QuoteId, Quote>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(quoteIdSerializer);
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(mapper, Quote.class));
        return template;
    }
}
