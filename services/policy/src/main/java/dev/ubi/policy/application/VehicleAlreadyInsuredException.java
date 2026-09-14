package dev.ubi.policy.application;

import dev.ubi.policy.domain.vo.Vin;

public class VehicleAlreadyInsuredException extends RuntimeException {

    private final Vin vin;

    public VehicleAlreadyInsuredException(Vin vin) {
        super("Vehicle with VIN %s is already reserved".formatted(vin));
        this.vin = vin;
    }

    public Vin vin() {
        return vin;
    }
}
