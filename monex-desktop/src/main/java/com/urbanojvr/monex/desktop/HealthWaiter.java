package com.urbanojvr.monex.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

final class HealthWaiter {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(400);

    private HealthWaiter() {
    }

    static void waitUntilReady(int port) throws InterruptedException, IOException {
        URI healthUri = URI.create("http://127.0.0.1:" + port + "/api/health");
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        IOException lastError = null;

        while (System.nanoTime() < deadline) {
            try {
                if (isHealthy(healthUri.toURL())) {
                    return;
                }
            } catch (IOException e) {
                lastError = e;
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }

        throw new IOException("El backend no respondió a tiempo en " + healthUri, lastError);
    }

    private static boolean isHealthy(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(1500);
        connection.setReadTimeout(1500);
        connection.setRequestMethod("GET");
        try {
            int code = connection.getResponseCode();
            if (code != 200) {
                return false;
            }
            try (InputStream in = connection.getInputStream()) {
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return body.contains("\"status\":\"UP\"") || body.contains("\"status\" : \"UP\"");
            }
        } finally {
            connection.disconnect();
        }
    }
}
