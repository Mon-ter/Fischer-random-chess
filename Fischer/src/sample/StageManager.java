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
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StageManager{
    Scene MainMenu;
    Scene GameStartMenu;
    Scene Statistics;
    Scene readFromFileScene;

    private final double sceneSize = 640;
    private final double buttonHeight = Main.PIECE_SIZE / 2.0;
    private final double buttonStandardWidth = Main.PIECE_SIZE * 2;
    private final double gameStartMenuButtonWidth = 234;
    private final double mainMenuButtonHeight = 60;
    private final double mainMenuButtonWidth = 300;
    private final int endingFontNoteSize = 60;
    private final int goBackFontSize = 15;

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
            note = "WHITE won!";
        } else if (result == Result.BLACK) {
            note = "BLACK won!";
        } else {
            note = "it ended with a draw!";
        }
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
        label.setFont(new Font("Arial", endingFontNoteSize));
        Scene scene = new Scene(layout, sceneSize, sceneSize);
        Label info = new Label("Your game is already saved for further usage");
        info.setFont(new Font("Arial", goBackFontSize));
        Button button = new Button("Play once again");
        button.setPrefSize(buttonStandardWidth, buttonHeight);
        button.setOnAction(e -> primaryStage.setScene(GameStartMenu));

        layout.getChildren().addAll(label, info, button);
        layout.setSpacing(Main.PROMOTION_BUTTON_SIZE);
        return scene;
    }

    public Scene createGame(boolean Fischer, boolean gameFromFile){
        Scene scene = new Scene(main.createContent(Fischer, gameFromFile, primaryStage));
        return scene;
    }

    public void MainMenu(){
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(5);
        Scene menu = new Scene(layout, sceneSize, sceneSize);

        Button button = new Button("Start Game");
        button.setPrefSize(mainMenuButtonWidth, mainMenuButtonHeight);
        button.setOnAction(e -> primaryStage.setScene(GameStartMenu));
        Button button2 = new Button("Game From File");
        button2.setPrefSize(mainMenuButtonWidth, mainMenuButtonHeight);
        button2.setOnAction(e -> primaryStage.setScene(readFromFileScene));
        Button button3 = new Button("Statistics");
        button3.setPrefSize(mainMenuButtonWidth, mainMenuButtonHeight);
        button3.setOnAction(e -> primaryStage.setScene(Statistics));

        layout.getChildren().addAll(button, button2, button3);

        MainMenu = menu;
    }

    public void GameStartMenu(){
        File directoryPath = new File(Main.class.getResource("graphics/").getPath());
        String contents[] = directoryPath.list();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(5);
        grid.setHgap(5);
        
        Scene gameMenu = new Scene(grid, sceneSize, sceneSize);

        Text textTimeControl = new Text("Time Control");
        Text textPieces = new Text("Pieces Style");
        Text textGameMode = new Text("Game Mode");

        ChoiceBox timeControlBox = new ChoiceBox<TimeControl>();
        timeControlBox.setPrefSize(buttonStandardWidth, buttonHeight);
        timeControlBox.getItems().addAll(TimeControl.NONE, TimeControl.THREE, TimeControl.THREE_PLUS_TWO, TimeControl.FIVE, TimeControl.FIFTEEN);        
        timeControlBox.getSelectionModel().selectFirst();

        timeControlBox.setOnAction((event) -> {
            main.timeControl = (TimeControl) timeControlBox.getValue();
        });

        ChoiceBox choiceBox = new ChoiceBox();
        choiceBox.setPrefSize(buttonStandardWidth, buttonHeight);

        for (String folder : contents) {
            choiceBox.getItems().add(folder);
        }
        choiceBox.getSelectionModel().selectFirst();

        choiceBox.setOnAction((event) -> {
            Main.graphicFolder = (String)choiceBox.getValue();
        });


        ToggleButton toggleButton = new ToggleButton("Fischer");
        toggleButton.setPrefSize(buttonStandardWidth, buttonHeight);

        Button button = new Button("Start New Game");
        button.setPrefSize(gameStartMenuButtonWidth, buttonHeight);
        button.setOnAction(e -> {
            boolean isSelected = toggleButton.isSelected();
            toggleButton.setSelected(false);
            primaryStage.setScene(createGame(isSelected, false));
            });

        Button buttonBack = new Button("Go back");
        buttonBack.setPrefSize(gameStartMenuButtonWidth, buttonHeight);
        buttonBack.setOnAction(e -> primaryStage.setScene(MainMenu));

        grid.add(timeControlBox,1,0);
        grid.add(choiceBox,1,1);
        grid.add(toggleButton,1,2);
        grid.add(button,0,3,2,1);
        grid.add(textTimeControl,0,0);
        grid.add(textPieces,0,1);
        grid.add(textGameMode,0,2);
        grid.add(buttonBack, 0, 4, 2, 1);
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
        button.setPrefSize(buttonStandardWidth, buttonHeight);
        button.setOnAction(e -> primaryStage.setScene(MainMenu));

        layout.getChildren().addAll(chart, button);

        Statistics = new Scene(layout, sceneSize, sceneSize);
    }

    public void readFromFileScene(){
        File directoryPath = new File(Main.class.getResource("graphics/").getPath());
        String contents[] = directoryPath.list();

        FileChooser fileChooser = new FileChooser();
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(5);
        Scene reading = new Scene(layout, sceneSize, sceneSize);

        ChoiceBox choiceBox = new ChoiceBox();
        choiceBox.setPrefSize(buttonStandardWidth, buttonHeight);

        for (String folder : contents) {
            choiceBox.getItems().add(folder);
        }

        choiceBox.setOnAction((event) -> {
            main.graphicFolder = (String)choiceBox.getValue();
        });
        choiceBox.getSelectionModel().selectFirst();

        Button getFile = new Button("Open File");        
        getFile.setPrefSize(buttonStandardWidth, buttonHeight);
        getFile.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            primaryStage.setScene(createGame(false, true));
                            if (main.onMove.getGameMode()) {
                                main.onMove.switchMode();
                            }
                            GameFile g = new GameFile(main.gameSupervisor,main.whiteKing,main.darkKing,main.whiteAlivePieces,main.darkAlivePieces);
                            g.readMoves(file.getAbsolutePath());
                        }
                    }
                });

        Button goBack = new Button("Go back");
        goBack.setPrefSize(buttonStandardWidth, buttonHeight);
        goBack.setOnAction(e -> primaryStage.setScene(MainMenu));

        layout.getChildren().addAll(choiceBox, getFile, goBack);

        readFromFileScene = reading;
    }

    public void OpenMainMenu(){
        primaryStage.setScene(MainMenu);
        primaryStage.show();
    }

    public void OpenEndingScene(Result result, GameSupervisor gameSupervisor){
        primaryStage.setScene(createEndingScene(result, gameSupervisor));
    }

}