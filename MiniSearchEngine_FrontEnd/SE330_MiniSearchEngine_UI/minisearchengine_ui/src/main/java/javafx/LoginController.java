package javafx;

import java.io.IOException;

import javafx.application.Platform;
import javafx.auth.AuthService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsername;
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
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập username và mật khẩu.");
            return;
        }

        btnLogin.setDisable(true);
        Task<Boolean> task = authService.loginAsync(username, password, successMsg -> {
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
