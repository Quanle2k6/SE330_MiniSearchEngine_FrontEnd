package javafx.auth;

import java.util.prefs.Preferences;

/**
 * Quản lý lưu trữ và lấy token từ system preferences
 */
public class TokenManager {
    private static final String PREFS_NODE = "javafx/minisearchengine/auth";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_NAME_KEY = "user_name";

    private static final Preferences prefs = Preferences.userRoot().node(PREFS_NODE);

    /**
     * Lưu access token
     */
    public static void saveAccessToken(String token) {
    if (token == null || token.isBlank()) {
        prefs.remove(ACCESS_TOKEN_KEY);
        return;
    }

    prefs.put(ACCESS_TOKEN_KEY, token);
}

    /**
     * Lấy access token
     */
    public static String getAccessToken() {
        return prefs.get(ACCESS_TOKEN_KEY, null);
    }

    /**
     * Lưu refresh token
     */
    public static void saveRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            prefs.remove(REFRESH_TOKEN_KEY);
            return;
        }
        prefs.put(REFRESH_TOKEN_KEY, token);
    }

    /**
     * Lấy refresh token
     */
    public static String getRefreshToken() {
        return prefs.get(REFRESH_TOKEN_KEY, null);
    }

    /**
     * Lưu user info
     */
    public static void saveUserInfo(Long userId, String email, String name) {
        prefs.putLong(USER_ID_KEY, userId != null ? userId : 0);
        prefs.put(USER_EMAIL_KEY, email != null ? email : "");
        prefs.put(USER_NAME_KEY, name != null ? name : "");
    }

    /**
     * Lấy user ID
     */
    public static Long getUserId() {
        long id = prefs.getLong(USER_ID_KEY, 0);
        return id > 0 ? id : null;
    }

    /**
     * Lấy user email
     */
    public static String getUserEmail() {
    String email = prefs.get(USER_EMAIL_KEY, null);

    return email == null || email.isBlank()
            ? null
            : email;
}

    /**
     * Lấy user name
     */
    public static String getUserName() {
        String name = prefs.get(USER_NAME_KEY, null);

        return name == null || name.isBlank()
                ? null
                : name;
    }

    /**
     * Kiểm tra xem user có token không
     */
    public static boolean isTokenExists() {
    String token = getAccessToken();

    return token != null
            && !token.isBlank();
}

    /**
     * Xóa tất cả token và user info (logout)
     */
    public static void clearAll() {
        prefs.remove(ACCESS_TOKEN_KEY);
        prefs.remove(REFRESH_TOKEN_KEY);
        prefs.remove(USER_ID_KEY);
        prefs.remove(USER_EMAIL_KEY);
        prefs.remove(USER_NAME_KEY);
    }
}
