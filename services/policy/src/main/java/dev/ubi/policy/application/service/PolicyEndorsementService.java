package dev.ubi.policy.application.service;

import dev.ubi.policy.application.PolicyNotFoundException;
import dev.ubi.policy.application.port.in.AddDriverUseCase;
import dev.ubi.policy.application.port.in.RemoveDriverUseCase;
import dev.ubi.policy.application.port.in.ReplaceVehicleUseCase;
import dev.ubi.policy.application.port.out.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class PolicyEndorsementService implements AddDriverUseCase, RemoveDriverUseCase, ReplaceVehicleUseCase {

    private final PolicyRepository repository;
    private final Clock clock;

    public PolicyEndorsementService(PolicyRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void addDriver(AddDriverCommand command) {
        var recordedAt = clock.instant();
        var id = command.policyId();
        var policy = repository.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
        var events = policy.addDriver(command.driverId(), command.driver(), command.effectiveAt(), recordedAt);
        repository.append(policy, events, recordedAt);
    }

    @Override
    @Transactional
    public void removeDriver(RemoveDriverCommand command) {
        var recordedAt = clock.instant();
        var id = command.policyId();
        var policy = repository.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
        var events = policy.removeDriver(command.driverId(), command.effectiveAt(), recordedAt);
        repository.append(policy, events, recordedAt);
    }

    @Override
    public void replaceVehicle(ReplaceVehicleCommand command) {
        var recordedAt = clock.instant();
        var id = command.policyId();
        var policy = repository.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
        var events = policy.replaceVehicle(command.newVehicle(), command.effectiveAt(), recordedAt);
        repository.append(policy, events, recordedAt);
    }
}
