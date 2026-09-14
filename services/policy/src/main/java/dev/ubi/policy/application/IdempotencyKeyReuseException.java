package dev.ubi.policy.application;

public class IdempotencyKeyReuseException extends RuntimeException {

    public IdempotencyKeyReuseException(String message) {
        super(message);
    }
}
