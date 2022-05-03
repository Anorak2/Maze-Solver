package com.example.mazesolver;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class MazeSolverController implements Initializable {
    public static class Block{
        boolean isExplored = false;
        boolean isWall, isStart, isFinish, canMoveHere;

        public Block(int x){
            isWall = false;
            isStart = false;
            isFinish = false;
            canMoveHere = true;
            if(x == 1){
                isWall = true;
                canMoveHere = false;
            }
            else if(x == 2){
                isStart = true;
            }
            else if(x == 3){
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
    @FXML
    Rectangle clickDetection;
    @FXML
    AnchorPane playScreen, buildScreen;


    //0 is clear space, 1 is a wall, 2 is start, 3 is finish, 4 is explored
    Block[][] mainMaze;
    //(Row, col)
    int[] startLocation = new int[2];
    int gridSize = 8, max = gridSize-1;
    long tileSize = (long) 500/gridSize;
    boolean finished = false, isPlaying = false, placeMode = false, isBuild = false;
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
        clickDetection.setOnMouseClicked(this::click);

        startMazeAlgorithm();
        slider();
    }

    public void startMazeAlgorithm(){
        path = new LinkedList<>();
        masterAnimation.getChildren().clear();
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
        if(canMove(row, col, common, col) && !(mainMaze[common][col].isExplored) && !finished){
            mazeAlgorithm(common, col);
        }
        common = col+1;
        if(canMove(row, col, row, common) && !(mainMaze[row][common].isExplored) && !finished){
            mazeAlgorithm(row, common);
        }
        common = col -1;
        if(canMove(row, col, row, common) && !(mainMaze[row][common].isExplored) && !finished){
            mazeAlgorithm(row, common);
        }
        common = row - 1;
        if(canMove(row, col, common, col) && !(mainMaze[common][col].isExplored) && !finished){
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
            return mainMaze[newRow][newCol].canMoveHere;
        }
        // checking move left/right
        else if((col + 1 == newCol || col - 1 == newCol) && row == newRow){
            return mainMaze[newRow][newCol].canMoveHere;
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
        masterAnimation.setOnFinished(e -> toggle());
    }
    public void setUnexplored(){
        for(int row = 0; row < gridSize; row++){
            for(int col = 0; col<gridSize; col++){
                mainMaze[row][col].isExplored = false;
            }
        }
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

    public void placeModeOn(){
        placeMode = true;
    }
    public void placeModeOff(){
        placeMode = false;
    }
    public void toggleBuildMode() {
        isBuild = !isBuild;
        playScreen.setVisible(!isBuild);
        buildScreen.setVisible(isBuild);
        if(!isBuild){
            mainMaze[6][5] = new Block(3);
            mainMaze[0][0] = new Block(2);
            finished = false;
            displayMaze();
            setUnexplored();
            startMazeAlgorithm();
        }
    }

    public void click(MouseEvent e){
        System.out.println("yeet");
        if(!isBuild){
            return;
        }
        int row = (int) ((e.getY())/tileSize);
        int col = (int) ((e.getX())/tileSize);
        if(placeMode && inBounds(row, col)){
            mainMaze[row][col] = new Block(1);
        }
        else{
            mainMaze[row][col] = new Block(0);
        }
        displayMaze();
    }
    public void clear(){
        loadPreset(0);
        displayMaze();
    }

    private boolean inBounds(int row, int col){
        return row <= max && row >= 0 && col <= max && col >= 0;
    }
    private void finish(){
        finished = true;
    }
    private void printMaze(){
        int loop = gridSize;
        for(int x = 0; x < loop; x++){
            for(int y = 0; y < loop; y++){
                if(mainMaze[x][y].isWall) {
                    System.out.print("0 ");
                }
                else if(mainMaze[x][y].canMoveHere) {
                    System.out.print(". ");
                }
                else if(mainMaze[x][y].isStart) {
                    System.out.print("S ");
                }
                else if(mainMaze[x][y].isFinish) {
                    System.out.print("F ");
                }
            }
            System.out.println();
        }
    }
    private void displayMaze(){
        mazeGridPane.getChildren().clear();

        for(int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if(mainMaze[row][col].isWall) {
                    Region newRectangle = new Region();
                    String x = roundCorners(row, col);
                    newRectangle.setStyle(x);
                    mazeGridPane.add(newRectangle, col, row);
                }
                if(!mainMaze[row][col].isWall){
                    Rectangle newRectangle = new Rectangle(tileSize, tileSize);
                    newRectangle.setOpacity(0);
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
    private String roundCorners(int row, int col){
        int[] roundness = {0, 0, 0, 0};
        int roundnessValue = 10;
        //If it is not an edge
        if(row+1 <= max && row-1 >= 0 && col+1 <= max && col-1 >= 0){
            //Checks up and left
            if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                roundness[3] = roundnessValue;
            }
            //Checks up and right
            if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                roundness[2] = roundnessValue;
            }
            //Checks down and right
            if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                roundness[1] = roundnessValue;
            }
            //Checks down and left
            if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                roundness[0] = roundnessValue;
            }
        }
        //If it is an edge
        else{
            //-------Corners-------
            if((row + 1 > max && col - 1 < 0) || (row + 1 > max && col + 1 > max) ||
                    (row - 1 < 0 && col + 1 > max) || (row - 1 < 0 && col - 1 < 0)) {
                //top left corner (clear)
                if (row - 1 < 0 && col - 1 < 0) {
                    roundness[0] = roundnessValue;
                    //Checks up and right
                    if(mainMaze[row][col+1].canMoveHere) {
                        roundness[1] = roundnessValue;
                    }
                    //Checks down and right
                    if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                        roundness[2] = roundnessValue;
                    }
                    //Checks down and left
                    if(mainMaze[row+1][col].canMoveHere ) {
                        roundness[3] = roundnessValue;
                    }
                }
                //top right corner (clear)
                else if (row - 1 < 0) {
                    roundness[1] = roundnessValue;
                    //Checks up and left
                    if(mainMaze[row][col-1].canMoveHere) {
                        roundness[0] = roundnessValue;
                    }
                    //Checks down and right
                    if(mainMaze[row+1][col].canMoveHere) {
                        roundness[2] = roundnessValue;
                    }
                    //Checks down and left
                    if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                        roundness[3] = roundnessValue;
                    }
                }
                //bottom right corner (clear)
                else if (col + 1 > max) {
                    roundness[2] = roundnessValue;
                    //Checks up and left
                    if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                        roundness[0] = roundnessValue;
                    }
                    //Checks up and right
                    if(mainMaze[row-1][col].canMoveHere) {
                        roundness[1] = roundnessValue;
                    }
                    //Checks down and left
                    if(mainMaze[row][col-1].canMoveHere) {
                        roundness[3] = roundnessValue;
                    }
                }
                //bottom left corner (clear)
                else {
                    roundness[3] = roundnessValue;
                    //Checks up and left
                    if(mainMaze[row-1][col].canMoveHere) {
                        roundness[0] = roundnessValue;
                    }
                    //Checks up and right
                    if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                        roundness[1] = roundnessValue;
                    }
                    //Checks down and right
                    if(mainMaze[row][col+1].canMoveHere) {
                        roundness[2] = roundnessValue;
                    }

                }
            }

            //-------Edges-------
            else{
                //down edge
                if(row + 1 > max){
                    //Checks up and left
                    if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                        roundness[0] = roundnessValue;
                    }
                    //Checks up and right
                    if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                        roundness[1] = roundnessValue;
                    }
                    //Checks down and right
                    if(mainMaze[row][col+1].canMoveHere) {
                        roundness[2] = roundnessValue;
                    }
                    //Checks down and left
                    if(mainMaze[row][col-1].canMoveHere) {
                        roundness[3] = roundnessValue;
                    }
                }
                //up edge
                else if(row - 1 < 0){
                    //Checks up and left
                    if(mainMaze[row][col-1].canMoveHere) {
                        roundness[0] = roundnessValue;
                    }
                    //Checks up and right
                    if(mainMaze[row][col+1].canMoveHere) {
                        roundness[1] = roundnessValue;
                    }
                    //Checks down and right
                    if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                        roundness[2] = roundnessValue;
                    }
                    //Checks down and left
                    if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                        roundness[3] = roundnessValue;
                    }
                }
                //left
                else if(col - 1 < 0){
                    //Checks up and left
                    if(mainMaze[row-1][col].canMoveHere) {
                        roundness[0] = roundnessValue;
                    }
                    //Checks up and right
                    if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                        roundness[1] = roundnessValue;
                    }
                    //Checks down and right
                    if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col+1].canMoveHere) {
                        roundness[2] = roundnessValue;
                    }
                    //Checks down and left
                    if(mainMaze[row+1][col].canMoveHere) {
                        roundness[3] = roundnessValue;
                    }
                }
                //right
                else{
                    //checks up and left
                    if(mainMaze[row-1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                        roundness[0] = roundnessValue;
                    }
                    //Checks up and right
                    if(mainMaze[row-1][col].canMoveHere) {
                        roundness[1] = roundnessValue;
                    }
                    //Checks down and right
                    if(mainMaze[row+1][col].canMoveHere) {
                        roundness[2] = roundnessValue;
                    }
                    //Checks down and left
                    if(mainMaze[row+1][col].canMoveHere && mainMaze[row][col-1].canMoveHere) {
                        roundness[3] = roundnessValue;
                    }
                }
            }
        }

        StringBuilder build = new StringBuilder();
        build.append("-fx-background-color: #25D961; -fx-background-radius: ");
        for(int i = 0; i < 4; i++){
            build.append(" ");
            build.append(roundness[i]);
        }
        build.append(";");

        return build.toString();
    }
    private void loadPreset(int x){
        if(x == 0){
            int[] temp;
            temp = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
            for(int z = 0; z < gridSize; z++){
                loadPresetHelper(temp, z);
            }
        }
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
        for(int i = 0; i < gridSize; i++){
            mainMaze[currentRow][i] = new Block(list[i]);
        }
    }
}