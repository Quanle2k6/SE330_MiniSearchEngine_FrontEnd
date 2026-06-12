error id: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/PrimaryController.java:javafx/PrimaryController#createNewTab#
file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/PrimaryController.java
empty definition using pc, found symbol in pc: javafx/PrimaryController#createNewTab#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1848
uri: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/PrimaryController.java
text:
```scala
package javafx;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PrimaryController {

    private static final String SEARCH_API_URL = "http://localhost:8080/search";
    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final List<HistoryEntry> historyEntries = new ArrayList<>();

    @FXML
    private TabPane mainTabPane;

    @FXML
    public void initialize() {
        @@createNewTab();
    }

    @FXML
    private void handleNewTab(ActionEvent event) {
        createNewTab();
    }

    @FXML
    private void handleOpenHistory(ActionEvent event) {
        openHistoryTab();
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
                .thenAccept(response -> Platform.runLater(() -> handleSearchResponse(response, vboxResults, query)))
                .exceptionally(error -> {
                    Platform.runLater(() -> showMessage(vboxResults, "Không thể kết nối tới server localhost:8080."));
                    return null;
                });
    }

    private void handleSearchResponse(HttpResponse<String> response, VBox vboxResults, String query) {
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
                vboxResults.getChildren().add(createResultCard(item, query));
            }
        } catch (JsonSyntaxException error) {
            showMessage(vboxResults, "Dữ liệu trả về từ server không đúng định dạng.");
        }
    }

    private VBox createResultCard(ResSearchItemDTO item, String query) {
        VBox card = new VBox(4);

        Label title = new Label(valueOrDefault(item.title, "(Không có tiêu đề)"));
        title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand;");
        title.setWrapText(true);
        title.setOnMouseEntered(e -> title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand; -fx-underline: true;"));
        title.setOnMouseExited(e -> title.setStyle("-fx-text-fill: #1a0dab; -fx-font-size: 16px; -fx-cursor: hand; -fx-underline: false;"));
        title.setOnMouseClicked(e -> openResultUrl(item.url, item.title, query));

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

    private void openResultUrl(String rawUrl, String title, String query) {
        String url = normalizeUrl(rawUrl);
        if (url.isEmpty()) {
            return;
        }

        addHistoryEntry(title, url, query);
        createBrowserTab(title, url);
    }

    private void addHistoryEntry(String title, String url, String query) {
        historyEntries.add(0, new HistoryEntry(LocalDateTime.now(), query, title, url));
    }

    private void openHistoryTab() {
        for (Tab tab : mainTabPane.getTabs()) {
            if ("Lịch sử truy cập".equals(tab.getText())) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }
        mainTabPane.getTabs().add(createHistoryTab());
        mainTabPane.getSelectionModel().selectLast();
    }

    private Tab createHistoryTab() {
        Tab historyTab = new Tab("Lịch sử truy cập");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff; -fx-padding: 15;");

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> filterType = new ComboBox<>(FXCollections.observableArrayList("Tất cả", "Truy vấn", "Tiêu đề", "URL"));
        filterType.setValue("Tất cả");
        filterType.setPrefWidth(120);

        TextField filterField = new TextField();
        filterField.setPromptText("Nhập từ khóa lọc...");
        filterField.setPrefWidth(280);
        filterField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dfe1e5; -fx-padding: 6 10 6 10;");

        Button btnFilter = new Button("Lọc");
        btnFilter.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");

        filterBar.getChildren().addAll(filterType, filterField, btnFilter);

        VBox listContainer = new VBox(12);
        listContainer.setStyle("-fx-padding: 10 0 0 0;");

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        root.setTop(filterBar);
        root.setCenter(scrollPane);

        Consumer<Void> refresh = ignored -> refreshHistoryList(listContainer, filterType.getValue(), filterField.getText().trim());
        btnFilter.setOnAction(e -> refresh.accept(null));
        filterField.setOnAction(e -> refresh.accept(null));
        filterType.setOnAction(e -> refresh.accept(null));

        refreshHistoryList(listContainer, filterType.getValue(), filterField.getText().trim());

        historyTab.setContent(root);
        return historyTab;
    }

    private void refreshHistoryList(VBox listContainer, String filterType, String filterText) {
        listContainer.getChildren().clear();
        if (historyEntries.isEmpty()) {
            Label empty = new Label("Chưa có lịch sử truy cập.");
            empty.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(empty);
            return;
        }

        String normalizedFilter = filterText == null ? "" : filterText.trim().toLowerCase();
        for (HistoryEntry entry : historyEntries) {
            if (!matchesFilter(entry, filterType, normalizedFilter)) {
                continue;
            }

            VBox itemBox = new VBox(4);
            itemBox.setStyle("-fx-padding: 12; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #fafafa;");

            Label labelTime = new Label(HISTORY_FORMATTER.format(entry.visitedAt));
            labelTime.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 12px;");

            Label labelQuery = new Label("Truy vấn: " + valueOrDefault(entry.query, "(Không có)"));
            labelQuery.setStyle("-fx-text-fill: #202124; -fx-font-size: 13px;");

            Label labelTitle = new Label("Tiêu đề: " + valueOrDefault(entry.title, "(Không có tiêu đề)"));
            labelTitle.setStyle("-fx-text-fill: #1a73e8; -fx-font-size: 14px; -fx-cursor: hand;");
            labelTitle.setOnMouseClicked(e -> createBrowserTab(entry.title, entry.url));

            Label labelUrl = new Label(valueOrDefault(entry.url, ""));
            labelUrl.setWrapText(true);
            labelUrl.setStyle("-fx-text-fill: #202124; -fx-font-size: 12px;");

            itemBox.getChildren().addAll(labelTime, labelQuery, labelTitle, labelUrl);
            listContainer.getChildren().add(itemBox);
        }

        if (listContainer.getChildren().isEmpty()) {
            Label empty = new Label("Không tìm thấy lịch sử phù hợp với bộ lọc.");
            empty.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(empty);
        }
    }

    private boolean matchesFilter(HistoryEntry entry, String filterType, String filterText) {
        String normalizedFilter = filterText == null ? "" : filterText.trim().toLowerCase();
        String query = valueOrDefault(entry.query, "").toLowerCase();
        String title = valueOrDefault(entry.title, "").toLowerCase();
        String urlText = valueOrDefault(entry.url, "").toLowerCase();

        if (normalizedFilter.isEmpty() || "Tất cả".equals(filterType)) {
            return normalizedFilter.isEmpty() || query.contains(normalizedFilter) || title.contains(normalizedFilter) || urlText.contains(normalizedFilter);
        }

        switch (filterType) {
            case "Truy vấn":
                return query.contains(normalizedFilter);
            case "Tiêu đề":
                return title.contains(normalizedFilter);
            case "URL":
                return urlText.contains(normalizedFilter);
            default:
                return false;
        }
    }

    private void createBrowserTab(String title, String url) {
        Tab browserTab = new Tab(title == null || title.isEmpty() ? "Trang web" : (title.length() > 18 ? title.substring(0, 18) + "..." : title));

        BorderPane browserRoot = new BorderPane();
        browserRoot.setStyle("-fx-background-color: #ffffff;");

        HBox browserBar = new HBox(8);
        browserBar.setAlignment(Pos.CENTER_LEFT);
        browserBar.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-border-color: #e4e4e4; -fx-border-width: 0 0 1 0;");

        Button btnBack = new Button("Back");
        Button btnForward = new Button("Forward");
        Button btnReload = new Button("Reload");
        TextField urlBar = new TextField(url);
        urlBar.setPrefWidth(420);
        Button btnGo = new Button("Đi đến");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 12px;");

        browserBar.getChildren().addAll(btnBack, btnForward, btnReload, urlBar, btnGo, statusLabel);

        WebView browserView = new WebView();
        WebEngine engine = browserView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                urlBar.setText(engine.getLocation());
                statusLabel.setText("Đã tải xong");
                updateNavigationButtons(engine.getHistory(), btnBack, btnForward);
            } else if (newState == Worker.State.RUNNING) {
                statusLabel.setText("Đang tải...");
            } else if (newState == Worker.State.FAILED) {
                statusLabel.setText("Không thể tải trang");
            }
        });

        btnBack.setOnAction(e -> {
            WebHistory history = engine.getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
            }
        });

        btnForward.setOnAction(e -> {
            WebHistory history = engine.getHistory();
            if (history.getCurrentIndex() + 1 < history.getEntries().size()) {
                history.go(1);
            }
        });

        btnReload.setOnAction(e -> engine.reload());
        btnGo.setOnAction(e -> loadUrl(engine, urlBar.getText().trim(), urlBar, statusLabel));
        urlBar.setOnAction(e -> loadUrl(engine, urlBar.getText().trim(), urlBar, statusLabel));

        browserRoot.setTop(browserBar);
        browserRoot.setCenter(browserView);
        browserTab.setContent(browserRoot);

        mainTabPane.getTabs().add(browserTab);
        mainTabPane.getSelectionModel().select(browserTab);

        loadUrl(engine, url, urlBar, statusLabel);
    }

    private void loadUrl(WebEngine engine, String rawUrl, TextField urlBar, Label statusLabel) {
        String normalized = normalizeUrl(rawUrl);
        urlBar.setText(normalized);
        if (normalized.isEmpty()) {
            statusLabel.setText("URL không hợp lệ");
            return;
        }
        engine.load(normalized);
    }

    private void updateNavigationButtons(WebHistory history, Button btnBack, Button btnForward) {
        btnBack.setDisable(history.getCurrentIndex() <= 0);
        btnForward.setDisable(history.getCurrentIndex() >= history.getEntries().size() - 1);
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

    private static class HistoryEntry {
        LocalDateTime visitedAt;
        String query;
        String title;
        String url;

        HistoryEntry(LocalDateTime visitedAt, String query, String title, String url) {
            this.visitedAt = visitedAt;
            this.query = query;
            this.title = title;
            this.url = url;
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: javafx/PrimaryController#createNewTab#