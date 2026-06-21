package javafx.auth;

public class AuthExample {

  /**
   * Example: Check auth status
   */
  public static void exampleCheckAuthStatus() {
    System.out.println("\n--- Checking Auth Status ---");

    if (HttpClientUtil.isAuthenticated()) {
      System.out.println("User is logged in");
      System.out.println("   Email: " + AuthUtil.getCurrentUserEmail());
      System.out.println("   Name: " + AuthUtil.getCurrentUserName());
      System.out.println("   ID: " + AuthUtil.getCurrentUserId());
    } else {
      System.out.println("User is not logged in");
    }
  }

  /**
   * Main method để test
   */
  public static void main(String[] args) {
    AuthService auth = new AuthService();

    try {
      // var register = auth.register(
      // "test1@gmail.com",
      // "123456",
      // "Test"
      // );

      // System.out.println(register.email);
      // System.out.println("login = " + new Gson().toJson(register));
      //

      // var login = auth.login(
      //     "test@gmail.com",
      //     "123456");

      // UserSession.getInstance().login(
      //     login.user.id,
      //     login.user.email,
      //     login.user.name,
      //     login.access_token);

      auth.logout();

      System.out.println(
          UserSession.getInstance().isLoggedIn());
      // System.out.println(UserSession.getInstance().getAccessToken());
      // System.out.println(UserSession.getInstance().isLoggedIn());
      // UserSession.getInstance().logout();

      // JsonObject body = new JsonObject();
      // body.addProperty("type", "QUERY");
      // body.addProperty("query", "loài bò");

      // var response = HttpClientUtil.post(
      // "http://localhost:8080/search/history",
      // body.toString()
      // );

      // System.out.println(response.statusCode());
      // System.out.println(response.body());
      // System.out.println("login = " + new Gson().toJson(login));
      // System.out.println(login.access_token);

    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
