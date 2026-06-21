error id: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/PrimaryController.java:_empty_/Tab#getUserData#
file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/PrimaryController.java
empty definition using pc, found symbol in pc: _empty_/Tab#getUserData#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 5660
uri: file:///D:/UIT/SE330/DA/SE330_MiniSearchEngine_FrontEnd/MiniSearchEngine_FrontEnd/SE330_MiniSearchEngine_UI/minisearchengine_ui/src/main/java/javafx/PrimaryController.java
text:
```scala
package javafx;

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
import java.util.Stack;
import java.util.function.Consumer;

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
import javafx.scene.Node;
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
import javafx.scene.web.WebView;

public class PrimaryController {

    private static final String SEARCH_API_URL = "http://localhost:8080/search";
    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final List<HistoryEntry> globalHistoryEntries = new ArrayList<>();

    @FXML private TabPane mainTabPane;
    @FXML private TextField urlBar;
    @FXML private Button btnBack;
    @FXML private Button btnForward;
    @FXML private Button btnReload;

    @FXML
    public void initialize() {
        // Lắng nghe sự kiện đổi tab để cập nhật lại trạng thái các nút Back/Forward trên thanh công cụ dùng chung
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
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null || !(selectedTab.getUserData() instanceof TabContext)) return;

        TabContext context = (TabContext) selectedTab.getUserData();
        if (context.backStack.isEmpty()) return;

        // Đẩy trạng thái hiện tại sang forwardStack trước khi lùi lại
        context.forwardStack.push(context.currentState);
        
        // Lấy trạng thái cũ ra và khôi phục giao diện
        TabState previousState = context.backStack.pop();
        context.currentState = previousState;

        restoreTabState(selectedTab, previousState);
        updateGlobalControls();
    }

    @FXML
    private void handleForward(ActionEvent event) {
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null || !(selectedTab.getUserData() instanceof TabContext)) return;

        TabContext context = (TabContext) selectedTab.getUserData();
        if (context.forwardStack.isEmpty()) return;

        // Đẩy trạng thái hiện tại vào lại backStack
        context.backStack.push(context.currentState);

        // Lấy trạng thái phía trước ra và khôi phục
        TabState nextState = context.forwardStack.pop();
        context.currentState = nextState;

        restoreTabState(selectedTab, nextState);
        updateGlobalControls();
    }

    @FXML
    private void handleReload(ActionEvent event) {
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null || !(selectedTab.getUserData() instanceof TabContext)) return;

        TabContext context = (TabContext) selectedTab.getUserData();
        if (context.currentState.type == StateType.WEB_VIEW && context.webView != null) {
            context.webView.getEngine().reload();
        } else if (context.currentState.type == StateType.SEARCH_RESULT) {
            // Thực hiện tìm kiếm lại với query cũ để làm mới dữ liệu
            restoreTabState(selectedTab, context.currentState);
        }
    }

    @FXML
    private void handleUrlInput(ActionEvent event) {
        String rawUrl = urlBar.getText().trim();
        if (rawUrl.isEmpty()) return;

        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getUserData() instanceof TabContext) {
            // Điều hướng URL ngay trên Tab đang chọn
            navigateToNewState(selectedTab, new TabState(StateType.WEB_VIEW, normalizeUrl(rawUrl), null));
        } else {
            // Nếu không có tab hợp lệ, mở URL ở một tab trình duyệt mới hoàn toàn
            openResultUrl(rawUrl, "Trang web", "");
        }
    }

    private void updateGlobalControls() {
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null || !(selectedTab.getUserData() instanceof TabContext)) {
            setToolbarDisabled(true, true, true, "");
            return;
        }

        TabContext context = (TabContext) selectedTab.@@getUserData();
        
        // Cập nhật nút Back / Forward dựa trên size của 2 Stack cục bộ trong Tab
        btnBack.setDisable(context.backStack.isEmpty());
        btnForward.setDisable(context.forwardStack.isEmpty());

        // Thiết lập thanh URL hiển thị và nút Reload dựa trên loại màn hình hiện tại của Tab
        if (context.currentState.type == StateType.WEB_VIEW) {
            urlBar.setText(context.currentState.value);
            urlBar.setDisable(false);
            btnReload.setDisable(false);
        } else if (context.currentState.type == StateType.SEARCH_RESULT) {
            urlBar.setText("Search: " + context.currentState.value);
            urlBar.setDisable(false);
            btnReload.setDisable(false);
        } else if (context.currentState.type == StateType.HISTORY) {
            urlBar.setText("app://history");
            urlBar.setDisable(true);
            btnReload.setDisable(true);
        } else { // Màn hình HOME ban đầu
            urlBar.setText("");
            urlBar.setDisable(false);
            btnReload.setDisable(true);
        }
    }

    private void setToolbarDisabled(boolean back, boolean forward, boolean reload, String urlText) {
        btnBack.setDisable(back);
        btnForward.setDisable(forward);
        btnReload.setDisable(reload);
        urlBar.setText(urlText);
    }

    // Hàm thực hiện chuyển trạng thái mới (Khi người dùng chủ động nhấn Tìm kiếm hoặc click vào một Link)
    private void navigateToNewState(Tab tab, TabState newState) {
        TabContext context = (TabContext) tab.getUserData();
        
        // Đẩy trạng thái hiện tại vào Back Stack
        context.backStack.push(context.currentState);
        // Khi có hành động mới, Forward Stack cũ sẽ bị xóa hoàn toàn theo quy tắc trình duyệt
        context.forwardStack.clear();
        
        // Thiết lập trạng thái hiện tại thành trạng thái mới
        context.currentState = newState;
        
        // Khôi phục hiển thị giao diện tương ứng với trạng thái mới
        restoreTabState(tab, newState);
        updateGlobalControls();
    }

    // Cơ chế khôi phục hoặc render giao diện dựa vào cấu trúc TabState được lấy ra từ Stack
    private void restoreTabState(Tab tab, TabState state) {
        TabContext context = (TabContext) tab.getUserData();
        BorderPane root = context.rootNode;

        switch (state.type) {
            case HOME:
                tab.setText("Tab mới");
                root.setTop(null);
                root.setCenter(context.homeContainer);
                context.txtSearch.setText("");
                break;

            case SEARCH_RESULT:
                String query = state.value;
                tab.setText(query.length() > 10 ? query.substring(0, 10) + "..." : query);
                context.txtSearch.setText(query);
                
                // Cấu hình thanh tìm kiếm nhỏ thu gọn đưa lên Top nếu nó đang ở giữa màn hình
                if (root.getTop() == null) {
                    context.homeContainer.getChildren().clear();
                    context.lblLogo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a73e8; -fx-cursor: hand;");
                    context.searchBarBox.setAlignment(Pos.CENTER_LEFT);
                    context.txtSearch.setPrefWidth(600);
                    context.topResultBar.getChildren().addAll(context.lblLogo, context.searchBarBox);
                    root.setTop(context.topResultBar);
                }
                
                root.setCenter(context.scrollResults);
                context.vboxResults.getChildren().clear();
                // Thực hiện gọi API tải dữ liệu tìm kiếm
                search(query, context.vboxResults);
                break;

            case WEB_VIEW:
                String url = state.value;
                tab.setText(state.title != null ? state.title : "Đang tải...");
                
                if (context.webView == null) {
                    context.webView = new WebView();
                    WebEngine engine = context.webView.getEngine();
                    
                    // Lắng nghe khi tải trang thành công để cập nhật lại title của tab và thanh URL gán trong Stack
                    engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            String pageTitle = engine.getTitle();
                            if (pageTitle != null) {
                                state.title = pageTitle;
                                tab.setText(pageTitle.length() > 18 ? pageTitle.substring(0, 18) + "..." : pageTitle);
                            }
                            if (mainTabPane.getSelectionModel().getSelectedItem() == tab) {
                                updateGlobalControls();
                            }
                        }
                    });

                    // Theo dõi sự thay đổi URL nội bộ bên trong WebView (khi người dùng click link trong trang web)
                    engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
                        if (mainTabPane.getSelectionModel().getSelectedItem() == tab && !newLoc.equals(context.currentState.value)) {
                            // Khi click link sâu vào web, tạo một trạng thái WEB_VIEW mới đưa vào Stack điều hướng
                            Platform.runLater(() -> navigateToNewState(tab, new TabState(StateType.WEB_VIEW, newLoc, engine.getTitle())));
                        }
                    });
                }
                
                root.setTop(null); // Trình duyệt WebView toàn màn hình, che thanh search bar nội bộ
                root.setCenter(context.webView);
                
                // Chỉ nạp lại URL nếu WebView chưa ở đúng trang đó
                if (!url.equals(context.webView.getEngine().getLocation())) {
                    context.webView.getEngine().load(url);
                }
                break;

            case HISTORY:
                tab.setText("Lịch sử truy cập");
                root.setTop(null);
                root.setCenter(createHistoryContent());
                break;
        }
    }

    private void createNewTab() {
        Tab newTab = new Tab("Tab mới");
        TabContext context = new TabContext();
        newTab.setUserData(context);

        context.rootNode.setStyle("-fx-background-color: #ffffff;");

        // ---- THIẾT KẾ MÀN HÌNH TRANG CHỦ BAN ĐẦU (HOME) ----
        context.homeContainer.setAlignment(Pos.CENTER);
        context.homeContainer.setStyle("-fx-padding: 0 0 50 0;");

        context.lblLogo.setText("Google");
        context.lblLogo.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");

        context.searchBarBox.setAlignment(Pos.CENTER);

        context.txtSearch.setPromptText("Tìm kiếm trên Mini Google hoặc nhập một URL...");
        context.txtSearch.setPrefWidth(500);
        context.txtSearch.setStyle("-fx-font-size: 14px; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #dfe1e5; -fx-padding: 8 15 8 15;");

        Button btnSearch = new Button("Tìm kiếm");
        btnSearch.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #f8f9fa; -fx-text-fill: #3c4043; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-background-radius: 4;");

        context.searchBarBox.getChildren().addAll(context.txtSearch, btnSearch);
        context.homeContainer.getChildren().addAll(context.lblLogo, context.searchBarBox);

        context.rootNode.setCenter(context.homeContainer);

        // ---- THIẾT KẾ KHUNG CHỨA KẾT QUẢ TÌM KIẾM (SEARCH_RESULT) ----
        context.scrollResults.setFitToWidth(true);
        context.scrollResults.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");

        context.vboxResults.setStyle("-fx-padding: 20 0 20 150;");
        context.scrollResults.setContent(context.vboxResults);

        context.topResultBar.setAlignment(Pos.CENTER_LEFT);
        context.topResultBar.setStyle("-fx-padding: 15 20 15 20; -fx-background-color: #ffffff; -fx-border-color: #e4e4e4; -fx-border-width: 0 0 1 0;");

        // Hành động kích hoạt khi nhấn Enter hoặc bấm tìm kiếm
        Runnable searchAction = () -> {
            String query = context.txtSearch.getText().trim();
            if (query.isEmpty()) return;

            // Kiểm tra nếu người dùng cố tình gõ link trực tiếp vào thanh search
            if (query.matches("(?i)^(https?://)?([a-z0-9-]+\\.)+[a-z]{2,}.*")) {
                navigateToNewState(newTab, new TabState(StateType.WEB_VIEW, normalizeUrl(query), null));
            } else {
                navigateToNewState(newTab, new TabState(StateType.SEARCH_RESULT, query, null));
            }
        };

        // Sự kiện Click Logo quay lại màn hình Home ban đầu của tab và đưa vào lịch sử Stack
        context.lblLogo.setOnMouseClicked(e -> {
            if (context.currentState.type != StateType.HOME) {
                // Đóng thanh tìm kiếm Top thu gọn lại và đưa về màn hình Home gốc ban đầu
                context.topResultBar.getChildren().clear();
                context.lblLogo.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");
                context.searchBarBox.setAlignment(Pos.CENTER);
                context.txtSearch.setPrefWidth(500);
                
                context.homeContainer.getChildren().addAll(context.lblLogo, context.searchBarBox);
                navigateToNewState(newTab, new TabState(StateType.HOME, "", null));
            }
        });

        btnSearch.setOnAction(e -> searchAction.run());
        context.txtSearch.setOnAction(e -> searchAction.run());

        newTab.setContent(context.rootNode);
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
        
        updateGlobalControls();
    }

    private void search(String query, VBox vboxResults) {
        addSearchHistory(query);
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
        
        // Nhấp vào tiêu đề bài viết để mở link sang màn hình WebView điều hướng mới
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
        if (url.isEmpty()) return;

        addGlobalHistoryEntry(title, url, query);
        
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getUserData() instanceof TabContext) {
            navigateToNewState(selectedTab, new TabState(StateType.WEB_VIEW, url, title));
        }
    }

    private void addGlobalHistoryEntry(String title, String url, String query) {
        globalHistoryEntries.add(0, new HistoryEntry(LocalDateTime.now(), query, title, url));
    }

    private void openHistoryTab() {
        // Nếu đã có tab lịch sử đang mở, chỉ cần kích hoạt chọn nó
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getUserData() instanceof TabContext) {
                TabContext ctx = (TabContext) tab.getUserData();
                if (ctx.currentState.type == StateType.HISTORY) {
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }
        }
        
        // Nếu chưa có, tạo tab mới chứa tính năng Lịch sử ứng dụng
        Tab historyTab = new Tab("Lịch sử truy cập");
        TabContext context = new TabContext();
        context.currentState = new TabState(StateType.HISTORY, "app://history", "Lịch sử truy cập");
        historyTab.setUserData(context);
        
        context.rootNode.setStyle("-fx-background-color: #ffffff; -fx-padding: 15;");
        context.rootNode.setCenter(createHistoryContent());
        
        mainTabPane.getTabs().add(historyTab);
        mainTabPane.getSelectionModel().select(historyTab);
        updateGlobalControls();
    }

    private void addSearchHistory(String query) {
        if (query == null || query.isBlank()) return;
        globalHistoryEntries.add(0, new HistoryEntry(LocalDateTime.now(), query, "Search: " + query, ""));
    }

    // Thiết kế phần khung hiển thị lịch sử ứng dụng (tách biệt để render lại bất kỳ khi nào gọi)
    private Node createHistoryContent() {
        BorderPane contentPane = new BorderPane();
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-padding: 0 0 10 0;");

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
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        contentPane.setTop(filterBar);
        contentPane.setCenter(scrollPane);

        Consumer<Void> refresh = ignored -> refreshHistoryList(listContainer, filterType.getValue(), filterField.getText().trim());
        btnFilter.setOnAction(e -> refresh.accept(null));
        filterField.setOnAction(e -> refresh.accept(null));
        filterType.setOnAction(e -> refresh.accept(null));

        refreshHistoryList(listContainer, filterType.getValue(), filterField.getText().trim());
        return contentPane;
    }

    private void refreshHistoryList(VBox listContainer, String filterType, String filterText) {
        listContainer.getChildren().clear();
        if (globalHistoryEntries.isEmpty()) {
            Label empty = new Label("Chưa có lịch sử truy cập.");
            empty.setStyle("-fx-text-fill: #4d5156; -fx-font-size: 14px;");
            listContainer.getChildren().add(empty);
            return;
        }

        String normalizedFilter = filterText == null ? "" : filterText.trim().toLowerCase();
        for (HistoryEntry entry : globalHistoryEntries) {
            if (!matchesFilter(entry, filterType, normalizedFilter)) continue;

            VBox itemBox = new VBox(4);
            itemBox.setStyle("-fx-padding: 12; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #fafafa;");

            Label labelTime = new Label(HISTORY_FORMATTER.format(entry.visitedAt));
            labelTime.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 12px;");

            Label labelQuery = new Label("Truy vấn: " + valueOrDefault(entry.query, "(Không có)"));
            labelQuery.setStyle("-fx-text-fill: #202124; -fx-font-size: 13px;");

            Label labelTitle = new Label("Tiêu đề: " + valueOrDefault(entry.title, "(Không có tiêu đề)"));
            labelTitle.setStyle("-fx-text-fill: #1a73e8; -fx-font-size: 14px; -fx-cursor: hand;");
            
            // Khi nhấn vào một liên kết cũ trong lịch sử, mở nó thành một trạng thái WebView mới
            labelTitle.setOnMouseClicked(e -> {
                Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
                if (selectedTab != null && selectedTab.getUserData() instanceof TabContext) {
                    navigateToNewState(selectedTab, new TabState(StateType.WEB_VIEW, entry.url, entry.title));
                }
            });

            Label labelUrl = new Label(valueOrDefault(entry.url, ""));
            labelUrl.setWrapText(true);
            labelUrl.setStyle("-fx-text-fill: #202124; -fx-font-size: 12px;");

            itemBox.getChildren().addAll(labelTime, labelQuery, labelTitle, labelUrl);
            listContainer.getChildren().add(itemBox);
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

    // ---- ĐỊNH NGHĨA CÁC ĐỐI TƯỢNG TRẠNG THÁI (STATE MANAGEMENT CLASSES) ----

    private enum StateType {
        HOME, SEARCH_RESULT, WEB_VIEW, HISTORY
    }

    // Lưu trữ thông tin vết trạng thái để đẩy vào 2 Stack
    private static class TabState {
        StateType type;
        String value; // Chứa Query tìm kiếm hoặc URL web tương ứng
        String title; // Tiêu đề trang Web (nếu có)

        TabState(StateType type, String value, String title) {
            this.type = type;
            this.value = value;
            this.title = title;
        }
    }

    // Class quản lý cấu trúc dữ liệu 2 Stacks cục bộ ứng với vòng đời của mỗi Tab riêng biệt
    private static class TabContext {
        final Stack<TabState> backStack = new Stack<>();
        final Stack<TabState> forwardStack = new Stack<>();
        
        // Trạng thái mặc định ban đầu khi tạo tab là màn hình HOME trống
        TabState currentState = new TabState(StateType.HOME, "", "Tab mới");

        // Giữ lại cấu trúc các View Node để chuyển đổi Layout linh hoạt
        final BorderPane rootNode = new BorderPane();
        final VBox homeContainer = new VBox(20);
        final Label lblLogo = new Label();
        final HBox searchBarBox = new HBox(10);
        final TextField txtSearch = new TextField();
        final ScrollPane scrollResults = new ScrollPane();
        final VBox vboxResults = new VBox(15);
        final HBox topResultBar = new HBox(25);
        
        WebView webView = null; // WebView sẽ chỉ khởi tạo lazy-load khi nào người dùng vào web
    }

    private static class RestSearchResponse {
        int statusCode;
        String error;
        Object message;
        JsonElement data;
    }

    private static class ResSearchItemDTO {
        String title;
        String url;
        String summary;
        String content;
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

empty definition using pc, found symbol in pc: _empty_/Tab#getUserData#