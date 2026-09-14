package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.vo.PolicyId;
import dev.ubi.policy.domain.vo.Vehicle;

import java.time.Instant;

public interface ReplaceVehicleUseCase {

    void replaceVehicle(ReplaceVehicleCommand command);

    record ReplaceVehicleCommand(PolicyId policyId, Vehicle newVehicle, Instant effectiveAt) {}
}
