error id: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/MainApp.java:AuthService#
file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/MainApp.java
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 424
uri: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/MainApp.java
text:
```scala
package javafx;

import java.io.IOException;

import javafx.application.Application;
import javafx.auth.AuthService;
import javafx.auth.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static Stage stage;

    private void autoLogin() {
        try {
            AuthService@@ auth = new AuthService();

            try {
                auth.register(
                        "user",
                        "123456");
            } catch (Exception ignored) {
                // Email đã tồn tại thì bỏ qua
            }

            var login = auth.login(
                    "user",
                    "123456",
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
        // Try to reload any existing session, otherwise start at login
        UserSession.getInstance().reload();

        if (UserSession.getInstance().isLoggedIn()) {
            setRoot("primary", "MiniSearchEngine");
        } else {
            setRoot("login", "Đăng nhập");
        }

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

```


#### Short summary: 

empty definition using pc, found symbol in pc: 