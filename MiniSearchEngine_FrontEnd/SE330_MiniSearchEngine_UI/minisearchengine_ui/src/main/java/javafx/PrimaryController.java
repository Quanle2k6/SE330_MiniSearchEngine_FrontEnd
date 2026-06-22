package javafx;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.auth.AuthService;
import javafx.auth.HttpClientUtil;
import javafx.auth.UserSession;
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
import javafx.util.Duration;

public class PrimaryController {

    private static final String SEARCH_API_URL = "http://localhost:8080/search";
    private static final String SEARCH_HISTORY_API_URL = "http://localhost:8080/search/history";
    private static final int HISTORY_PAGE_SIZE = 50;
    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Gson gson = new Gson();
    private final List<HistoryEntry> historyEntries = new ArrayList<>();

    // FXML Bindings cho thanh công cụ dùng chung ở phía trên
    @FXML private TabPane mainTabPane;
    @FXML private TextField urlBar;
    @FXML private Button btnBack;
    @FXML private Button btnForward;
    @FXML private Button btnReload;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        // Lắng nghe sự kiện chuyển tab để cập nhật thanh URL và trạng thái nút bấm
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateGlobalControls();
        });

        createNewTab();
    }

    @FXML
    private void handleNewTab(ActionEvent event) {
        createNewTab();
    }

    @FXML
    private void handleOpenHistory(ActionEvent event) {
        openHistoryTab();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateSelectedBrowser(-1);
    }

    @FXML
    private void handleForward(ActionEvent event) {
        navigateSelectedBrowser(1);
    }

    @FXML
    private void handleReload(ActionEvent event) {
        reloadSelectedBrowser();
    }

    @FXML
    private void handleUrlInput(ActionEvent event) {
        String rawUrl = urlBar.getText().trim();
        if (rawUrl.isEmpty()) return;

        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getUserData() instanceof WebView) {
            WebView browserView = (WebView) selectedTab.getUserData();
            String normalizedUrl = normalizeUrl(rawUrl);
            loadUrl(browserView.getEngine(), normalizedUrl);
        } else {
            openResultUrl(rawUrl, "Trang web", "");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AuthService auth = new AuthService();
        var task = auth.logoutAsync(msg -> {
            Platform.runLater(() -> {
                try {
                    MainApp.setRoot("login", "Đăng nhập");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }, err -> {
            Platform.runLater(() -> {
                System.err.println("Logout failed: " + err);
                try {
                    MainApp.setRoot("login", "Đăng nhập");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void updateGlobalControls() {
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;

        Object data = selectedTab.getUserData();
        if (data instanceof WebView) {
            WebView browserView = (WebView) data;
            WebEngine engine = browserView.getEngine();
            WebHistory history = engine.getHistory();

            urlBar.setText(engine.getLocation());
            urlBar.setDisable(false);

            btnBack.setDisable(history.getCurrentIndex() <= 0);
            btnForward.setDisable(history.getCurrentIndex() >= history.getEntries().size() - 1);
            btnReload.setDisable(false);
        } else {
            // Nếu là tab Tìm kiếm hoặc Lịch sử
            urlBar.setText("");
            urlBar.setDisable(false); // Vẫn cho phép nhập url để tạo tab trình duyệt mới
            btnBack.setDisable(true);
            btnForward.setDisable(true);
            btnReload.setDisable(true);
        }
    }

    private void createNewTab() {
        createNewTab(null);
    }

    private void createNewTab(String initialQuery) {
    Tab newTab = new Tab("Tab mới");

    BorderPane tabContentRoot = new BorderPane();
    tabContentRoot.setStyle("-fx-background-color: #ffffff;");

    // ---- MÀN HÌNH TRANG CHỦ (Ban đầu xếp dọc - VBox) ----
    VBox homeContainer = new VBox(20);
    homeContainer.setAlignment(Pos.CENTER);
    homeContainer.setStyle("-fx-padding: 0 0 50 0;"); // Đẩy nhẹ trọng tâm lên trên cho đẹp

    Label lblLogo = new Label("Cạch Cạch");
    lblLogo.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");

    HBox searchBarBox = new HBox(10);
    searchBarBox.setAlignment(Pos.CENTER);

    TextField txtSearch = new TextField();
    txtSearch.setPromptText("Tìm kiếm hoặc nhập một URL...");
    txtSearch.setPrefWidth(500);
    txtSearch.setStyle("-fx-font-size: 14px; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #dfe1e5; -fx-padding: 8 15 8 15;");

    Button btnSearch = new Button("Tìm kiếm");
    btnSearch.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #f8f9fa; -fx-text-fill: #3c4043; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-background-radius: 4;");

    searchBarBox.getChildren().addAll(txtSearch, btnSearch);
    homeContainer.getChildren().addAll(lblLogo, searchBarBox);

    // Mặc định ban đầu hiển thị trang chủ ở giữa màn hình
    tabContentRoot.setCenter(homeContainer);

    // ---- MÀN HÌNH KẾT QUẢ (Chuẩn bị sẵn ScrollPane) ----
    ScrollPane scrollResults = new ScrollPane();
    scrollResults.setFitToWidth(true);
    scrollResults.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");

    VBox vboxResults = new VBox(15);
    vboxResults.setStyle("-fx-padding: 20 0 20 150;");
    scrollResults.setContent(vboxResults);

    // Thanh Header chứa Logo + Ô tìm kiếm nằm ngang khi sang trang kết quả
    HBox topResultBar = new HBox(25);
    topResultBar.setAlignment(Pos.CENTER_LEFT);
    topResultBar.setStyle("-fx-padding: 15 20 15 20; -fx-background-color: #ffffff; -fx-border-color: #e4e4e4; -fx-border-width: 0 0 1 0;");

    // Hành động xử lý khi bấm tìm kiếm hoặc nhấn Enter
    Runnable searchAction = () -> {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        // 1. Đổi tên tiêu đề Tab thành từ khóa ngắn gọn
        newTab.setText(query.length() > 10 ? query.substring(0, 10) + "..." : query);

        if (tabContentRoot.getTop() == null) {
            homeContainer.getChildren().clear();

            lblLogo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a73e8; -fx-cursor: hand;");
            
            // Đổi căn lề thanh tìm kiếm về bên trái thay vì ở giữa
            searchBarBox.setAlignment(Pos.CENTER_LEFT);
            txtSearch.setPrefWidth(600); // Kéo dài ô nhập liệu ở trang kết quả cho rộng rãi

            // Thêm Logo và Ô tìm kiếm nằm ngang sát nhau cạnh trên
            topResultBar.getChildren().addAll(lblLogo, searchBarBox);

            // Cấu trúc lại BorderPane: Thanh kiếm tìm đưa lên TOP, kết quả đưa vào CENTER
            tabContentRoot.setTop(topResultBar);
            tabContentRoot.setCenter(scrollResults);
        }

        // 3. Tiến hành xóa kết quả cũ và gọi API lấy dữ liệu mới (Từ khóa vẫn giữ nguyên trong txtSearch)
        vboxResults.getChildren().clear();
        search(query, vboxResults);
    };

    // Sự kiện Click Logo để quay lại trang chủ của Tab đó (nếu cần thiết)
    lblLogo.setOnMouseClicked(e -> {
        if (tabContentRoot.getTop() != null) {
            topResultBar.getChildren().clear();
            lblLogo.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");
            searchBarBox.setAlignment(Pos.CENTER);
            txtSearch.setPrefWidth(500);
            
            homeContainer.getChildren().addAll(lblLogo, searchBarBox);
            tabContentRoot.setTop(null);
            tabContentRoot.setCenter(homeContainer);
            newTab.setText("Tab mới");
        }
    });

    btnSearch.setOnAction(e -> searchAction.run());
    txtSearch.setOnAction(e -> searchAction.run());

    newTab.setContent(tabContentRoot);
    mainTabPane.getTabs().add(newTab);
    mainTabPane.getSelectionModel().select(newTab);

    if (initialQuery != null && !initialQuery.isBlank()) {
        txtSearch.setText(initialQuery);
        searchAction.run();
    }
}

    private void search(String query, VBox vboxResults) {
        saveQueryHistory(query);
        Label loading = new Label("Đang tìm kiếm...");
        loading.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
        vboxResults.getChildren().setAll(loading);

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20");
        String url = SEARCH_API_URL + "?query=" + encodedQuery;
        
        HttpClientUtil.getAsync(url)
                .thenAccept(response -> Platform.runLater(() -> handleSearchResponse(response, vboxResults, query)))
                .exceptionally(error -> {
                    Platform.runLater(() -> showMessage(vboxResults, "Không thể kết nối tới server localhost:8080."));
                    return null;
                });
    }

    private void handleSearchResponse(HttpResponse<String> response, VBox vboxResults, String query) {
        if (response.statusCode() != 200) {
            if(response.statusCode() == 401) {
                showMessage(vboxResults, "Bạn cần đăng nhập để thực hiện tìm kiếm.");
            } else {
                showMessage(vboxResults, "Tìm kiếm thất bại. Mã lỗi: " + response.statusCode());
                
            }
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

    private void saveQueryHistory(String query) {
        if (query == null || query.isBlank()) return;

        JsonObject body = new JsonObject();
        body.addProperty("type", "QUERY");
        body.addProperty("query", query);
        postSearchHistory(body);
    }

    private void saveUrlHistory(String title, String url) {
        if (url == null || url.isBlank()) return;

        JsonObject body = new JsonObject();
        body.addProperty("type", "URL");
        body.addProperty("title", valueOrDefault(title, "Trang web"));
        body.addProperty("url", url);
        postSearchHistory(body);
    }

    private void postSearchHistory(JsonObject body) {
        // Check if user is authenticated before posting
        if (!UserSession.getInstance().isLoggedIn()) {
            System.err.println("Save search history skipped: User not logged in");
            return;
        }

        HttpClientUtil.postAsync(SEARCH_HISTORY_API_URL, gson.toJson(body))
                .thenAccept(response -> {
                    if (response.statusCode() >= 400) {
                        System.err.println("Save search history failed: HTTP " + response.statusCode() + " - " + response.body());
                    }
                })
                .exceptionally(error -> {
                    System.err.println("Save search history failed: " + error.getMessage());
                    return null;
                });
    }

    private void openResultUrl(String rawUrl, String title, String query) {
        String url = normalizeUrl(rawUrl);
        if (url.isEmpty()) return;

        createBrowserTab(title, url);
        saveUrlHistory(title, url);
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

    private void navigateSelectedBrowser(int offset) {
        Tab selected = mainTabPane.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Object data = selected.getUserData();
        if (!(data instanceof WebView)) return;
        
        WebHistory history = ((WebView) data).getEngine().getHistory();
        int index = history.getCurrentIndex() + offset;
        if (index >= 0 && index < history.getEntries().size()) {
            history.go(offset);
        }
    }

    private void reloadSelectedBrowser() {
        Tab selected = mainTabPane.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Object data = selected.getUserData();
        if (data instanceof WebView) {
            ((WebView) data).getEngine().reload();
        }
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
        
        // Check if user is authenticated
        if (!UserSession.getInstance().isLoggedIn()) {
            Label message = new Label("Vui lòng đăng nhập để xem lịch sử tìm kiếm.");
            message.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(message);
            return;
        }
        
        boolean useApiHistory = true;
        if (useApiHistory) {
            Label loading = new Label("Đang tải lịch sử...");
            loading.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(loading);

            String url = SEARCH_HISTORY_API_URL + "?page=0&size=" + HISTORY_PAGE_SIZE;
            HttpClientUtil.getAsync(url)
                    .thenAccept(response -> Platform.runLater(() -> handleHistoryResponse(response, listContainer, filterType, filterText)))
                    .exceptionally(error -> {
                        Platform.runLater(() -> showMessage(listContainer, "Không thể tải lịch sử từ server."));
                        return null;
                    });
            return;
        }
        if (historyEntries.isEmpty()) {
            Label empty = new Label("Chưa có lịch sử truy cập.");
            empty.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(empty);
            return;
        }

        String normalizedFilter = filterText == null ? "" : filterText.trim().toLowerCase();
        for (HistoryEntry entry : historyEntries) {
            if (!matchesFilter(entry, filterType, normalizedFilter)) continue;

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
            case "Truy vấn": return query.contains(normalizedFilter);
            case "Tiêu đề": return title.contains(normalizedFilter);
            case "URL": return urlText.contains(normalizedFilter);
            default: return false;
        }
    }

    private void handleHistoryResponse(HttpResponse<String> response, VBox listContainer, String filterType, String filterText) {
        if (response.statusCode() != 200) {
            showMessage(listContainer, "Không thể tải lịch sử từ server. Mã lỗi: " + response.statusCode());
            return;
        }

        try {
            RestSearchResponse restResponse = gson.fromJson(response.body(), RestSearchResponse.class);
            if (restResponse == null || restResponse.statusCode >= 400 || restResponse.error != null) {
                showMessage(listContainer, "Không thể tải lịch sử từ server.");
                return;
            }

            renderHistoryList(listContainer, filterType, filterText, getHistoryItems(restResponse.data));
        } catch (JsonSyntaxException error) {
            showMessage(listContainer, "Dữ liệu lịch sử trả về không đúng định dạng.");
        }
    }

    private void renderHistoryList(VBox listContainer, String filterType, String filterText, List<SearchHistoryItem> historyItems) {
        listContainer.getChildren().clear();
        if (historyItems.isEmpty()) {
            Label empty = new Label("Chưa có lịch sử truy cập.");
            empty.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(empty);
            return;
        }

        String normalizedFilter = filterText == null ? "" : filterText.trim().toLowerCase();
        for (SearchHistoryItem item : historyItems) {
            if (!matchesFilter(item, filterType, normalizedFilter)) continue;

            VBox itemBox = new VBox(4);
            itemBox.setStyle("-fx-padding: 12; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #fafafa;");

            Label labelTime = new Label(formatVisitedAt(item.visitedAt));
            labelTime.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 12px;");

            Label labelType = new Label("Loại: " + valueOrDefault(item.type, ""));
            labelType.setStyle("-fx-text-fill: #202124; -fx-font-size: 13px;");

            Label labelTitle = new Label(getHistoryLabel(item));
            labelTitle.setStyle("-fx-text-fill: #1a73e8; -fx-font-size: 14px; -fx-cursor: hand;");
            labelTitle.setWrapText(true);
            labelTitle.setOnMouseClicked(e -> openHistoryItem(item));

            Label labelUrl = new Label(valueOrDefault(item.url, ""));
            labelUrl.setWrapText(true);
            labelUrl.setStyle("-fx-text-fill: #202124; -fx-font-size: 12px;");

            itemBox.getChildren().addAll(labelTime, labelType, labelTitle, labelUrl);
            listContainer.getChildren().add(itemBox);
        }

        if (listContainer.getChildren().isEmpty()) {
            Label empty = new Label("Không tìm thấy lịch sử phù hợp với bộ lọc.");
            empty.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(empty);
        }
    }

    private boolean matchesFilter(SearchHistoryItem item, String filterType, String filterText) {
        String normalizedFilter = filterText == null ? "" : filterText.trim().toLowerCase();
        String query = valueOrDefault(item.query, "").toLowerCase();
        String title = valueOrDefault(item.title, "").toLowerCase();
        String urlText = valueOrDefault(item.url, "").toLowerCase();

        if (normalizedFilter.isEmpty() || isAllFilter(filterType)) {
            return normalizedFilter.isEmpty() || query.contains(normalizedFilter) || title.contains(normalizedFilter) || urlText.contains(normalizedFilter);
        }

        if (isQueryFilter(filterType)) return query.contains(normalizedFilter);
        if (isTitleFilter(filterType)) return title.contains(normalizedFilter);
        if ("URL".equals(filterType)) return urlText.contains(normalizedFilter);
        return false;
    }

    private boolean isAllFilter(String filterType) {
        return filterType == null || (!isQueryFilter(filterType) && !isTitleFilter(filterType) && !"URL".equals(filterType));
    }

    private boolean isQueryFilter(String filterType) {
        return filterType != null && filterType.startsWith("Tr");
    }

    private boolean isTitleFilter(String filterType) {
        return filterType != null && (filterType.startsWith("Ti") || filterType.startsWith("TiÃ"));
    }

    private String getHistoryLabel(SearchHistoryItem item) {
        if ("QUERY".equals(item.type)) {
            return "Query: " + valueOrDefault(item.query, "");
        }
        return "Title: " + valueOrDefault(item.title, "Trang web");
    }

    private String formatVisitedAt(String visitedAt) {
        return valueOrDefault(visitedAt, "").replace('T', ' ');
    }

    private void openHistoryItem(SearchHistoryItem item) {
        if ("QUERY".equals(item.type)) {
            String query = valueOrDefault(item.query, "");
            if (!query.isEmpty()) createNewTab(query);
            return;
        }

        if ("URL".equals(item.type)) {
            String url = normalizeUrl(item.url);
            if (!url.isEmpty()) createBrowserTab(valueOrDefault(item.title, "Trang web"), url);
        }
    }

    private WebEngine createBrowserTab(String title, String url) {
        return createBrowserTab(title, url, false);
    }

    private WebEngine createBrowserTab(String title, String url, boolean recordInitialLoad) {
        Tab browserTab = new Tab(title == null || title.isEmpty() ? "Trang web" : (title.length() > 18 ? title.substring(0, 18) + "..." : title));

        BorderPane browserRoot = new BorderPane();
        browserRoot.setStyle("-fx-background-color: #ffffff;");

        WebView browserView = new WebView();
        browserTab.setUserData(browserView);
        WebEngine engine = browserView.getEngine();
        final boolean[] hasCompletedInitialLoad = { false };
        final String[] pendingHistoryUrl = { null };
        final String[] lastSavedHistoryUrl = { null };
        PauseTransition historySaveDelay = new PauseTransition(Duration.millis(1200));
        Runnable savePendingHistory = () -> {
            String currentUrl = engine.getLocation();
            if (!isRecordableUrl(currentUrl)) {
                currentUrl = pendingHistoryUrl[0];
            }

            if (pendingHistoryUrl[0] != null
                    && isRecordableUrl(currentUrl)
                    && !isSameUrl(lastSavedHistoryUrl[0], currentUrl)) {
                saveUrlHistory(valueOrDefault(engine.getTitle(), "Trang web"), currentUrl);
                lastSavedHistoryUrl[0] = currentUrl;
            }

            pendingHistoryUrl[0] = null;
        };
        historySaveDelay.setOnFinished(event -> {
            Worker.State state = engine.getLoadWorker().getState();
            if (state != Worker.State.SCHEDULED && state != Worker.State.RUNNING) {
                savePendingHistory.run();
            }
        });

        engine.setCreatePopupHandler(popupFeatures -> createBrowserTab("Trang web", null, true));

        // Cập nhật trạng thái cho tab và thanh công cụ khi WebEngine tải dữ liệu
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                String pageTitle = engine.getTitle();
                if (pageTitle != null) {
                    browserTab.setText(pageTitle.length() > 18 ? pageTitle.substring(0, 18) + "..." : pageTitle);
                }
                
                if (mainTabPane.getSelectionModel().getSelectedItem() == browserTab) {
                    updateGlobalControls();
                }

                historySaveDelay.stop();
                savePendingHistory.run();

                hasCompletedInitialLoad[0] = true;
            } else if (newState == Worker.State.FAILED || newState == Worker.State.CANCELLED) {
                historySaveDelay.stop();
                pendingHistoryUrl[0] = null;
            }
        });

        // Lắng nghe khi đường dẫn thay đổi (ví dụ người dùng click vào link trên web)
        engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (mainTabPane.getSelectionModel().getSelectedItem() == browserTab) {
                urlBar.setText(newLoc);
            }

            if ((hasCompletedInitialLoad[0] || recordInitialLoad)
                    && isRecordableUrl(newLoc)
                    && !isSameUrl(oldLoc, newLoc)) {
                pendingHistoryUrl[0] = newLoc;
                historySaveDelay.playFromStart();
            }
        });

        browserRoot.setCenter(browserView);
        browserTab.setContent(browserRoot);

        mainTabPane.getTabs().add(browserTab);
        mainTabPane.getSelectionModel().select(browserTab);

        loadUrl(engine, url);
        return engine;
    }

    private void loadUrl(WebEngine engine, String rawUrl) {
        String normalized = normalizeUrl(rawUrl);
        if (normalized.isEmpty()) return;
        engine.load(normalized);
    }

    private boolean isRecordableUrl(String url) {
        return url != null && url.matches("(?i)^https?://.*");
    }

    private boolean isSameUrl(String firstUrl, String secondUrl) {
        if (firstUrl == null || secondUrl == null) return false;
        return firstUrl.equals(secondUrl);
    }

    private String normalizeUrl(String rawUrl) {
        String url = valueOrDefault(rawUrl, "").trim();
        if (url.isEmpty()) return "";
        if (!url.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) return "https://" + url;
        return url;
    }

    private List<ResSearchItemDTO> getSearchItems(JsonElement data) {
        if (data == null || data.isJsonNull()) return Collections.emptyList();
        if (data.isJsonArray()) {
            return gson.fromJson(data, new TypeToken<List<ResSearchItemDTO>>() {}.getType());
        }
        if (data.isJsonObject()) {
            JsonObject dataObject = data.getAsJsonObject();
            JsonElement items = dataObject.get("items");
            if (items != null && items.isJsonArray()) {
                return gson.fromJson(items, new TypeToken<List<ResSearchItemDTO>>() {}.getType());
            }
            ResSearchItemDTO item = gson.fromJson(dataObject, ResSearchItemDTO.class);
            return item == null ? Collections.emptyList() : Collections.singletonList(item);
        }
        return Collections.emptyList();
    }

    private List<SearchHistoryItem> getHistoryItems(JsonElement data) {
        if (data == null || data.isJsonNull()) return Collections.emptyList();
        if (data.isJsonArray()) {
            return gson.fromJson(data, new TypeToken<List<SearchHistoryItem>>() {}.getType());
        }
        if (data.isJsonObject()) {
            JsonElement items = data.getAsJsonObject().get("items");
            if (items != null && items.isJsonArray()) {
                return gson.fromJson(items, new TypeToken<List<SearchHistoryItem>>() {}.getType());
            }
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

    private static class SearchHistoryItem {
        Long id;
        String type;
        String visitedAt;
        String query;
        String title;
        String url;
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
