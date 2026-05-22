package javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PrimaryController {

    @FXML
    private TextField txtSearchQuery;

    @FXML
    private Button btnSearch;

    @FXML
    private Label lblStatus;

    @FXML
    private VBox vboxResults;

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = txtSearchQuery.getText().trim();
        
        if (query.isEmpty()) {
            lblStatus.setText("Vui lòng nhập từ khóa cần tìm!");
            return;
        }

        lblStatus.setText("Đang tìm kiếm từ khóa: \"" + query + "\"...");
        
        // Xóa các kết quả của lần tìm kiếm trước (nếu có)
        vboxResults.getChildren().clear();

        // MOCK DATA: Giả lập đổ dữ liệu tìm kiếm (Sau này bạn sẽ thay bằng hàm gọi từ Backend của bạn)
        for (int i = 1; i <= 3; i++) {
            VBox resultCard = createResultCard(
                "Tài liệu kết quả số " + i + " liên quan đến " + query,
                "D:\\UIT\\Documents\\Project_Core\\sample_file_" + i + ".txt",
                "Đoạn trích chứa từ khóa: ... nội dung tìm thấy cho cụm từ " + query + " hiển thị tại đây để người dùng dễ đọc..."
            );
            vboxResults.getChildren().add(resultCard);
        }

        lblStatus.setText("Tìm thấy 3 kết quả cho: \"" + query + "\" (trong 12ms)");
    }

    // Hàm bổ trợ tạo nhanh một "Card" kết quả tìm kiếm bằng code Java
    private VBox createResultCard(String title, String path, String snippet) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-background-color: white;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1a0dab; -fx-cursor: hand;");
        
        Label lblPath = new Label(path);
        lblPath.setStyle("-fx-font-size: 12px; -fx-text-fill: #006621;");
        
        Label lblSnippet = new Label(snippet);
        lblSnippet.setStyle("-fx-font-size: 13px; -fx-text-fill: #545454;");
        lblSnippet.setWrapText(true);

        card.getChildren().addAll(lblTitle, lblPath, lblSnippet);
        return card;
    }
}