module javafx {
    requires com.google.gson;
    requires java.net.http;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    opens javafx to com.google.gson, javafx.fxml;
    exports javafx;
}
