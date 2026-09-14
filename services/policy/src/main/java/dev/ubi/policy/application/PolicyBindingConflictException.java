package dev.ubi.policy.application;

public class PolicyBindingConflictException extends RuntimeException {

    private final String key;

    public PolicyBindingConflictException(String key) {
        super("policy binding conflicts with existing for key %s".formatted(key));
        this.key = key;
    }

    public String key() {
        return key;
    }
}
