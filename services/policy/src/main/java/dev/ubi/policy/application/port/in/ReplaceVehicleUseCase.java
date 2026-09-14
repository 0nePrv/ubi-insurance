package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.vo.Vehicle;

import java.time.Instant;

public interface ReplaceVehicleUseCase {

    void replaceVehicle(ReplaceVehicleCommand command);

    record ReplaceVehicleCommand(Vehicle newVehicle, Instant effectiveAt, Instant recordedAt) {}
}
