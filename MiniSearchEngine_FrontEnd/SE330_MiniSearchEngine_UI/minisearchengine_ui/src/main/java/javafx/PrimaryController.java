package javafx;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class PrimaryController {

    private static final String SEARCH_API_URL = "http://localhost:8080/search";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @FXML
    private TabPane mainTabPane;

    @FXML
    public void initialize() {
        createNewTab();
    }

    @FXML
    private void handleNewTab(ActionEvent event) {
        createNewTab();
    }

    private void search(String query, VBox vboxResults) {
        Label loading = new Label("Đang tìm kiếm...");
        loading.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
        vboxResults.getChildren().setAll(loading);

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SEARCH_API_URL + "?query=" + encodedQuery))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> handleSearchResponse(response, vboxResults)))
                .exceptionally(error -> {
                    Platform.runLater(() -> showMessage(vboxResults, "Không thể kết nối tới server localhost:8080."));
                    return null;
                });
    }

    private void handleSearchResponse(HttpResponse<String> response, VBox vboxResults) {
        if (response.statusCode() != 200) {
            showMessage(vboxResults, "Tìm kiếm thất bại. Mã lỗi: " + response.statusCode());
            return;
        }

        try {
            RestSearchResponse restResponse = gson.fromJson(response.body(), RestSearchResponse.class);
            if (restResponse == null) {
                showMessage(vboxResults, "Dữ liệu trả về từ server không đúng định dạng.");
                return;
            }

            if (restResponse.statusCode >= 400 || restResponse.error != null) {
                String message = restResponse.message == null ? restResponse.error : String.valueOf(restResponse.message);
                showMessage(vboxResults, valueOrDefault(message, restResponse.error));
                return;
            }

            List<ResSearchItemDTO> items = getSearchItems(restResponse.data);
            if (items.isEmpty()) {
                showMessage(vboxResults, "Không tìm thấy kết quả phù hợp.");
                return;
            }

            vboxResults.getChildren().clear();
            for (ResSearchItemDTO item : items) {
                vboxResults.getChildren().add(createResultCard(item));
            }
        } catch (JsonSyntaxException error) {
            showMessage(vboxResults, "Dữ liệu trả về từ server không đúng định dạng.");
        }
    }

    private VBox createResultCard(ResSearchItemDTO item) {
        VBox card = new VBox(4);

        Label title = new Label(valueOrDefault(item.title, "(Không có tiêu đề)"));
        title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand;");
        title.setWrapText(true);
        title.setOnMouseEntered(e -> title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand; -fx-underline: true;"));
        title.setOnMouseExited(e -> title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand; -fx-underline: false;"));
        title.setOnMouseClicked(e -> openResultUrl(item.url));

        Label link = new Label(valueOrDefault(item.url, ""));
        link.setStyle("-fx-text-fill: #202124; -fx-font-size: 12px;");
        link.setWrapText(true);

        Label snippet = new Label(valueOrDefault(item.summary, item.content));
        snippet.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
        snippet.setWrapText(true);

        card.getChildren().addAll(title, link, snippet);
        return card;
    }

    private void showMessage(VBox vboxResults, String message) {
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
        label.setWrapText(true);
        vboxResults.getChildren().setAll(label);
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue == null ? "" : defaultValue;
        }
        return value;
    }

    private void openResultUrl(String rawUrl) {
        String url = normalizeUrl(rawUrl);
        if (url.isEmpty() || !Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return;
        }

        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IllegalArgumentException | IOException error) {
            System.err.println("Cannot open URL: " + url);
        }
    }

    private String normalizeUrl(String rawUrl) {
        String url = valueOrDefault(rawUrl, "").trim();
        if (url.isEmpty()) {
            return "";
        }

        if (!url.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            return "https://" + url;
        }

        return url;
    }

    private List<ResSearchItemDTO> getSearchItems(JsonElement data) {
        if (data == null || data.isJsonNull()) {
            return Collections.emptyList();
        }

        if (data.isJsonArray()) {
            return gson.fromJson(data, new TypeToken<List<ResSearchItemDTO>>() {
            }.getType());
        }

        if (data.isJsonObject()) {
            JsonObject dataObject = data.getAsJsonObject();
            JsonElement items = dataObject.get("items");
            if (items != null && items.isJsonArray()) {
                return gson.fromJson(items, new TypeToken<List<ResSearchItemDTO>>() {
                }.getType());
            }

            ResSearchItemDTO item = gson.fromJson(dataObject, ResSearchItemDTO.class);
            return item == null ? Collections.emptyList() : Collections.singletonList(item);
        }

        return Collections.emptyList();
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
        vboxResults.setStyle("-fx-padding: 10 50 20 50;");
        scrollResults.setContent(vboxResults);

        Runnable searchAction = () -> {
            String query = txtSearch.getText().trim();
            if (query.isEmpty()) {
                return;
            }

            newTab.setText(query.length() > 10 ? query.substring(0, 10) + "..." : query);
            searchContainer.setAlignment(Pos.TOP_LEFT);
            searchContainer.setStyle("-fx-padding: 15 0 10 50; -fx-background-color: #f8f9fa; -fx-border-color: #e4e4e4; -fx-border-width: 0 0 1 0;");
            lblLogo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");
            searchBarBox.setAlignment(Pos.CENTER_LEFT);

            tabContentRoot.setCenter(null);
            tabContentRoot.setTop(searchContainer);
            tabContentRoot.setCenter(scrollResults);

            vboxResults.getChildren().clear();
            search(query, vboxResults);
        };

        btnSearch.setOnAction(e -> searchAction.run());
        txtSearch.setOnAction(e -> searchAction.run());

        newTab.setContent(tabContentRoot);
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    private static class RestSearchResponse {
        int statusCode;
        String error;
        Object message;
        JsonElement data;
    }

    private static class ResSearchListDTO {
        String query;
        long totalResults;
        int page;
        int size;
        List<ResSearchItemDTO> items;
    }

    private static class ResSearchItemDTO {
        Long rank;
        Long index;
        String title;
        String url;
        String summary;
        String content;
        Double score;
    }
}
