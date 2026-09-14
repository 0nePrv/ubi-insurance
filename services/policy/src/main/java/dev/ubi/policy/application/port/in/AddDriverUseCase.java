package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.vo.Driver;
import dev.ubi.policy.domain.vo.DriverId;
import dev.ubi.policy.domain.vo.PolicyId;

import java.time.Instant;

public interface AddDriverUseCase {

    void addDriver(AddDriverCommand command);

    record AddDriverCommand(PolicyId policyId, DriverId driverId, Driver driver, Instant effectiveAt) {

    }
}
