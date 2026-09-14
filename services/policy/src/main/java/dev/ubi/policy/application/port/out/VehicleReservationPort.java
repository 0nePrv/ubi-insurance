package dev.ubi.policy.application.port.out;

import dev.ubi.policy.domain.vo.PolicyId;
import dev.ubi.policy.domain.vo.Vin;

public interface VehicleReservationPort {

    void reserve(Vin vin, PolicyId policyId);

    void release(Vin vin, PolicyId policyId);
}
