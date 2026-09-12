package dev.ubi.policy;

import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.event.PolicyDrafted;
import dev.ubi.policy.domain.event.PolicyIssued;
import dev.ubi.policy.domain.event.PremiumAdjusted;
import dev.ubi.policy.domain.vo.*;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyTest {

    @Test
    void premiumJumpIsClampedTo20Percent() {
        LocalDate localDate = LocalDate.of(2026, Month.JUNE, 1);
        var zonedDateTime = ZonedDateTime.of(localDate.atStartOfDay(), ZoneOffset.UTC);
        var instant = zonedDateTime.toInstant();
        var premium = Money.ofMinorRub(10_000);
        var proposed = Money.ofMinorRub(10_500);
        var policy = Policy.rehydrate(List.of(
            Fixtures.drafted(instant, new Term(zonedDateTime, TermMode.YEAR), premium),
            Fixtures.issued(instant))
        );
        var events = policy.adjustPremium(proposed, instant, "AUTO-UBI:3", instant.plus(Period.ofDays(1)));

        assertThat(events).singleElement()
            .isInstanceOfSatisfying(PremiumAdjusted.class, e -> {
                assertThat(e.proposed()).isEqualTo(proposed);
                assertThat(e.applied()).isEqualTo(proposed);
            });
    }

    private interface Fixtures {

        PolicyId ID = new PolicyId(UUID.randomUUID());
        Vehicle VEHICLE = new Vehicle(Vin.of("A".repeat(17)), 100, 2000, "777");
        DriverId DRIVER_ID = new DriverId(UUID.randomUUID());
        Driver DRIVER = new Driver(
            "John", "Doe", LocalDate.of(2000, Month.APRIL, 3),
            new DrivingLicense(), LocalDate.of(2018, Month.APRIL, 3)
        );

        static PolicyDrafted drafted(Instant effectiveAt, Term term, Money premium) {
            return new PolicyDrafted(ID, effectiveAt, term, premium, VEHICLE, Map.of(DRIVER_ID, DRIVER));
        }

        static PolicyIssued issued(Instant effectiveAt) {
            return new PolicyIssued(ID, effectiveAt);
        }
    }
}
