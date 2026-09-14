package dev.ubi.policy.application.port.out;

import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.event.PolicyEvent;
import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PolicyRepository {

    Optional<Policy> findById(PolicyId id);

    /**
     * @param expectedVersion версия агрегата на момент загрузки, ДО выполнения команды
     * @throws dev.ubi.policy.application.ConcurrentPolicyModificationException если поток изменился с тех пор
     */
    void append(PolicyId id, int expectedVersion, List<PolicyEvent> newEvents, Instant recordedAt);

    default void append(Policy existingPolicy, List<PolicyEvent> events, Instant recordedAt) {
        append(existingPolicy.id(), existingPolicy.loadedVersion(), events, recordedAt);
    }
}
