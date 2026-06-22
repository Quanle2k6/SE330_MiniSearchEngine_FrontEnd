error id: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/LoginController.java:javafx/scene/control/TextField#
file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/LoginController.java
empty definition using pc, found symbol in pc: javafx/scene/control/TextField#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 323
uri: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/LoginController.java
text:
```scala
package javafx;

import java.io.IOException;

import javafx.application.Platform;
import javafx.auth.AuthService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.@@TextField;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnShowRegister;
    @FXML private Label lblMessage;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        lblMessage.setText("");
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập email và mật khẩu.");
            return;
        }

        btnLogin.setDisable(true);
        Task<Boolean> task = authService.loginAsync(email, password, successMsg -> {
            Platform.runLater(() -> {
                try {
                    MainApp.setRoot("primary", "MiniSearchEngine");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }, errorMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Đăng nhập thất bại: " + errorMsg);
                btnLogin.setDisable(false);
            });
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleShowRegister() {
        try {
            MainApp.setRoot("register", "Đăng ký");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: javafx/scene/control/TextField#