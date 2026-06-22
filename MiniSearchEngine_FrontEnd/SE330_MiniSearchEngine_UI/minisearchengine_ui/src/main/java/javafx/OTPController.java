package javafx;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.auth.AuthService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class OTPController {
    @FXML private TextField txtOtp1;
    @FXML private TextField txtOtp2;
    @FXML private TextField txtOtp3;
    @FXML private TextField txtOtp4;
    @FXML private TextField txtOtp5;
    @FXML private TextField txtOtp6;
    @FXML private Button btnVerify;
    @FXML private Button btnResendOtp;
    @FXML private Button btnBackLogin;
    @FXML private Label lblMessage;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        lblMessage.setText("");

        List<TextField> inputs = List.of(txtOtp1, txtOtp2, txtOtp3, txtOtp4, txtOtp5, txtOtp6);
        for (int i = 0; i < inputs.size(); i++) {
            TextField field = inputs.get(i);
            field.setTextFormatter(new TextFormatter<>(change -> {
                if (change.isDeleted()) {
                    return change;
                }
                String text = change.getText();
                if (!text.matches("\\d?")) {
                    return null;
                }
                if (change.getControlNewText().length() > 1) {
                    return null;
                }
                return change;
            }));

            final int index = i;
            field.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && field.getText().isEmpty() && index > 0) {
                    inputs.get(index - 1).requestFocus();
                    inputs.get(index - 1).positionCaret(inputs.get(index - 1).getText().length());
                }
            });

            field.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && newValue.length() == 1 && index < inputs.size() - 1) {
                    inputs.get(index + 1).requestFocus();
                }
            });
        }
    }

    @FXML
    private void handleVerifyOtp() {
        String otp = getOtpValue();
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
        btnResendOtp.setDisable(true);

        Task<Boolean> task = authService.verifyOtpAsync(email, otp, successMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Xác thực OTP thành công. Vui lòng đăng nhập.");
                Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        "Tài khoản của bạn đã được xác thực. Vui lòng đăng nhập để tiếp tục.",
                        ButtonType.OK);
                alert.setTitle("Xác thực OTP");
                alert.setHeaderText("Xác thực thành công");
                alert.showAndWait();

                try {
                    MainApp.setRoot("login", "Đăng nhập");
                } catch (IOException e) {
                    e.printStackTrace();
                    lblMessage.setText("OTP xác thực thành công nhưng không thể chuyển sang đăng nhập.");
                    btnVerify.setDisable(false);
                    btnResendOtp.setDisable(false);
                }
            });
        }, errorMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Xác thực OTP thất bại: " + errorMsg);
                btnVerify.setDisable(false);
                btnResendOtp.setDisable(false);
            });
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleResendOtp() {
        String email = RegisterController.pendingOtpEmail;

        if (email == null || email.isBlank()) {
            lblMessage.setText("Không có dữ liệu đăng ký. Vui lòng quay lại đăng ký.");
            return;
        }

        btnResendOtp.setDisable(true);
        lblMessage.setText("Đang gửi lại mã OTP...");

        Task<Boolean> task = authService.resendOtpAsync(email, successMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Mã OTP đã được gửi lại. Vui lòng kiểm tra email.");
                btnResendOtp.setDisable(false);
            });
        }, errorMsg -> {
            Platform.runLater(() -> {
                lblMessage.setText("Gửi lại OTP thất bại: " + errorMsg);
                btnResendOtp.setDisable(false);
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

    private String getOtpValue() {
        return String.join("",
                txtOtp1.getText().trim(),
                txtOtp2.getText().trim(),
                txtOtp3.getText().trim(),
                txtOtp4.getText().trim(),
                txtOtp5.getText().trim(),
                txtOtp6.getText().trim());
    }
}
