package sample;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Main extends Application {

    public static final int TILE_SIZE = 80;
    public static final int SQUARE_NUMBER = 8;
    public static final int PIECE_SIZE = 70;
    public static final int PIECE_NUMBER = 16;
    
    public Tile [][] board = new Tile [SQUARE_NUMBER][SQUARE_NUMBER];

    private Group tiles = new Group();
    private Group pieces = new Group();

    Army whiteAlivePieces = new Army();
    Army darkAlivePieces = new Army();

    boolean doubleCheck = false;
    boolean check = false;

    int enPassantXPossibility;

    Square graveyard = null;

    Piece whiteKing = null;
    Piece darkKing = null;

    TurnIndicator onMove = null;
    GameSupervisor gameSupervisor = null;
    
    public static Color darkTileColour = Color.TOMATO;
    public static Color lightTileColour = Color.WHITESMOKE;
    public static String graphicFolder = "new";

    private Parent createContent(boolean FischerOnes, Stage stage) {
        BorderPane root = new BorderPane();
        Pane center = new Pane();
        Pane right = new Pane();
        root.setRight(right);
        root.setCenter(center);

        center.setPrefSize(TILE_SIZE * SQUARE_NUMBER, TILE_SIZE * SQUARE_NUMBER);
        center.getChildren().addAll(tiles, pieces);

        right.setPrefSize(TILE_SIZE * 3, TILE_SIZE * SQUARE_NUMBER);
        Button draw = new Button("Take draw");
        Button resign = new Button("Resign");
        draw.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(createEndingScene(Result.DRAW, gameSupervisor, stage));
            }
        });

        resign.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(createEndingScene(onMove.getPieceColour() == PieceColour.WHITE ? Result.BlACK : Result.WHITE, gameSupervisor, stage));
            }
        });

        HBox manualEndings = new HBox(draw, resign);
        manualEndings.relocate((TILE_SIZE * 3 - manualEndings.getWidth()) / 2, TILE_SIZE * 4);

        right.getChildren().add(manualEndings);

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

                if (y == 1) {
                    Piece piece = makePiece(PieceColour.BLACK, PieceKind.PAWN, x, y, board, stage);
                    tile.setPiece(piece);
                    pieces.getChildren().add(piece);
                    darkAlivePieces.hire(piece);
                } else if (y == 6) {
                    Piece piece = makePiece(PieceColour.WHITE, PieceKind.PAWN, x, y, board, stage);
                    tile.setPiece(piece);
                    pieces.getChildren().add(piece);
                    whiteAlivePieces.hire(piece);
                } else if (y == 0) {
                    Piece piece = makePiece(PieceColour.BLACK, positionCreator.retrieveWhichPiece(x), x, y, board, stage);
                    tile.setPiece(piece);
                    pieces.getChildren().add(piece);
                    darkAlivePieces.hire(piece);
                } else if (y == 7) {
                    Piece piece = makePiece(PieceColour.WHITE, positionCreator.retrieveWhichPiece(x), x, y, board, stage);
                    tile.setPiece(piece);
                    pieces.getChildren().add(piece);
                    whiteAlivePieces.hire(piece);
                }
            }
        }

        whiteAlivePieces.makeThemReady(board, enPassantXPossibility, whiteKing, darkKing);

        darkKing = board [positionCreator.yKing()] [0].getPiece();
        whiteKing = board [positionCreator.yKing()] [7].getPiece();

        return root;
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

            Move result = tryMove(piece, newX, newY);
            if (result != null) {
                boolean check = board[newX][newY].getPiece() != null;
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
                }
                if (result.type == MoveType.PAWN_DOUBLE_MOVE) {
                    enPassantXPossibility = newX;
                } else {
                    enPassantXPossibility = -2;
                }
                int pieceDuplicationm = 0;
                piece.move(newX, newY);
                board[oldX][oldY].setPiece(null);
                board[newX][newY].setPiece(piece);
                Pair<Note, Note> annotation = new Pair(firstMoved, secondMoved);
                gameSupervisor.add(annotation, check, pieceDuplicationm);
                if (onMove.getPieceColour() == PieceColour.WHITE) {
                    darkAlivePieces.makeThemReady(board, enPassantXPossibility, darkKing, whiteKing);
                } else {

                    whiteAlivePieces.makeThemReady(board, enPassantXPossibility, whiteKing, darkKing);
                }
                onMove.switchTurn();

            } else {
                piece.doNotMove();
            }

            piece.repaint();
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
    }

    private Scene createEndingScene(Result result, GameSupervisor gameSupervisor, Stage stage) {
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        String note;

        if (result == Result.WHITE) {
            note = "WHITE won";
        } else if (result == Result.BlACK) {
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
            writer.write("\n" + gameSupervisor.pgnNotation + "\n\n");
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
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.LEFT) {
                  if (onMove.getGameMode()) {
                      onMove.switchMode();
                      gameSupervisor.setCounter();
                  }
                  gameSupervisor.retraceMove();
            } else if (key.getCode() == KeyCode.RIGHT) {
                if (onMove.getGameMode()) {

                } else {
                    if(gameSupervisor.repeatMove()) {
                        onMove.switchMode();
                    }
                }
            }
        });

        return scene;
    }

    public Scene MainMenu(Stage stage){
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        Scene menu = new Scene(layout, 640, 640);
        
        Button button = new Button("Start Game");
        button.setOnAction(e -> stage.setScene(GameStartMenu(stage)));        
        Button button2 = new Button("Game From File");
        button2.setOnAction(e -> stage.setScene(readFromFileScene(stage)));
        Button button3 = new Button("Statistics");
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

        ChoiceBox choiceBox = new ChoiceBox();

        for (String folder : contents) {
            choiceBox.getItems().add(folder);
        }

        choiceBox.setOnAction((event) -> {
            graphicFolder = (String)choiceBox.getValue();
        });

        ToggleButton toggleButton = new ToggleButton("Fischer");

        Button button = new Button("Start New Game");
        button.setOnAction(e -> stage.setScene(createGame(toggleButton.isSelected(), stage)));  
        
        layout.getChildren().addAll(choiceBox, toggleButton, button);

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
                        //odpalenie gry z pliku
                    }
                }
            });

        Button button2 = new Button("Go back");
        button2.setOnAction(e -> stage.setScene(MainMenu(stage)));

        layout.getChildren().addAll(getFile, button2);

        return reading;
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
