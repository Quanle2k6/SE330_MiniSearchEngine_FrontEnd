package javafx.auth;

/**
 * Singleton để quản lý user session hiện tại trong memory
 */
public class UserSession {
    private static UserSession instance;
    private Long userId;
    private String email;
    private String name;
    private String accessToken;

    private UserSession() {
    }

    /**
     * Lấy singleton instance
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
            // Load từ preferences nếu có
            instance.reload();
        }
        return instance;
    }

    /**
     * Load user session từ TokenManager
     */
    public void reload() {
    userId = TokenManager.getUserId();
    email = TokenManager.getUserEmail();
    name = TokenManager.getUserName();
    accessToken = TokenManager.getAccessToken();

    if (accessToken == null || accessToken.isBlank()) {
        logout();
    }
}

    /**
     * Khởi tạo session với user info
     */
    public void login(Long userId, String email, String name, String accessToken) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.accessToken = accessToken;
        
        // Lưu vào preferences
        TokenManager.saveAccessToken(accessToken);
        TokenManager.saveUserInfo(userId, email, name);
    }

    /**
     * Đăng xuất và xóa session
     */
    public void logout() {
        this.userId = null;
        this.email = null;
        this.name = null;
        this.accessToken = null;
        TokenManager.clearAll();
    }

    /**
     * Kiểm tra xem user đã login chưa
     */
    public boolean isLoggedIn() {
        return userId != null && accessToken != null && !accessToken.isEmpty();
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    // Setters
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        TokenManager.saveAccessToken(accessToken);
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
