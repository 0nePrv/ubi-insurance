package dev.ubi.policy.domain.exception;

public class ZeroPremiumException extends PolicyDomainException {

    public ZeroPremiumException() {
        super("Premium value is zero");
    }
}
