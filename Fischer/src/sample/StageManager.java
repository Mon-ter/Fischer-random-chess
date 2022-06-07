package sample;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StageManager{
    Scene MainMenu;
    Scene GameStartMenu;
    Scene Statistics;
    Scene readFromFileScene;

    Stage primaryStage;
    Main main;

    StageManager(Stage primaryStage, Main main){
        this.primaryStage = primaryStage;
        this.main = main;
        MainMenu();
        GameStartMenu();
        Statistic();
        readFromFileScene();
    }
    
    public Scene createEndingScene(Result result, GameSupervisor gameSupervisor) {
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        String note;

        if (result == Result.WHITE) {
            note = "WHITE won";
        } else if (result == Result.BLACK) {
            note = "BLACK won";
        } else {
            note = "it ended with a draw";
        }
        note += "\n now it's time to allow users to save game moves using gameSupervisor";
        try {
            File pgnFile = new File("pgn.txt");
            pgnFile.createNewFile();
        } catch(IOException e){
        }
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            FileWriter writer = new FileWriter("pgn.txt", true);
            writer.write(dtf.format(now));
            writer.write("\n" + gameSupervisor.pgnNotation);
            if(result == Result.BLACK){
                writer.write("0-1\n\n");
            }else if(result == Result.WHITE){
                writer.write("1-0\n\n");
            }else{
                writer.write("1/2-1/2\n\n");
            }
            writer.close();
        }catch (IOException e){

        }

        main.appendResultToStats(result);

        Label label = new Label(note);
        Scene scene = new Scene(layout, 300, 300);

        Button button = new Button("Start Game");
        button.setOnAction(e -> primaryStage.setScene(GameStartMenu));

        layout.getChildren().addAll(label, button);

        return scene;
    }

    public Scene createGame(boolean Fischer){
        Scene scene = new Scene(main.createContent(Fischer, primaryStage));
        return scene;
    }

    public void MainMenu(){
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        Scene menu = new Scene(layout, 640, 640);

        Button button = new Button("Start Game");
        button.setPrefSize(Main.PIECE_SIZE * 2, Main.PIECE_SIZE / 2);
        button.setOnAction(e -> primaryStage.setScene(GameStartMenu));
        Button button2 = new Button("Game From File");
        button2.setPrefSize(Main.PIECE_SIZE * 2, Main.PIECE_SIZE / 2);
        button2.setOnAction(e -> primaryStage.setScene(readFromFileScene));
        Button button3 = new Button("Statistics");
        button3.setPrefSize(Main.PIECE_SIZE * 2, Main.PIECE_SIZE / 2);
        button3.setOnAction(e -> primaryStage.setScene(Statistics));

        layout.getChildren().addAll(button, button2, button3);

        MainMenu = menu;
    }

    public void GameStartMenu(){
        File directoryPath = new File(Main.class.getResource("graphics/").getPath());
        String contents[] = directoryPath.list();

        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        Scene gameMenu = new Scene(layout, 640, 640);

        ChoiceBox timeControlBox = new ChoiceBox<TimeControl>();
        timeControlBox.getItems().addAll(TimeControl.NONE, TimeControl.THREE, TimeControl.THREE_PLUS_TWO, TimeControl.FIVE, TimeControl.FIFTEEN);

        timeControlBox.setOnAction((event) -> {
            main.timeControl = (TimeControl) timeControlBox.getValue();
        });

        ChoiceBox choiceBox = new ChoiceBox();

        for (String folder : contents) {
            choiceBox.getItems().add(folder);
        }

        choiceBox.setOnAction((event) -> {
            Main.graphicFolder = (String)choiceBox.getValue();
        });

        ToggleButton toggleButton = new ToggleButton("Fischer");
        toggleButton.setPrefSize(Main.PIECE_SIZE * 2, Main.PIECE_SIZE / 2);
        Button button = new Button("Start New Game");
        button.setPrefSize(Main.PIECE_SIZE * 2, Main.PIECE_SIZE / 2);
        button.setOnAction(e -> primaryStage.setScene(createGame(toggleButton.isSelected())));

        layout.getChildren().addAll(timeControlBox, choiceBox, toggleButton, button);

        GameStartMenu = gameMenu;
    }

    public void Statistic(){
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        int whiteWins = 0;
        int blackWins = 0;
        int draws = 0;

        main.checkIfStatsFileExists();

        try {
            File myObj = new File("stats.txt");
            Scanner myReader = new Scanner(myObj);
            whiteWins = myReader.nextInt();
            blackWins = myReader.nextInt();
            draws = myReader.nextInt();
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("White won", whiteWins),
                new PieChart.Data("Black won", blackWins),
                new PieChart.Data("Draw", draws)
        );

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("As it stands");

        Button button = new Button("Go back");
        button.setOnAction(e -> primaryStage.setScene(MainMenu));

        layout.getChildren().addAll(chart, button);

        Statistics = new Scene(layout, 8 * Main.TILE_SIZE, 8 * Main.TILE_SIZE);
    }

    public void readFromFileScene(){
        File directoryPath = new File(Main.class.getResource("graphics/").getPath());
        String contents[] = directoryPath.list();

        FileChooser fileChooser = new FileChooser();
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        Scene reading = new Scene(layout, 640, 640);

        ChoiceBox choiceBox = new ChoiceBox();

        for (String folder : contents) {
            choiceBox.getItems().add(folder);
        }

        choiceBox.setOnAction((event) -> {
            main.graphicFolder = (String)choiceBox.getValue();
        });

        Button getFile = new Button("Open File");
        getFile.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            primaryStage.setScene(createGame(false));
                            if (main.onMove.getGameMode()) {
                                main.onMove.switchMode();
                            }
                            GameFile g = new GameFile(main.gameSupervisor,main.whiteKing,main.darkKing,main.whiteAlivePieces,main.darkAlivePieces);
                            g.readMoves(file.getAbsolutePath());
                        }
                    }
                });

        Button button2 = new Button("Go back");
        button2.setOnAction(e -> primaryStage.setScene(MainMenu));

        layout.getChildren().addAll(getFile, button2);

        readFromFileScene = reading;
    }

    public void OpenMainMenu(){
        primaryStage.setScene(MainMenu);
        primaryStage.show();
    }

}