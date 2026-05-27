package javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PrimaryController {

    @FXML
    private TabPane mainTabPane;

    private int tabCounter = 1;

    @FXML
    public void initialize() {
        // Tự động mở tab đầu tiên khi vừa khởi chạy ứng dụng
        createNewTab();
    }

    @FXML
    private void handleNewTab(ActionEvent event) {
        createNewTab();
    }

    private void VirtualSearch(String query, VBox vboxResults) {
        // Hàm này sẽ được gọi khi người dùng thực hiện tìm kiếm
        // Bạn sẽ thay thế phần này bằng logic thực tế để gọi công cụ tìm kiếm của bạn

        for (int i = 1; i <= 4; i++) {
                VBox card = new VBox(4);
                
                Label title = new Label("Kết quả thứ " + i + " cho từ khóa: " + query);
                title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand;");
                // Hiệu ứng gạch chân khi di chuột giống liên kết web
                title.setOnMouseEntered(e -> title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand; -fx-underline: true;"));
                title.setOnMouseExited(e -> title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand; -fx-underline: false;"));

                Label link = new Label("https://minisearchengine.com/doc/file_sample_" + i + ".txt");
                link.setStyle("-fx-text-fill: #202124; -fx-font-size: 12px;");

                Label snippet = new Label("Văn bản trích đoạn hiển thị nội dung khớp từ khóa \"" + query + "\" giúp người dùng đánh giá độ chính xác trước khi mở tệp...");
                snippet.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
                snippet.setWrapText(true);

                card.getChildren().addAll(title, link, snippet);
                vboxResults.getChildren().add(card);
            }
    }

    private void createNewTab() {
        Tab newTab = new Tab("Tab mới");
        
        BorderPane tabContentRoot = new BorderPane();
        tabContentRoot.setStyle("-fx-background-color: #ffffff;");

        VBox searchContainer = new VBox(20);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setStyle("-fx-padding: 40 0 20 0;");

        Label lblLogo = new Label("Google");
        lblLogo.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");

        HBox searchBarBox = new HBox(10);
        searchBarBox.setAlignment(Pos.CENTER);
        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm trên Mini Google hoặc nhập một URL...");
        txtSearch.setPrefWidth(500);
        txtSearch.setStyle("-fx-font-size: 14px; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #dfe1e5; -fx-padding: 8 15 8 15;");

        Button btnSearch = new Button("Tìm kiếm");
        btnSearch.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #f8f9fa; -fx-text-fill: #3c4043; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-background-radius: 4;");

        searchBarBox.getChildren().addAll(txtSearch, btnSearch);
        searchContainer.getChildren().addAll(lblLogo, searchBarBox);
        
        tabContentRoot.setCenter(searchContainer);

        ScrollPane scrollResults = new ScrollPane();
        scrollResults.setFitToWidth(true);
        scrollResults.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");
        
        VBox vboxResults = new VBox(15);
        vboxResults.setStyle("-fx-padding: 10 50 20 50;"); // Căn lề trái giống trang kết quả Google
        scrollResults.setContent(vboxResults);

        Runnable searchAction = () -> {
            String query = txtSearch.getText().trim();
            if (query.isEmpty()) return;

            newTab.setText(query.length() > 10 ? query.substring(0, 10) + "..." : query);
            searchContainer.setAlignment(Pos.TOP_LEFT);
            searchContainer.setStyle("-fx-padding: 15 0 10 50; -fx-background-color: #f8f9fa; -fx-border-color: #e4e4e4; -fx-border-width: 0 0 1 0;");
            lblLogo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");
            searchBarBox.setAlignment(Pos.CENTER_LEFT);
            
            tabContentRoot.setCenter(null);
            tabContentRoot.setTop(searchContainer);
            tabContentRoot.setCenter(scrollResults);

            vboxResults.getChildren().clear();
            
            VirtualSearch(query, vboxResults); // ọi hàm tìm kiếm ảo (Sau này sẽ gọi hàm xử lý từ công cụ tìm kiếm chính của bạn)
            
        };

        btnSearch.setOnAction(e -> searchAction.run());
        txtSearch.setOnAction(e -> searchAction.run());

        // 6. Hoàn thiện đưa nội dung vào Tab và thêm Tab vào màn hình chính
        newTab.setContent(tabContentRoot);
        mainTabPane.getTabs().add(newTab);
        
        // Tự động chuyển góc nhìn sang Tab vừa mới tạo
        mainTabPane.getSelectionModel().select(newTab);


    }
}