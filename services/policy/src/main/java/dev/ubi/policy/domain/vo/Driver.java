package dev.ubi.policy.domain.vo;

import java.time.LocalDate;

public record Driver(String firstName, String lastName, LocalDate birthDate,
                     DrivingLicense license, LocalDate licenseIssuanceDate) {

}
