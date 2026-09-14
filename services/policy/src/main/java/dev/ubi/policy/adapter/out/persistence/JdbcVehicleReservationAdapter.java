package dev.ubi.policy.adapter.out.persistence;

import dev.ubi.policy.application.VehicleAlreadyInsuredException;
import dev.ubi.policy.application.port.out.VehicleReservationPort;
import dev.ubi.policy.domain.vo.PolicyId;
import dev.ubi.policy.domain.vo.Vin;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcVehicleReservationAdapter implements VehicleReservationPort {

    private static final String RESERVE = """
        insert into vehicle_reservation (vin, policy_id)
        values (:vin, :policyId)
        on conflict do nothing
        """;

    private static final String RELEASE = """
        delete from vehicle_reservation
        where vin = :vin and policy_id = :policyId
        """;

    private final JdbcClient client;

    public JdbcVehicleReservationAdapter(JdbcClient client) {
        this.client = client;
    }

    @Override
    public void reserve(Vin vin, PolicyId policyId) {
        int inserted = client.sql(RESERVE)
            .param("vin", vin.value())
            .param("policyId", policyId.value())
            .update();
        if (inserted == 0) {
            throw new VehicleAlreadyInsuredException(vin);
        }
    }

    @Override
    public void release(Vin vin, PolicyId policyId) {
        client.sql(RELEASE)
            .param("vin", vin.value())
            .param("policyId", policyId.value())
            .update();
    }
}
