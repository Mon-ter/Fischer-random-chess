package sample;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.util.Duration;
import javafx.util.Pair;

public class Main extends Application {

    public static final int TILE_SIZE = 80;
    public static final int SQUARE_NUMBER = 8;
    public static final int PIECE_SIZE = 80;
    public static final int PROMOTION_BUTTON_SIZE = TILE_SIZE / 2;

    public Tile [][] board = new Tile [SQUARE_NUMBER][SQUARE_NUMBER];

    private Group tiles = new Group();
    private Group pieces = new Group();

    Army whiteAlivePieces = new Army();
    Army darkAlivePieces = new Army();

    Clock whiteClock = null;
    Clock darkClock = null;

    boolean doubleCheck = false;
    boolean check = false;
    int enPassantXPossibility;

    Square graveyard = null;

    Piece whiteKing = null;
    Piece darkKing = null;

    TurnIndicator onMove = null;
    GameSupervisor gameSupervisor = null;

    private Pane right = null;
    private HBox promotionBox = null;

    public static TimeControl timeControl = TimeControl.NONE;

    public static Color darkTileColour = Color.TOMATO;
    public static Color lightTileColour = Color.WHITESMOKE;
    public static String graphicFolder = "new";

    private Parent createContent(boolean FischerOnes, Stage stage) {

        BorderPane root = new BorderPane();
        Pane center = new Pane();
        Pane right = new Pane();
        root.setRight(right);
        root.setCenter(center);

        this.right = right;

        center.setPrefSize(TILE_SIZE * SQUARE_NUMBER, TILE_SIZE * SQUARE_NUMBER);
        center.getChildren().addAll(tiles, pieces);

        right.setPrefSize(TILE_SIZE * 3, TILE_SIZE * SQUARE_NUMBER);



        Button draw = new Button("Take draw");
        Button resign = new Button("Resign");

        draw.setPrefSize(PIECE_SIZE, PIECE_SIZE / 2);
        resign.setPrefSize(PIECE_SIZE, PIECE_SIZE / 2);
        draw.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(createEndingScene(Result.DRAW, gameSupervisor, stage));
            }
        });

        resign.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(createEndingScene(onMove.getPieceColour() == PieceColour.WHITE ? Result.BLACK : Result.WHITE, gameSupervisor, stage));
            }
        });

        HBox manualEndings = new HBox(draw, resign);
        manualEndings.relocate((TILE_SIZE * 3 - 2 * PIECE_SIZE) / 2, TILE_SIZE * 4);
        right.getChildren().add(manualEndings);

        Button backwardReview = new Button("Back");
        Button forwardReview = new Button("Forward");

        backwardReview.setPrefSize(PIECE_SIZE, PIECE_SIZE);
        forwardReview.setPrefSize(PIECE_SIZE, PIECE_SIZE);

        backwardReview.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (gameSupervisor.anyMovesMade()) {
                    if (onMove.getGameMode()) {
                        onMove.switchMode();
                        gameSupervisor.setCounter();
                    }
                    gameSupervisor.retraceMove();
                }
            }
        });

        forwardReview.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (onMove.getGameMode()) {

                } else {
                    if(gameSupervisor.repeatMove()) {
                        onMove.switchMode();
                    }
                }
            }
        });

        HBox gameReview = new HBox(backwardReview, forwardReview);

        gameReview.relocate((TILE_SIZE * 3 - 2 * PIECE_SIZE) / 2, TILE_SIZE * 5);
        right.getChildren().add(gameReview);

        ExCoordinates positionCreator = new ExCoordinates(FischerOnes);

        onMove = new TurnIndicator(true);
        gameSupervisor = new GameSupervisor(board);
        graveyard = new Square(-1, -1);
        enPassantXPossibility = -2;

        for (int y = 0; y < SQUARE_NUMBER; y++) {
            for (int x = 0; x < SQUARE_NUMBER; x++) {
                Tile tile = new Tile((y + x) % 2 != 0, x, y);
                tiles.getChildren().add(tile);
                board [x] [y] = tile;

                if (yOfStartingPiecePosition(y)) {
                    addPieceToBoard(positionCreator, x, y, stage, tile);
                }

            }
        }

        darkKing = board [positionCreator.yKing()] [0].getPiece();
        whiteKing = board [positionCreator.yKing()] [7].getPiece();

        whiteAlivePieces.addOurKing(whiteKing);
        darkAlivePieces.addOurKing(darkKing);

        whiteAlivePieces.addEnemyArmy(darkAlivePieces);
        darkAlivePieces.addEnemyArmy(whiteAlivePieces);

        whiteAlivePieces.makeThemReady(enPassantXPossibility);



        if (timeControl != TimeControl.NONE) {
            whiteClock = new Clock(timeControl, gameSupervisor, stage);
            darkClock = new Clock(timeControl, gameSupervisor, stage);

            darkClock.getTimer().relocate((TILE_SIZE * 3 - whiteClock.getWidth()) / 2, TILE_SIZE / 2);
            whiteClock.getTimer().relocate((TILE_SIZE * 3 - darkClock.getWidth()) / 2, TILE_SIZE * SQUARE_NUMBER - 3 * TILE_SIZE / 2);

            right.getChildren().addAll(whiteClock.getTimer(), darkClock.getTimer());
            whiteClock.play();
        }

        return root;
    }

    public boolean yOfStartingPiecePosition(int y) {
        return y == 0 || y == 1 || y == 6 || y == 7;
    }
    public void addPieceToBoard(ExCoordinates positionCreator, int x, int y, Stage stage, Tile tile) {
        PieceColour colour = (y == 6 || y == 7) ? PieceColour.WHITE : PieceColour.BLACK;
        PieceKind kind = (y == 1 || y == 6) ? PieceKind.PAWN : positionCreator.retrieveWhichPiece(x);
        Army army = (colour == PieceColour.WHITE) ? whiteAlivePieces : darkAlivePieces;
        Piece piece = makePiece(colour, kind, x, y, board, stage);
        tile.setPiece(piece);
        pieces.getChildren().add(piece);
        army.hire(piece);
    }

    private Move tryMove(Piece piece, int newX, int newY) {
        if (isOnMove(piece)) {
            Square moveCandidate = new Square(newX, newY);
            return piece.isMoveLegit(moveCandidate);
        } else {
            return null;
        }
    }

    private int conversionToSquareMiddle(double pixel) {
        return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    private Piece makePiece(PieceColour colour, PieceKind kind, int x, int y, Tile [] [] board, Stage stage) {

        Piece piece = new Piece(colour, kind, x, y, board, onMove);

        piece.setOnMouseReleased(e -> {
            int newX = conversionToSquareMiddle(piece.getLayoutX());
            int newY = conversionToSquareMiddle(piece.getLayoutY());

            int oldX = conversionToSquareMiddle(piece.getOldX());
            int oldY = conversionToSquareMiddle(piece.getOldY());

            int pieceDuplication;

            Move result = tryMove(piece, newX, newY);
            if (result != null) {
                boolean take = board[newX][newY].getPiece() != null;
                pieceDuplication = 0;
                for(int i = 0; i < 8; ++i){
                    if (i != oldY && board[oldX][i].getPiece() != null &&
                            (board[oldX][i].getPiece().getKind() == piece.getKind() && board[oldX][i].getPiece().getColour() == piece.getColour())){
                        pieceDuplication = 1;
                    }
                    if ( i != oldX && board[i][oldY].getPiece() != null &&
                            board[i][oldY].getPiece().getKind() == piece.getKind() && board[i][oldY].getPiece().getColour() == piece.getColour()){
                        pieceDuplication = 2;
                    }
                }
                Note firstMoved = new Note(piece, oldX, oldY, newX, newY);
                Note secondMoved = null;
                if (result.type == MoveType.KILL) {
                    Army whichArmy = (onMove.getPieceColour() == PieceColour.WHITE) ? darkAlivePieces : whiteAlivePieces;
                    secondMoved = new Note(board [newX] [newY].getPiece(), newX, newY, graveyard.getX(), graveyard.getY());
                    whichArmy.kill(board [newX] [newY].getPiece());

                } else if (result.type == MoveType.EN_PASSANT) {
                    Army whichArmy = (onMove.getPieceColour() == PieceColour.WHITE) ? darkAlivePieces : whiteAlivePieces;
                    int yOfVictim = (onMove.getPieceColour() == PieceColour.WHITE) ? newY + 1 : newY - 1;
                    secondMoved = new Note(board [newX] [yOfVictim].getPiece(), newX, newY, graveyard.getX(), graveyard.getY());
                    whichArmy.kill(board [newX] [yOfVictim].getPiece());
                } else if (result.type == MoveType.SIMPLE_PROMOTION) {
                    promotionBox = promotionHBox(piece);
                    right.getChildren().add(promotionBox);
                    promotionBox.relocate(TILE_SIZE / 2, 2 * TILE_SIZE);
                    onMove.switchMode();
                } else if (result.type == MoveType.KILL_PROMOTION) {
                    Army whichArmy = (onMove.getPieceColour() == PieceColour.WHITE) ? darkAlivePieces : whiteAlivePieces;
                    secondMoved = new Note(board [newX] [newY].getPiece(), newX, newY, graveyard.getX(), graveyard.getY());
                    whichArmy.kill(board [newX] [newY].getPiece());
                    promotionBox = promotionHBox(piece);
                    right.getChildren().add(promotionBox);
                    promotionBox.relocate(TILE_SIZE / 2, 2 * TILE_SIZE);
                    onMove.switchMode();
                }
                if (result.type == MoveType.PAWN_DOUBLE_MOVE) {
                    enPassantXPossibility = newX;


                } else {
                    enPassantXPossibility = -2;
                }
                piece.move(newX, newY);
                board[oldX][oldY].setPiece(null);
                board[newX][newY].setPiece(piece);
                Pair<Note, Note> annotation = new Pair(firstMoved, secondMoved);
                gameSupervisor.add(annotation, take, pieceDuplication);
                piece.repaint();
                if (onMove.getPieceColour() == PieceColour.WHITE) {
                    boolean isThereACheck = whiteAlivePieces.lookForChecks(-2, darkKing.getCoordinates());


                    darkAlivePieces.makeThemReady(enPassantXPossibility);
                    if (darkAlivePieces.doTheyHaveAnyMoves()) {
                        if (timeControl != TimeControl.NONE) {
                            whiteClock.stop();
                            darkClock.play();
                        }
                    } else {
                        Result finalResult = isThereACheck ? Result.WHITE : Result.DRAW;
                        stage.setScene(createEndingScene(finalResult, gameSupervisor, stage));
                    }
                } else {
                    boolean isThereACheck = darkAlivePieces.lookForChecks(-2, whiteKing.getCoordinates());
                    whiteAlivePieces.makeThemReady(enPassantXPossibility);
                    if (whiteAlivePieces.doTheyHaveAnyMoves()) {
                        if (timeControl != TimeControl.NONE) {
                            darkClock.stop();
                            whiteClock.play();
                        }
                    } else {
                        Result finalResult = isThereACheck ? Result.BLACK : Result.DRAW;
                        stage.setScene(createEndingScene(finalResult, gameSupervisor, stage));
                    }
                }
                onMove.switchTurn();

            } else {
                piece.repaint();
                piece.doNotMove();
            }


        });
        return piece;
    }

    public boolean isOnMove(Piece piece) {
        return onMove.getGameMode() && piece.getColour() == onMove.getPieceColour();
    }

    public void clearData(){
        board = new Tile [SQUARE_NUMBER][SQUARE_NUMBER];

        tiles = new Group();
        pieces = new Group();

        whiteAlivePieces = new Army();
        darkAlivePieces = new Army();

        doubleCheck = false;
        check = false;

        enPassantXPossibility = 0;

        graveyard = null;

        whiteKing = null;
        darkKing = null;

        onMove = null;
        gameSupervisor = null;

        whiteClock = null;
        darkClock = null;

        right = null;
        promotionBox = null;
    }


    private Scene createEndingScene(Result result, GameSupervisor gameSupervisor, Stage stage) {
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

        Label label = new Label(note);
        Scene scene = new Scene(layout, 300, 300);

        Button button = new Button("Start Game");
        button.setOnAction(e -> stage.setScene(GameStartMenu(stage)));

        layout.getChildren().addAll(label, button);

        return scene;
    }

    public Scene createGame(boolean Fischer, Stage stage){
        clearData();
        Scene scene = new Scene(createContent(Fischer, stage));
        return scene;
    }

    public Scene MainMenu(Stage stage){
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        Scene menu = new Scene(layout, 640, 640);

        Button button = new Button("Start Game");
        button.setPrefSize(PIECE_SIZE * 2, PIECE_SIZE / 2);
        button.setOnAction(e -> stage.setScene(GameStartMenu(stage)));
        Button button2 = new Button("Game From File");
        button2.setPrefSize(PIECE_SIZE * 2, PIECE_SIZE / 2);
        button2.setOnAction(e -> stage.setScene(readFromFileScene(stage)));
        Button button3 = new Button("Statistics");
        button3.setPrefSize(PIECE_SIZE * 2, PIECE_SIZE / 2);
        button3.setOnAction(e -> stage.setScene(Statistic(stage)));

        layout.getChildren().addAll(button, button2, button3);

        return menu;
    }

    public Scene GameStartMenu(Stage stage){
        File directoryPath = new File(Main.class.getResource("graphics/").getPath());
        String contents[] = directoryPath.list();

        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        Scene gameMenu = new Scene(layout, 640, 640);

        ChoiceBox timeControlBox = new ChoiceBox<TimeControl>();
        timeControlBox.getItems().addAll(TimeControl.NONE, TimeControl.THREE, TimeControl.THREE_PLUS_TWO, TimeControl.FIVE, TimeControl.FIFTEEN);

        timeControlBox.setOnAction((event) -> {
            timeControl = (TimeControl) timeControlBox.getValue();
        });

        ChoiceBox choiceBox = new ChoiceBox();

        for (String folder : contents) {
            choiceBox.getItems().add(folder);
        }

        choiceBox.setOnAction((event) -> {
            graphicFolder = (String)choiceBox.getValue();
        });

        ToggleButton toggleButton = new ToggleButton("Fischer");
        toggleButton.setPrefSize(PIECE_SIZE * 2, PIECE_SIZE / 2);
        Button button = new Button("Start New Game");
        button.setPrefSize(PIECE_SIZE * 2, PIECE_SIZE / 2);
        button.setOnAction(e -> stage.setScene(createGame(toggleButton.isSelected(), stage)));

        layout.getChildren().addAll(timeControlBox, choiceBox, toggleButton, button);

        return gameMenu;
    }

    public Scene Statistic(Stage stage){
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        Scene stats = new Scene(layout, 640, 640);

        Button button = new Button("Go back");
        button.setOnAction(e -> stage.setScene(MainMenu(stage)));

        layout.getChildren().addAll(button);

        return stats;
    }

    public Scene readFromFileScene(Stage stage){
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
            graphicFolder = (String)choiceBox.getValue();
        });

        Button getFile = new Button("Open File");
        getFile.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(stage);
                        if (file != null) {
                            stage.setScene(createGame(false,stage));
                            if (onMove.getGameMode()) {
                                onMove.switchMode();
                            }

                        }
                    }
                });

        Button button2 = new Button("Go back");
        button2.setOnAction(e -> stage.setScene(MainMenu(stage)));

        layout.getChildren().addAll(getFile, button2);

        return reading;
    }

    public class Clock {

        private HBox timer;

        private short tinyTimer;

        private Label tensOfMinutes;
        private Label onesOfMinutes;

        private Label tensOfSeconds;
        private Label onesOfSeconds;

        private Label semicolon;

        private Timeline timeline;
        private long timeLeft;

        private int increment;

        private GameSupervisor gameSupervisor;
        private Stage stage;

        ArrayList<Label> backupList;

        Clock(TimeControl timeControl, GameSupervisor gameSupervisor, Stage stage) {

            this.gameSupervisor = gameSupervisor;
            this.stage = stage;

            setTime(timeControl);

            backupList = new ArrayList<>();

            tensOfSeconds = new Label();
            onesOfSeconds = new Label();
            tensOfMinutes = new Label();
            onesOfMinutes = new Label();

            semicolon = new Label(":");
            semicolon.setPrefSize(Main.TILE_SIZE / 4, Main.TILE_SIZE);
            semicolon.setFont(new Font("Arial", Main.TILE_SIZE));

            backupList.add(tensOfMinutes);
            backupList.add(onesOfMinutes);
            backupList.add(tensOfSeconds);
            backupList.add(onesOfSeconds);

            initialize();
            timer = new HBox(tensOfMinutes, onesOfMinutes, semicolon, tensOfSeconds, onesOfSeconds);

            tinyTimer = 0;
            increment = (timeControl == TimeControl.THREE_PLUS_TWO) ? 2 : 0;

            timeline = new Timeline(new KeyFrame(Duration.millis(10),
                    e -> {
                        tinyTimer++;
                        if (tinyTimer >= 100) {
                            subtractSecond();
                            checkEndGame();
                            writeTimeDown();
                            tinyTimer = 0;
                        }

                    }));
            timeline.setCycleCount(Animation.INDEFINITE);
        }

        public void stop() {
            timeline.stop();
            timeLeft += increment;
            writeTimeDown();
        }


        public void play() {
            timeline.play();
        }

        public HBox getTimer() {
            return timer;
        }

        private void initialize() {

            for (Label label : backupList) {
                label.setPrefSize(Main.TILE_SIZE / 2, Main.TILE_SIZE);
                label.setFont(new Font("Arial", Main.TILE_SIZE));
            }

            writeTimeDown();
        }
        private void writeTimeDown() {
            tensOfMinutes.setText(String.valueOf((timeLeft / 60) / 10));
            onesOfMinutes.setText(String.valueOf((timeLeft / 60) % 10));

            tensOfSeconds.setText(String.valueOf((timeLeft % 60) / 10));
            onesOfSeconds.setText(String.valueOf((timeLeft % 60) % 10));

        }

        private void subtractSecond() {
            timeLeft--;

        }

        public double getWidth() {
            return Main.TILE_SIZE * 2.5;
        }

        private void setTime(TimeControl timeControl) {
            if (timeControl == TimeControl.THREE) {
                timeLeft = 180;
            } else if (timeControl == TimeControl.FIVE) {
                timeLeft = 300;
            } else if (timeControl == TimeControl.FIFTEEN) {
                timeLeft = 900;
            } else if (timeControl == TimeControl.THREE_PLUS_TWO) {
                timeLeft = 180;
            }
        }

        private void checkEndGame() {
            if (timeLeft == 0) {
                timeline.stop();
                Result result = (onMove.getPieceColour() == PieceColour.BLACK) ? Result.WHITE : Result.BLACK;
                stage.setScene(createEndingScene(result, gameSupervisor, stage));
            }
        }
    }

    public HBox promotionHBox(Piece piece) {
        Button queen = new Button("Q");
        Button bishop = new Button("B");
        Button knight = new Button("K");
        Button rook = new Button("R");

        queen.setPrefSize(PROMOTION_BUTTON_SIZE, PROMOTION_BUTTON_SIZE);
        bishop.setPrefSize(PROMOTION_BUTTON_SIZE, PROMOTION_BUTTON_SIZE);
        knight.setPrefSize(PROMOTION_BUTTON_SIZE, PROMOTION_BUTTON_SIZE);
        rook.setPrefSize(PROMOTION_BUTTON_SIZE, PROMOTION_BUTTON_SIZE);

        queen.setOnAction(e -> {
            doPromotionButtonAction(PieceKind.QUEEN, piece);
        } );
        bishop.setOnAction(e -> {
            doPromotionButtonAction(PieceKind.BISHOP, piece);
        } );
        knight.setOnAction(e -> {
            doPromotionButtonAction(PieceKind.KNIGHT, piece);
        } );
        rook.setOnAction(e -> {
            doPromotionButtonAction(PieceKind.ROOK, piece);
        } );
        return new HBox(queen, bishop, knight, rook);
    }

    public void doPromotionButtonAction(PieceKind buttonKind, Piece movingPiece) {
        right.getChildren().remove(promotionBox);
        movingPiece.setKind(buttonKind);
        onMove.switchMode();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Fischer Chess");
        primaryStage.getIcons().add(new Image("sample/pawn.png"));
        primaryStage.setScene(MainMenu(primaryStage));
        primaryStage.show();
    }

    public static void main(String[] args) {

        Application.launch(args);
    }

}