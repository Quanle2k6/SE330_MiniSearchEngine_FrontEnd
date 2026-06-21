package javafx;

import javafx.application.Application;
import javafx.auth.AuthService;
import javafx.auth.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    private static Stage stage;

    private void autoLogin() {
        try {
            AuthService auth = new AuthService();

            try {
                auth.register(
                        "test@gmail.com",
                        "123456",
                        "Test User");
            } catch (Exception ignored) {
                // Email đã tồn tại thì bỏ qua
            }

            var login = auth.login(
                    "test@gmail.com",
                    "123456");

            UserSession.getInstance().login(
                    login.user.id,
                    login.user.email,
                    login.user.name,
                    login.access_token);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(@SuppressWarnings("exports") Stage s) throws IOException {
        stage = s;
        System.out.println("Auto Login");
        autoLogin();
        System.out.println("Logged In = " +
                UserSession.getInstance().isLoggedIn());

        System.out.println("User ID = " +
                UserSession.getInstance().getUserId());

        System.out.println("Token = " +
                UserSession.getInstance().getAccessToken());
        setRoot("primary", "");

    }

    static void setRoot(String fxml) throws IOException {
        setRoot(fxml, stage.getTitle());
    }

    static void setRoot(String fxml, String title) throws IOException {
        Scene scene = new Scene(loadFXML(fxml));
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
