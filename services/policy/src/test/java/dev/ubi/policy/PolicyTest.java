package dev.ubi.policy;

import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.event.PolicyDrafted;
import dev.ubi.policy.domain.event.PolicyIssued;
import dev.ubi.policy.domain.event.PremiumAdjusted;
import dev.ubi.policy.domain.vo.PolicyId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyTest {

    @Test
    void premiumJumpIsClampedTo20Percent() {
        PolicyId policyId = Fixtures.newPolicyId();
        var policy = Policy.rehydrate(List.of(
            new PolicyDrafted(policyId, Fixtures.RECORDED_AT, Fixtures.TERM, Fixtures.PREMIUM, Fixtures.VEHICLE, Map.of()),
            new PolicyIssued(policyId, Fixtures.RECORDED_AT)
        ));
        var events = policy.adjustPremium(Fixtures.PROPOSED_PREMIUM, Fixtures.RECORDED_AT.plus(Duration.ofDays(20)), "AUTO-UBI:3", Fixtures.RECORDED_AT.plus(Duration.ofDays(20)));

        assertThat(events).singleElement()
            .isInstanceOfSatisfying(PremiumAdjusted.class, e -> {
                assertThat(e.proposed()).isEqualTo(Fixtures.PROPOSED_PREMIUM);
                assertThat(e.applied()).isEqualTo(Fixtures.CLAMPED_PREMIUM);
            });
    }
}
