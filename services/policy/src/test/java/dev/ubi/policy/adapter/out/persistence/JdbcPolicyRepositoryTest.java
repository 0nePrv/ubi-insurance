package dev.ubi.policy.adapter.out.persistence;

import dev.ubi.policy.adapter.out.persistence.serde.EventSerdeConfiguration;
import dev.ubi.policy.application.ConcurrentPolicyModificationException;
import dev.ubi.policy.application.port.out.PolicyRepository;
import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.vo.*;
import dev.ubi.testsupport.InfrastructureContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static dev.ubi.policy.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@Import({JdbcPolicyRepository.class, EventSerdeConfiguration.class, InfrastructureContainers.class})
class JdbcPolicyRepositoryTest {

    @Autowired
    private PolicyRepository repository;

    @Autowired
    private JdbcClient client;

    @Autowired
    private TransactionOperations transactionTemplate;

    @Test
    void savesAndRestoresDraftedPolicy() {
        var id = new PolicyId(UUID.randomUUID());
        var recordedAt = LocalDate.of(2020, Month.JANUARY, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var term = new Term(ZonedDateTime.ofInstant(recordedAt.plus(Period.ofDays(1)), ZoneOffset.UTC), TermMode.YEAR);
        var premium = Money.ofMinorRub(10_000);
        Map<DriverId, Driver> drivers = singleDriver(newDriverId());
        var drafted = Policy.draft(id, term, premium, VEHICLE, drivers, recordedAt);

        repository.append(id, 0, drafted, recordedAt);

        assertThat(repository.findById(id)).isPresent().hasValueSatisfying(p -> {
            assertThat(p.id()).isEqualTo(id);
            assertThat(p.version()).isEqualTo(1);
            assertThat(p.status()).isEqualTo(PolicyStatus.DRAFT);
            assertThat(p.term()).isEqualTo(term);
            assertThat(p.monthlyPremium()).isEqualTo(premium);
            assertThat(p.vehicle()).isEqualTo(VEHICLE);
            assertThat(p.drivers()).containsExactlyInAnyOrderElementsOf(drivers.values());
        });
    }

    @Test
    void returnsEmptyForUnknownPolicy() {
        assertThat(repository.findById(newPolicyId())).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentAppendFromSameVersionFailsForOneWriter() throws Exception {
        var id = transactionTemplate.execute(_ -> givenIssuedPolicy());                 // фикстура: draft + issue, в потоке 2 события
        var barrier = new CyclicBarrier(2);
        var executor = Executors.newFixedThreadPool(2);

        Callable<Optional<Exception>> writer = () -> {
            var policy = repository.findById(id).orElseThrow();
            var events = policy.addDriver(newDriverId(), BOB, RECORDED_AT.plus(Period.ofDays(20)), RECORDED_AT);
            barrier.await(5, TimeUnit.SECONDS);
            try {
                transactionTemplate.executeWithoutResult(_ -> repository.append(policy, events, RECORDED_AT));
                return Optional.empty();
            } catch (Exception e) {
                return Optional.of(e);
            }
        };

        var results = executor.invokeAll(List.of(writer, writer), 15, TimeUnit.SECONDS);
        executor.shutdownNow();

        var failures = results.stream().map(JdbcPolicyRepositoryTest::get).flatMap(Optional::stream).toList();

        assertThat(failures).singleElement().isInstanceOf(ConcurrentPolicyModificationException.class);
        assertThat(repository.findById(id).orElseThrow().version()).isEqualTo(3);
    }

    @Test
    void sequentialOrderAfterMultipleCommands() {
        var id = givenIssuedPolicy();
        var policy = repository.findById(id).orElseThrow();
        var rejected = policy.cancel("Reason", RECORDED_AT.plus(Period.ofDays(20)), RECORDED_AT);
        repository.append(policy, rejected, RECORDED_AT);
        List<Integer> versions = client.sql("select version from policy_events where stream_id = :id order by version")
            .param("id", id.value())
            .query(Integer.class)
            .list();
        assertThat(versions).doesNotHaveDuplicates().doesNotContainNull();
        int version = versions.getFirst();
        for (int i = 1; i < versions.size(); i++) {
            assertThat(versions.get(i)).isEqualTo(version + 1);
            version = versions.get(i);
        }
    }

    private PolicyId givenIssuedPolicy() {
        var id = newPolicyId();
        var drafted = Policy.draft(id, TERM, PREMIUM, VEHICLE, singleDriver(newDriverId()), RECORDED_AT);
        repository.append(id, 0, drafted, RECORDED_AT);
        var draftedPolicy = repository.findById(id).orElseThrow();
        repository.append(draftedPolicy, draftedPolicy.issue(RECORDED_AT), RECORDED_AT);
        return id;
    }

    private static <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
