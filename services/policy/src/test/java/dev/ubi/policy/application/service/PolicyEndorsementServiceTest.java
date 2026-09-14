package dev.ubi.policy.application.service;

import dev.ubi.policy.application.PolicyNotFoundException;
import dev.ubi.policy.application.port.in.AddDriverUseCase;
import dev.ubi.policy.application.port.out.PolicyRepository;
import dev.ubi.policy.domain.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static dev.ubi.policy.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyEndorsementServiceTest {

    @Mock
    private PolicyRepository repository;

    private PolicyEndorsementService service;

    @BeforeEach
    void setUp() {
        service = new PolicyEndorsementService(repository, Clock.fixed(IN_TERM, ZoneOffset.UTC));
    }

    @Test
    void throwsNotFoundExceptionIfPolicyDoesNotExist() {
        var policyId = newPolicyId();
        var command = new AddDriverUseCase.AddDriverCommand(policyId, newDriverId(), ALICE, IN_TERM);
        when(repository.findById(policyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addDriver(command)).isInstanceOf(PolicyNotFoundException.class);

        verify(repository).findById(policyId);
        verify(repository, never()).append(any(), any(), any());
    }

    @Test
    void passesLoadedVersionNotCurrentOne() {
        var policyId = newPolicyId();
        var policy = issuedPolicy(policyId, newDriverId());
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));

        service.addDriver(new AddDriverUseCase.AddDriverCommand(policyId, newDriverId(), BOB, IN_TERM));

        var captor = ArgumentCaptor.forClass(Policy.class);
        verify(repository).append(captor.capture(), anyList(), eq(IN_TERM));

        var passed = captor.getValue();
        assertThat(passed.loadedVersion()).isEqualTo(2);
        assertThat(passed.version()).isEqualTo(3);

    }
}
