package dev.ubi.policy.adapter.out.persistence.serde;

public class UnsupportedSchemaVersionException extends RuntimeException {

    private final String eventType;
    private final int schemaVersion;

    public UnsupportedSchemaVersionException(String eventType, int schemaVersion) {
        super("No reader for %s schema version %d".formatted(eventType, schemaVersion));
        this.eventType = eventType;
        this.schemaVersion = schemaVersion;
    }

    public String eventType() {
        return eventType;
    }

    public int schemaVersion() {
        return schemaVersion;
    }
}
