module javafx {
    requires com.google.gson;
    requires java.desktop;
    requires java.net.http;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.web;
    requires java.prefs;
    opens javafx to com.google.gson, javafx.fxml;
    opens javafx.auth to com.google.gson;
    exports javafx;
}
