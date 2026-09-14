package dev.ubi.policy.application.port.in;

import dev.ubi.policy.domain.Quote;
import dev.ubi.policy.domain.vo.Driver;
import dev.ubi.policy.domain.vo.DriverId;
import dev.ubi.policy.domain.vo.TermMode;
import dev.ubi.policy.domain.vo.Vehicle;

import java.time.ZonedDateTime;
import java.util.Map;

public interface RequestQuoteUseCase {

    Quote requestQuote(RequestQuoteCommand command);

    record RequestQuoteCommand(String holderId,
                               Vehicle vehicle,
                               Map<DriverId, Driver> drivers,
                               ZonedDateTime termStart,
                               TermMode termMode) {
    }
}
