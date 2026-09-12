package dev.ubi.policy.domain.event;

import dev.ubi.policy.domain.vo.DriverId;

public sealed interface DriverEvent extends PolicyEvent permits DriverAdded, DriverDetailsUpdated, DriverRemoved {

    DriverId driverId();
}
