package dev.ubi.policy.application.service;

import dev.ubi.policy.application.PolicyNotFoundException;
import dev.ubi.policy.application.port.in.*;
import dev.ubi.policy.application.port.out.PolicyRepository;
import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.event.PolicyEvent;
import dev.ubi.policy.domain.vo.PolicyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;

@Service
public class PolicyEndorsementService implements AddDriverUseCase, RemoveDriverUseCase, UpdateDriverDetailsUseCase,
    ReplaceVehicleUseCase, CancelPolicyUseCase {

    private final PolicyRepository repository;
    private final Clock clock;

    public PolicyEndorsementService(PolicyRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void addDriver(AddDriverCommand command) {
        endorse(command.policyId(), ((policy, recordedAt) ->
            policy.addDriver(command.driverId(), command.driver(), command.effectiveAt(), recordedAt)));
    }

    @Override
    @Transactional
    public void updateDriverDetails(UpdateDriverDetailsCommand command) {
        endorse(command.policyId(), ((policy, recordedAt) ->
            policy.updateDriverDetails(command.driverId(), command.driver(), command.effectiveAt(), recordedAt)));
    }

    @Override
    @Transactional
    public void removeDriver(RemoveDriverCommand command) {
        endorse(command.policyId(), (policy, recordedAt) ->
            policy.removeDriver(command.driverId(), command.effectiveAt(), recordedAt));
    }

    @Override
    @Transactional
    public void replaceVehicle(ReplaceVehicleCommand command) {
        endorse(command.policyId(), (policy, recordedAt) ->
            policy.replaceVehicle(command.newVehicle(), command.effectiveAt(), recordedAt));
    }

    @Override
    @Transactional
    public void cancel(CancelPolicyCommand command) {
        endorse(command.policyId(), (policy, recordedAt) ->
            policy.cancel(command.reason(), command.effectiveAt(), recordedAt));
    }

    private void endorse(PolicyId id, BiFunction<Policy, Instant, List<PolicyEvent>> command) {
        var recordedAt = clock.instant();
        var policy = repository.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
        repository.append(policy, command.apply(policy, recordedAt), recordedAt);
    }
}
