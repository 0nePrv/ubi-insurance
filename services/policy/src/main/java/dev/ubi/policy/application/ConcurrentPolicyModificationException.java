package dev.ubi.policy.application;

import dev.ubi.policy.domain.vo.PolicyId;
import org.springframework.dao.DuplicateKeyException;

public class ConcurrentPolicyModificationException extends RuntimeException {

    private final PolicyId id;
    private final int expectedVersion;

    public ConcurrentPolicyModificationException(PolicyId id, int expectedVersion, DuplicateKeyException e) {
        super("For policy with id %s expected version %s already exists".formatted(id, expectedVersion), e);
        this.id = id;
        this.expectedVersion = expectedVersion;
    }

    public PolicyId id() {
        return id;
    }

    public int expectedVersion() {
        return expectedVersion;
    }
}
