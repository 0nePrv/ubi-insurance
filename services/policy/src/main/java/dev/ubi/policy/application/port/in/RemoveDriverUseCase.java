package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.vo.DriverId;
import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public interface RemoveDriverUseCase {

    void removeDriver(RemoveDriverCommand command);

    record RemoveDriverCommand(PolicyId policyId, DriverId driverId, Instant effectiveAt) {

    }
}
