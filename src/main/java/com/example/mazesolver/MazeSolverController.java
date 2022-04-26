package com.example.mazesolver;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MazeSolverController {
    @FXML
    private Label welcomeText;

    public void initialize(){

    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}