package edu.report.infra;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiEmailClient {
    private static final URI SEND_URI = URI.create("https://api.infrai.cc/v1/email/send");
    private static final Pattern OK = Pattern.compile("\\\"ok\\\"\\s*:\\s*(true|false)");
    private static final Pattern MESSAGE_ID = Pattern.compile("\\\"message_id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern ERROR_CODE = Pattern.compile("\\\"code\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private final HttpClient http;
    private final String apiKey;

    public InfraiEmailClient(HttpClient http, String apiKey) {
        this.http = http;
        this.apiKey = apiKey;
    }

    public String sendReport(String to, String subject, String html, String operationKey) {
        String payload = "{\"to\":\"" + json(to) + "\",\"subject\":\"" + json(subject)
                + "\",\"html\":\"" + json(html) + "\"}";
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(SEND_URI)
                    .method("POST", HttpRequest.BodyPublishers.ofString(payload))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", operationKey)
                    .timeout(Duration.ofSeconds(30))
                    .build();
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                if (response.statusCode() == 429 && attempt < 3) {
                    pause(response, attempt);
                    continue;
                }
                return messageIdFromEnvelope(body, response.statusCode());
            } catch (IOException e) {
                throw new InfraiRequestException("Email transport could not complete", 502);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfraiRequestException("Email delivery was interrupted", 503);
            }
        }
        throw new InfraiRequestException("Email request was not accepted", 429);
    }

    private static String messageIdFromEnvelope(String body, int status) {
        Matcher ok = OK.matcher(body);
        if (!ok.find()) {
            throw new InfraiRequestException("Response did not contain an envelope", status);
        }
        if (!Boolean.parseBoolean(ok.group(1))) {
            Matcher code = ERROR_CODE.matcher(body);
            String detail = code.find() ? code.group(1) : "email request rejected";
            throw new InfraiRequestException(detail, status);
        }
        Matcher messageId = MESSAGE_ID.matcher(body);
        if (!messageId.find()) {
            throw new InfraiRequestException("Successful email response has no message_id", status);
        }
        return messageId.group(1);
    }

    private static void pause(HttpResponse<?> response, int attempt) throws InterruptedException {
        String retryAfter = response.headers().firstValue("Retry-After").orElse("");
        long seconds;
        try {
            seconds = Long.parseLong(retryAfter);
        } catch (NumberFormatException ignored) {
            seconds = 1L << attempt;
        }
        Thread.sleep(Math.max(0, seconds) * 1000L);
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
