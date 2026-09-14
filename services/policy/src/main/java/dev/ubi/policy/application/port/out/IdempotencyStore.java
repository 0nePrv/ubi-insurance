package dev.ubi.policy.application.port.out;

import java.util.Optional;

public interface IdempotencyStore {

    Optional<IdempotencyRecord> claim(String key, String requestHash);

    void complete(String key, int responseCode, String responseBody);

    record IdempotencyRecord(Status status, Integer responseCode, String responseBody) {
        public enum Status { IN_PROGRESS, COMPLETED }
    }
}
