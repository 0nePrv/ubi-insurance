package dev.ubi.policy.domain.exception;

import dev.ubi.policy.domain.vo.Vin;

public class VehicleUnchangedException extends PolicyDomainException {

    private final Vin vin;

    public VehicleUnchangedException(Vin vin) {
        super("Vehicle not changed (vin %s)".formatted(vin));
        this.vin = vin;
    }

    public Vin vin() {
        return vin;
    }
}
