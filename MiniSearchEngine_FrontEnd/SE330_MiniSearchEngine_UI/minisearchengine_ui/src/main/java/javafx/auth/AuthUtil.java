package javafx.auth;

import javafx.auth.AuthService.Consumer;
import javafx.concurrent.Task;

/**
 * Helper utility class cho auth operations
 */
public class AuthUtil {
    
    /**
     * Perform logout: xóa session và return callback
     */
    public static void logout(
        Consumer<Void> onSuccess,
        Consumer<String> onError) {

    AuthService authService =
            new AuthService();

    Task<Boolean> task =
            authService.logoutAsync(
                    success -> {
                        if (onSuccess != null) {
                            onSuccess.accept(null);
                        }
                    },
                    error -> {
                        if (onError != null) {
                            onError.accept(error);
                        }
                    });

    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
}

    /**
     * Kiểm tra xem user đã login chưa
     */
    public static boolean isLoggedIn() {
        return UserSession.getInstance().isLoggedIn();
    }

    /**
     * Get current user info
     */
    public static String getCurrentUserEmail() {
        return UserSession.getInstance().getEmail();
    }

    /**
     * Get current user name
     */
    public static String getCurrentUserName() {
        return UserSession.getInstance().getName();
    }

    /**
     * Get current user ID
     */
    public static Long getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }

    /**
     * Print current session info (for debugging)
     */
    public static void printSessionInfo() {
        System.out.println("=== Session Info ===");
        System.out.println("Is Logged In: " + isLoggedIn());
        System.out.println("User ID: " + getCurrentUserId());
        System.out.println("Email: " + getCurrentUserEmail());
        System.out.println("Name: " + getCurrentUserName());
        System.out.println("====================");
    }
}
