package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.vo.Driver;
import dev.ubi.policy.domain.vo.DriverId;
import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public interface UpdateDriverDetailsUseCase {

    void updateDriverDetails(UpdateDriverDetailsCommand command);

    record UpdateDriverDetailsCommand(PolicyId policyId, DriverId driverId, Driver driver, Instant effectiveAt) {

    }
}
