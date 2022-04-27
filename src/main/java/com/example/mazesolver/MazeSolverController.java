package com.example.mazesolver;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class MazeSolverController implements Initializable {
    public class Block{
        boolean isExplored = false;
        boolean isWall, isStart, isFinish, isEmpty;

        public Block(int x){
            isWall = false;
            isStart = false;
            isFinish = false;
            isEmpty = true;
            if(x == 1){
                isWall = true;
                isEmpty = false;
            }
            if(x == 2){
                isStart = true;
            }
            if(x == 3){
                isFinish = true;
            }
        }
    }
    @FXML
    GridPane mazeGridPane;
    @FXML
    Circle playerCircle;
    @FXML
    Slider speedSlider;
    @FXML
    ImageView pauseImage, playImage;


    //0 is clear space, 1 is a wall, 2 is start, 3 is finish, 4 is explored
    Block[][] mainMaze;
    //(Row, col)
    int[] startLocation = new int[2];
    int gridSize = 8;
    long tileSize = (long) 500/gridSize;
    boolean finished = false, isPlaying = false;
    Queue<int[]> path = new LinkedList<>();
    SequentialTransition masterAnimation = new SequentialTransition();


    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainMaze = new Block[gridSize][gridSize];
        mazeGridPane.getChildren().clear();
        loadPreset(1);
        printMaze();
        displayMaze();
        playerCircle.setRadius(tileSize/2);
        playerCircle.setCenterX(50 + playerCircle.getRadius() + gridSize*startLocation[1]);
        playerCircle.setCenterY(50 + playerCircle.getRadius() + gridSize*startLocation[0]);


        startMazeAlgorithm();
        slider();
    }

    public void startMazeAlgorithm(){
        path.clear();
        mazeAlgorithm(startLocation[0], startLocation[1]);
        setupAnimations();
    }
    public void mazeAlgorithm(int row, int col){
        System.out.println(row + " " + col);

        //logging that we've been here
        mainMaze[row][col].isExplored = true;
        path.add(new int[]{row, col});

        //Checking if we made it to the finish
        if(mainMaze[row][col].isFinish) {
            finish();
            return;
        }

        //Checking to move up/right/left/down
        int common = row+1;
        if(canMove(row, col, common, col) && !mainMaze[common][col].isExplored && !finished){
            mazeAlgorithm(common, col);
        }
        common = col+1;
        if(canMove(row, col, row, common) && !mainMaze[row][common].isExplored && !finished){
            mazeAlgorithm(row, common);
        }
        common = col -1;
        if(canMove(row, col, row, common) && !mainMaze[row][common].isExplored && !finished){
            mazeAlgorithm(row, common);
        }
        common = row - 1;
        if(canMove(row, col, common, col) && !mainMaze[common][col].isExplored && !finished){
            mazeAlgorithm(common, col);
        }

    }

    private boolean canMove(int row, int col, int newRow, int newCol){
        //Checks if the bot can move there, can only move up down left right

        //bounds detection
        if(row < 0 || row > gridSize-1 || col < 0 || col > gridSize-1) {
            return false;
        }
        if(newRow < 0 || newRow > gridSize-1 || newCol < 0 || newCol > gridSize-1) {
            return false;
        }
        //Checking move up/down
        else if((row + 1 == newRow || row - 1 == newRow) && col == newCol){
            return mainMaze[newRow][newCol].isEmpty;
        }
        // checking move left/right
        else if((col + 1 == newCol || col - 1 == newCol) && row == newRow){
            return mainMaze[newRow][newCol].isEmpty;
        }
        return false;
    }
    public void setupAnimations(){
        int[] temp;
        int loop = path.size();
        System.out.println();

        //Adding the translations
        for(int x = 0; x < loop; x++) {
            temp = path.remove();
            TranslateTransition translate = new TranslateTransition();
            translate.setNode(playerCircle);
            translate.setToY(tileSize * temp[0]);
            translate.setToX(tileSize * temp[1]);

            masterAnimation.getChildren().add(translate);
            path.add(temp);
        }
        masterAnimation.setOnFinished(e -> {
            toggle();
        });
    }

    public void toggle(){
        if(isPlaying){
            masterAnimation.pause();
        }
        else {
            masterAnimation.play();
        }
        pauseImage.setVisible(!isPlaying);
        playImage.setVisible(isPlaying);
        isPlaying = !isPlaying;
    }
    public void slider(){
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> masterAnimation.setRate(( double) newValue));
    }


    private void finish(){
        finished = true;
        System.out.println("yay");
    }
    private void printMaze(){
        int loop = gridSize;
        for(int x = 0; x < loop; x++){
            for(int y = 0; y < loop; y++){
                if(mainMaze[x][y].isWall) {
                    System.out.print("0 ");
                }
                else if(mainMaze[x][y].isStart) {
                    System.out.print("S ");
                }
                else if(mainMaze[x][y].isFinish) {
                    System.out.print("F ");
                }
                else if(mainMaze[x][y].isEmpty) {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }
    private void displayMaze(){
        mazeGridPane.getChildren().clear();

        int loop = gridSize;
        for(int row = 0; row < loop; row++) {
            for (int col = 0; col < loop; col++) {
                if(mainMaze[row][col].isWall) {
                    Rectangle newRectangle = new Rectangle(tileSize, tileSize, Color.web("20bf55"));
                    mazeGridPane.add(newRectangle, col, row);
                }
                if(mainMaze[row][col].isStart) {
                    startLocation[0] = row;
                    startLocation[1] = col;
                }
                if(mainMaze[row][col].isFinish) {
                    Circle newCircle = new Circle(gridSize*3, Color.RED);
                    mazeGridPane.add(newCircle, col, row);
                }
            }
        }
    }
    private void loadPreset(int x){
        if(x == 1){
            int[] temp =    {2, 0, 1, 0, 0, 0, 0, 1};
            loadPresetHelper(temp, 0);
            temp = new int[]{1, 0, 1, 0, 1, 0, 0, 0};
            loadPresetHelper(temp, 1);
            temp = new int[]{0, 0, 0, 0, 1, 0, 0, 0};
            loadPresetHelper(temp, 2);
            temp = new int[]{0, 1, 1, 1, 1, 0, 1, 0};
            loadPresetHelper(temp, 3);
            temp = new int[]{0, 1, 1, 0, 0, 0, 1, 0};
            loadPresetHelper(temp, 4);
            temp = new int[]{0, 0, 1, 0, 1, 1, 1, 0};
            loadPresetHelper(temp, 5);
            temp = new int[]{0, 1, 1, 0, 1, 3, 1, 0};
            loadPresetHelper(temp, 6);
            temp = new int[]{0, 0, 0, 1, 0, 0, 0, 0};
            loadPresetHelper(temp, 7);
        }
    }
    private void loadPresetHelper(int[] list, int currentRow){
        for(int i = 0; i < 8; i++){
            mainMaze[currentRow][i] = new Block(list[i]);
        }
    }

}