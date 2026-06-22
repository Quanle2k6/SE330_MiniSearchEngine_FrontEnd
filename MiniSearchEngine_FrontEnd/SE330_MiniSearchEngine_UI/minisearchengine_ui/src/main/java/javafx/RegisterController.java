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
    public static String pendingOtpEmail;
    public static String pendingOtpName;
    public static String pendingOtpPassword;

    @FXML private TextField txtEmail;
    @FXML private TextField txtName;
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
        String email = txtEmail.getText().trim();
        String name = txtName.getText().trim();
        String password = txtPassword.getText().trim();
        String confirm = txtPasswordConfirm.getText().trim();

        if (email.isEmpty() || name.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập email, họ tên và mật khẩu.");
            return;
        }

        if (!password.equals(confirm)) {
            lblMessage.setText("Mật khẩu xác nhận không khớp.");
            return;
        }

        btnRegister.setDisable(true);
        RegisterController.pendingOtpEmail = email;
        RegisterController.pendingOtpName = name;
        RegisterController.pendingOtpPassword = password;

        Task<Boolean> task = authService.registerAsync(email, name, password, successMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Đăng ký thành công. Vui lòng nhập mã OTP 6 ký tự.");
                try {
                    MainApp.setRoot("otp", "Xác thực OTP");
                } catch (IOException e) {
                    e.printStackTrace();
                    lblMessage.setText("Đăng ký thành công nhưng không thể mở màn hình OTP.");
                    btnRegister.setDisable(false);
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
