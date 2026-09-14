package dev.ubi.policy.adapter.out;

import dev.ubi.policy.application.port.out.RatingPort;
import dev.ubi.policy.domain.vo.Driver;
import dev.ubi.policy.domain.vo.Money;
import dev.ubi.policy.domain.vo.Vehicle;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

@Component
public class RatingPortStub implements RatingPort {

    @Override
    public PremiumQuote calculatePremium(Vehicle vehicle, Collection<Driver> drivers, LocalDate effectiveDate) {
        return new PremiumQuote(Money.ofMinorRub(10_000L), "v1", Map.of());
    }
}
