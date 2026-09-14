package dev.ubi.policy.application;

import dev.ubi.policy.domain.vo.PolicyId;
import org.springframework.dao.DuplicateKeyException;

public class ConcurrentPolicyModificationException extends RuntimeException {

    private final PolicyId id;
    private final int conflictingVersion;

    public ConcurrentPolicyModificationException(PolicyId id, int conflictingVersion, DuplicateKeyException e) {
        super("For policy with id %s version %s already exists".formatted(id, conflictingVersion), e);
        this.id = id;
        this.conflictingVersion = conflictingVersion;
    }

    public PolicyId id() {
        return id;
    }

    public int conflictingVersion() {
        return conflictingVersion;
    }
}
