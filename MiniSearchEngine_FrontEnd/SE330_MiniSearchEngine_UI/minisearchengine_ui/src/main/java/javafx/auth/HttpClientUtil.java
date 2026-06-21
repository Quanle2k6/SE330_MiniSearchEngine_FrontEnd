package javafx.auth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Utility class để wrapper HttpClient requests với automatic auth header
 */
public class HttpClientUtil {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Tạo HttpRequest builder với Authorization header nếu user đã login
     */
    private static HttpRequest.Builder createBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(DEFAULT_TIMEOUT);

        // Thêm auth header nếu user đã login
        String token = UserSession.getInstance().getAccessToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    /**
     * GET request
     */
    public static HttpResponse<String> get(String url) throws Exception {
        HttpRequest request = createBuilder(url)
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * POST request
     */
    public static HttpResponse<String> post(String url, String body, String contentType) throws Exception {
        HttpRequest request = createBuilder(url)
                .header("Content-Type", contentType != null ? contentType : "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * POST request với default content-type
     */
    public static HttpResponse<String> post(String url, String body) throws Exception {
        return post(url, body, "application/json");
    }

    /**
     * PUT request
     */
    public static HttpResponse<String> put(String url, String body, String contentType) throws Exception {
        HttpRequest request = createBuilder(url)
                .header("Content-Type", contentType != null ? contentType : "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * PUT request với default content-type
     */
    public static HttpResponse<String> put(String url, String body) throws Exception {
        return put(url, body, "application/json");
    }

    /**
     * DELETE request
     */
    public static HttpResponse<String> delete(String url) throws Exception {
        HttpRequest request = createBuilder(url)
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Async GET request
     */
    public static java.util.concurrent.CompletableFuture<HttpResponse<String>> getAsync(String url) {
        HttpRequest request = createBuilder(url)
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Async POST request
     */
    public static java.util.concurrent.CompletableFuture<HttpResponse<String>> postAsync(String url, String body) {
        HttpRequest request = createBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Async POST request với custom content-type
     */
    public static java.util.concurrent.CompletableFuture<HttpResponse<String>> postAsync(String url, String body, String contentType) {
        HttpRequest request = createBuilder(url)
                .header("Content-Type", contentType != null ? contentType : "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Check xem user có token không
     */
    public static boolean isAuthenticated() {
        return UserSession.getInstance().isLoggedIn();
    }
}
