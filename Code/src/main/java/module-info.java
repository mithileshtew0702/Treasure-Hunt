module com.treasurehunt {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;
    requires org.junit.jupiter.api;
    requires javafx.swing;
    requires org.mockito;
    requires json.simple;


    opens com.treasurehunt to javafx.fxml;
    exports com.treasurehunt;
}