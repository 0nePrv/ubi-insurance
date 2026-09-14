package dev.ubi.policy.adapter.out.persistence;

import dev.ubi.policy.adapter.out.persistence.serde.PolicyEventSerde;
import dev.ubi.policy.adapter.out.persistence.serde.SerializedEvent;
import dev.ubi.policy.application.ConcurrentPolicyModificationException;
import dev.ubi.policy.application.port.out.PolicyRepository;
import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.event.PolicyEvent;
import dev.ubi.policy.domain.vo.PolicyId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPolicyRepository implements PolicyRepository {

    private static final String SELECT_STREAM = """
            select event_type, schema_version, payload, effective_at
            from policy_events
            where stream_id = :streamId
            order by version
            """;

    private static final String INSERT_EVENT = """
            insert into policy_events (stream_id, version, event_type, schema_version, payload, effective_at, recorded_at)
            values (:streamId, :version, :eventType, :schemaVersion, cast(:payload as jsonb), :effectiveAt, :recordedAt)
            """;

    private final JdbcClient jdbcClient;
    private final PolicyEventSerde serde;

    public JdbcPolicyRepository(JdbcClient jdbcClient, PolicyEventSerde serde) {
        this.jdbcClient = jdbcClient;
        this.serde = serde;
    }

    @Override
    public Optional<Policy> findById(PolicyId id) {
        var events = jdbcClient.sql(SELECT_STREAM)
            .param("streamId", id.value())
            .query((rs, _) -> serde.deserialize(new SerializedEvent(
                rs.getString("event_type"),
                rs.getInt("schema_version"),
                rs.getString("payload"),
                rs.getObject("effective_at", OffsetDateTime.class).toInstant())))
            .list();

        return events.isEmpty() ? Optional.empty() : Optional.of(Policy.rehydrate(events));
    }

    /**
     * Версия события — его порядковый номер в потоке, а не номер команды:
     * при expectedVersion = 5 три события получают 6, 7 и 8.
     * Конкурентная транзакция, стартовавшая с той же версии, упрётся
     * в unique (stream_id, version) — это и есть optimistic concurrency.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void append(PolicyId id, int expectedVersion, List<PolicyEvent> newEvents, Instant recordedAt) {
        var recordedAtOffset = recordedAt.atOffset(ZoneOffset.UTC);
        var version = expectedVersion;

        try {
            for (var event : newEvents) {
                var stored = serde.serialize(event);
                jdbcClient.sql(INSERT_EVENT)
                    .param("streamId", id.value())
                    .param("version", ++version)
                    .param("eventType", stored.eventType())
                    .param("schemaVersion", stored.schemaVersion())
                    .param("payload", stored.payload())
                    .param("effectiveAt", stored.effectiveAt().atOffset(ZoneOffset.UTC))
                    .param("recordedAt", recordedAtOffset)
                    .update();
            }
        } catch (DuplicateKeyException e) {
            throw new ConcurrentPolicyModificationException(id, version, e);
        }
    }
}
