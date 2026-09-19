package edu.report.infra;

public final class InfraiRequestException extends RuntimeException {
    private final int statusCode;

    public InfraiRequestException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
