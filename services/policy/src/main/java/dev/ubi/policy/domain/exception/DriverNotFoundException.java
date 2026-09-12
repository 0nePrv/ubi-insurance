package dev.ubi.policy.domain.exception;

import dev.ubi.policy.domain.vo.DriverId;

public class DriverNotFoundException extends PolicyDomainException {

    private final DriverId driverId;

    public DriverNotFoundException(DriverId driverId) {
        super("Driver with id %s not found".formatted(driverId));
        this.driverId = driverId;
    }

    public DriverId driverId() {
        return driverId;
    }
}
