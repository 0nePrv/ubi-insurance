package dev.ubi.policy.adapter.out.persistence;

import dev.ubi.policy.application.IdempotencyKeyReuseException;
import dev.ubi.policy.application.port.out.IdempotencyStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class JdbcIdempotencyStore implements IdempotencyStore {

    private static final String CLAIM = """
            insert into idempotency_key (key, request_hash, status)
            values (:key, :requestHash, 'IN_PROGRESS')
            on conflict do nothing
            """;

    private static final String SELECT_BY_KEY = """
            select request_hash, status, response_code, response_body
            from idempotency_key
            where key = :key
            """;

    private static final String COMPLETE = """
            update idempotency_key
            set status = 'COMPLETED', response_code = :responseCode,
                response_body = cast(:responseBody as jsonb)
            where key = :key
            """;

    private final JdbcClient client;

    public JdbcIdempotencyStore(JdbcClient client) {
        this.client = client;
    }

    /**
     * Захват ключа идёт в транзакции вызывающего: иначе при падении операции
     * в таблице останется мёртвый IN_PROGRESS, и ключ будет заблокирован навсегда.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<IdempotencyRecord> claim(String key, String requestHash) {
        var claimed = client.sql(CLAIM)
                          .param("key", key)
                          .param("requestHash", requestHash)
                          .update() == 1;

        if (claimed) {
            return Optional.empty();
        }

        var existing = client.sql(SELECT_BY_KEY)
            .param("key", key)
            .query((rs, _) -> new Existing(
                rs.getString("request_hash"),
                IdempotencyRecord.Status.valueOf(rs.getString("status")),
                rs.getObject("response_code", Integer.class),
                rs.getString("response_body")))
            .single();

        if (!existing.requestHash().equals(requestHash)) {
            throw new IdempotencyKeyReuseException(key);
        }

        return Optional.of(new IdempotencyRecord(
            existing.status(), existing.responseCode(), existing.responseBody()));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(String key, int responseCode, String responseBody) {
        client.sql(COMPLETE)
            .param("key", key)
            .param("responseCode", responseCode)
            .param("responseBody", responseBody)
            .update();
    }

    private record Existing(String requestHash, IdempotencyRecord.Status status, Integer responseCode, String responseBody) {
    }
}
