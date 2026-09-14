package dev.ubi.policy.adapter.out.persistence.serde;

import dev.ubi.policy.domain.vo.QuoteId;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class QuoteSerdeConfiguration {

    public static final String KEY_PREFIX = "quote";

    @Bean
    public RedisSerializer<QuoteId> quoteIdSerializer() {
        return new RedisSerializer<>() {

            @Override
            public byte[] serialize(@Nullable QuoteId value) throws SerializationException {
                if (value == null) {
                    throw new EmptyQuoteIdException();
                }
                var key = KEY_PREFIX + ":" + value.value();
                return key.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public @Nullable QuoteId deserialize(byte @Nullable [] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                String key = new String(bytes, StandardCharsets.UTF_8);
                return new QuoteId(UUID.fromString(key.substring(KEY_PREFIX.length() + 1)));
            }
        };
    }
}
