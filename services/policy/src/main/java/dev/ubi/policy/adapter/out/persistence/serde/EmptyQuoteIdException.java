package dev.ubi.policy.adapter.out.persistence.serde;

import org.springframework.data.redis.serializer.SerializationException;

public class EmptyQuoteIdException extends SerializationException {

    public EmptyQuoteIdException() {
        super("Quote id is empty");
    }
}
