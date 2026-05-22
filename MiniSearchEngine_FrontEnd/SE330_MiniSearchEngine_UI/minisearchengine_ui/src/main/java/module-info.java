module javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    opens javafx to javafx.fxml;
    exports javafx;
}