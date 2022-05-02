module com.example.mazesolver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mazesolver to javafx.fxml;
    exports com.example.mazesolver;
}