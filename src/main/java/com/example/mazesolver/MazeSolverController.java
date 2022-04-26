package com.example.mazesolver;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MazeSolverController {
    @FXML
    GridPane mazeGridPane;

    int[][] mainMaze;
    int gridSize = 8;
    int tileSize = 500/gridSize;

    public void initialize() {
        mainMaze = new int[gridSize][gridSize];
        mazeGridPane.getChildren().clear();
        loadPreset(1);
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                System.out.print(mainMaze[x][y] + " ");
            }
            System.out.println();
        }
        displayMaze();
    }


    public void displayMaze(){
        mazeGridPane.getChildren().clear();

        for(int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if(mainMaze[row][col] == 1) {
                    Rectangle newRectangle = new Rectangle(tileSize, tileSize, Color.web("20bf55"));
                    mazeGridPane.add(newRectangle, col, row);
                }
            }
        }
    }
    public void loadPreset(int x){
        if(x == 1){
            int[] temp =    {2, 0, 1, 0, 0, 0, 0, 1};
            mainMaze[0] = temp;
            temp = new int[]{1, 0, 1, 0, 1, 0, 0, 0};
            mainMaze[1] = temp;
            temp = new int[]{0, 0, 0, 0, 1, 0, 0, 0};
            mainMaze[2] = temp;
            temp = new int[]{0, 1, 1, 1, 1, 0, 1, 0};
            mainMaze[3] = temp;
            temp = new int[]{0, 1, 1, 0, 0, 0, 1, 0};
            mainMaze[4] = temp;
            temp = new int[]{0, 0, 1, 0, 1, 1, 1, 0};
            mainMaze[5] = temp;
            temp = new int[]{0, 1, 1, 0, 1, 2, 1, 0};
            mainMaze[6] = temp;
            temp = new int[]{0, 0, 0, 1, 0, 0, 0, 0};
            mainMaze[7] = temp;
        }
    }

}