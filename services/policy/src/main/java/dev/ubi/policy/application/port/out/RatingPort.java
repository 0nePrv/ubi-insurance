package dev.ubi.policy.application.port.out;

import dev.ubi.policy.domain.vo.Driver;
import dev.ubi.policy.domain.vo.Money;
import dev.ubi.policy.domain.vo.Vehicle;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

public interface RatingPort {

    PremiumQuote calculatePremium(Vehicle vehicle, Collection<Driver> drivers, LocalDate effectiveDate);

    record PremiumQuote(Money annualPremium, String tariffVersion, Map<String, Double> factors) {

    }
}
