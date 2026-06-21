package javafx.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Service để handle auth operations (login, register)
 */
public class AuthService {
  private static final String BASE_URL = "http://localhost:8080/api/v1/auth";
  private static final String LOGIN_ENDPOINT = BASE_URL + "/login";
  private static final String REGISTER_ENDPOINT = BASE_URL + "/register";
  private static final String LOGOUT_ENDPOINT = BASE_URL + "/logout";

  private final HttpClient httpClient;
  private final Gson gson;

  public AuthService() {
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new Gson();
  }

  /**
   * Login với email và password
   * 
   * @param email    user email
   * @param password user password
   * @return LoginResponse hoặc null nếu fail
   */
  public LoginResponse login(String username, String password) throws Exception {
    JsonObject body = new JsonObject();
    body.addProperty("username", username);
    body.addProperty("password", password);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(LOGIN_ENDPOINT))
        .timeout(Duration.ofSeconds(10))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    ApiResponse<LoginResponse> apiResponse = ApiResponse.fromJson(response.body(), LoginResponse.class);

    if (response.statusCode() == 200) {

      if (apiResponse != null && apiResponse.isSuccess() && apiResponse.data != null) {

        return apiResponse.data;
      }
    }

    throw new Exception("Login failed: " + apiResponse.message);
  }

  /**
   * Register user mới
   * 
   * @param email    user email
   * @param password user password
   * @param name     user name
   * @return RegisterResponse hoặc null nếu fail
   */
  public RegisterResponse register(String username, String password) throws Exception {
    JsonObject body = new JsonObject();
    body.addProperty("username", username);
    body.addProperty("password", password);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(REGISTER_ENDPOINT))
        .timeout(Duration.ofSeconds(10))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      ApiResponse<RegisterResponse> apiResponse = ApiResponse.fromJson(response.body(), RegisterResponse.class);

    if (response.statusCode() == 201 || response.statusCode() == 200) {

      if (apiResponse != null && apiResponse.isSuccess() && apiResponse.data != null) {
        return apiResponse.data;
      }
    }

    throw new Exception("Register failed: " + apiResponse.message);
  }

  /**
   * Tạo Task để login async
   */
  public Task<Boolean> loginAsync(String username, String password, Consumer<String> onSuccess, Consumer<String> onError) {
    return new Task<Boolean>() {
      @Override
      protected Boolean call() {
        try {
          LoginResponse response = login(username, password);
          if (response != null && response.access_token != null) {
            // Map username to email field for compatibility
            String userEmail = response.user.username != null ? response.user.username : response.user.email;
            UserSession.getInstance().login(
                response.user.id,
                userEmail,
                response.user.name,
                response.access_token);
            onSuccess.accept("Login successful");
            return true;
          }
          onError.accept("Login failed: Invalid response");
          return false;
        } catch (Exception e) {
          onError.accept(e.getMessage());
          return false;
        }
      }
    };
  }

  /**
   * Tạo Task để register async
   */
  public Task<Boolean> registerAsync(String username, String password, Consumer<String> onSuccess,
      Consumer<String> onError) {
    return new Task<Boolean>() {
      @Override
      protected Boolean call() {
        try {
          RegisterResponse response = register(username, password);
          if (response != null) {
            onSuccess.accept("Register successful");
            return true;
          }
          onError.accept("Register failed: Invalid response");
          return false;
        } catch (Exception e) {
          onError.accept(e.getMessage());
          return false;
        }
      }
    };
  }

  public void logout() throws Exception {

    String accessToken =
            UserSession.getInstance().getAccessToken();

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(LOGOUT_ENDPOINT))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization",
                    "Bearer " + accessToken)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> response =
            httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {

        ApiResponse<Void> apiResponse =
                ApiResponse.fromJson(
                        response.body(),
                        Void.class);

        throw new Exception(
                apiResponse != null
                        ? apiResponse.message
                        : "Logout failed");
    }

    UserSession.getInstance().logout();
}
public Task<Boolean> logoutAsync(
        Consumer<String> onSuccess,
        Consumer<String> onError) {

    return new Task<Boolean>() {

        @Override
        protected Boolean call() {

            try {

                logout();

                onSuccess.accept(
                        "Logout successful");

                return true;

            } catch (Exception e) {

                onError.accept(
                        e.getMessage());

                return false;
            }
        }
    };
}

  // DTOs
  public static class LoginResponse {
    public String access_token;
    public UserInfo user;

    public static class UserInfo {
      public Long id;
      public String email;
      public String username;
      public String name;
    }
  }

  public static class RegisterResponse {
    public Long id;
    public String email;
    public String name;
  }

  @FunctionalInterface
  public interface Consumer<T> {
    void accept(T t);
  }
}
