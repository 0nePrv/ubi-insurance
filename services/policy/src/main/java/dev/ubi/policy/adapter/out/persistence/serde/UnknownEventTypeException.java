package dev.ubi.policy.adapter.out.persistence.serde;

public class UnknownEventTypeException extends RuntimeException {

    private final String eventType;

    public UnknownEventTypeException(String eventType) {
        super("Unknown event type in store: %s".formatted(eventType));
        this.eventType = eventType;
    }

    public String eventType() {
        return eventType;
    }
}
