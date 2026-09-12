package dev.ubi.policy.domain.exception;

import dev.ubi.policy.domain.vo.DriverId;

public class DriverAlreadyExistsException extends PolicyDomainException {

    private final DriverId driverId;

    public DriverAlreadyExistsException(DriverId driverId) {
        super("Driver with id %s already exists".formatted(driverId));
        this.driverId = driverId;
    }

    public DriverId driverId() {
        return driverId;
    }
}
