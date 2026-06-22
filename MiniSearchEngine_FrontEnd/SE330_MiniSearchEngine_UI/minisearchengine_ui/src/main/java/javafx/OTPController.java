package javafx;

import java.io.IOException;

import javafx.application.Platform;
import javafx.auth.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class OTPController {
    @FXML private TextField txtOtp;
    @FXML private Button btnVerify;
    @FXML private Button btnBackLogin;
    @FXML private Label lblMessage;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        lblMessage.setText("");
    }

    @FXML
    private void handleVerifyOtp() {
        String otp = txtOtp.getText().trim();
        String email = RegisterController.pendingOtpEmail;

        if (email == null || email.isBlank()) {
            lblMessage.setText("Không có dữ liệu đăng ký. Vui lòng quay lại đăng ký.");
            return;
        }

        if (!otp.matches("\\d{6}")) {
            lblMessage.setText("Mã OTP phải gồm 6 chữ số.");
            return;
        }

        btnVerify.setDisable(true);
        authService.verifyOtpAsync(email, otp, successMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Xác thực OTP thành công. Vui lòng đăng nhập.");
                try {
                    MainApp.setRoot("login", "Đăng nhập");
                } catch (IOException e) {
                    e.printStackTrace();
                    lblMessage.setText("OTP xác thực thành công nhưng không thể chuyển sang đăng nhập.");
                    btnVerify.setDisable(false);
                }
            });
        }, errorMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Xác thực OTP thất bại: " + errorMsg);
                btnVerify.setDisable(false);
            });
        });
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
