package edu.report.config;

import java.time.Clock;

public record ReportSettings(String apiKey, String recipient, Clock clock) {
    public static ReportSettings fromEnvironment() {
        String apiKey = required("INFRAI_API_KEY");
        String recipient = required("REPORT_RECIPIENT");
        return new ReportSettings(apiKey, recipient, Clock.systemUTC());
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name);
        }
        return value;
    }
}
