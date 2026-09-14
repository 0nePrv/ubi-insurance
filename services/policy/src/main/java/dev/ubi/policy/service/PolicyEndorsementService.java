package dev.ubi.policy.service;

import dev.ubi.policy.application.PolicyNotFoundException;
import dev.ubi.policy.application.port.in.AddDriverUseCase;
import dev.ubi.policy.application.port.in.RemoveDriverUseCase;
import dev.ubi.policy.application.port.out.PolicyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class PolicyEndorsementService implements AddDriverUseCase, RemoveDriverUseCase {

    private final PolicyRepository repository;
    private final Clock clock;

    @Autowired
    public PolicyEndorsementService(PolicyRepository repository) {
        this.repository = repository;
        this.clock = Clock.systemUTC();
    }

    PolicyEndorsementService(PolicyRepository repository, Clock clock) {
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
}
