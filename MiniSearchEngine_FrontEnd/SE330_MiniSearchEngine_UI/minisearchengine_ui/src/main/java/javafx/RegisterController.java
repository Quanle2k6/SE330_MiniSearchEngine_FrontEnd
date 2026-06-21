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

public class RegisterController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPasswordConfirm;
    @FXML private Button btnRegister;
    @FXML private Button btnBackLogin;
    @FXML private Label lblMessage;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        lblMessage.setText("");
    }

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String confirm = txtPasswordConfirm.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập username và mật khẩu.");
            return;
        }

        if (!password.equals(confirm)) {
            lblMessage.setText("Mật khẩu xác nhận không khớp.");
            return;
        }

        btnRegister.setDisable(true);

        Task<Boolean> task = authService.registerAsync(username, password, successMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Đăng ký thành công. Vui lòng đăng nhập.");
                try {
                    MainApp.setRoot("login", "Đăng nhập");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }, errorMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Đăng ký thất bại: " + errorMsg);
                btnRegister.setDisable(false);
            });
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleBackLogin() {
        try {
            MainApp.setRoot("login", "Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
