package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;

public class Main extends Application {

    public static final int TILE_SIZE = 80;
    public static final int SQUARE_NUMBER = 8;
    public static final int PIECE_RADIUS = 30;
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

    private Parent createContent(boolean FischerOnes) {
        BorderPane root = new BorderPane();
        Pane center = new Pane();
        root.setCenter(center);

        center.setPrefSize(TILE_SIZE * SQUARE_NUMBER, TILE_SIZE * SQUARE_NUMBER);
        center.getChildren().addAll(tiles, pieces);

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
                    Piece piece = makePiece(PieceColour.BLACK, PieceKind.PAWN, x, y, board);
                    tile.setPiece(piece);
                    pieces.getChildren().add(piece);
                    darkAlivePieces.hire(piece);
                } else if (y == 6) {
                    Piece piece = makePiece(PieceColour.WHITE, PieceKind.PAWN, x, y, board);
                    tile.setPiece(piece);
                    pieces.getChildren().add(piece);
                    whiteAlivePieces.hire(piece);
                } else if (y == 0) {
                    Piece piece = makePiece(PieceColour.BLACK, positionCreator.retrieveWhichPiece(x), x, y, board);
                    tile.setPiece(piece);
                    pieces.getChildren().add(piece);
                    darkAlivePieces.hire(piece);
                } else if (y == 7) {
                    Piece piece = makePiece(PieceColour.WHITE, positionCreator.retrieveWhichPiece(x), x, y, board);
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

    private Piece makePiece(PieceColour colour, PieceKind kind, int x, int y, Tile [] [] board) {

        Piece  piece = new Piece(colour, kind, x, y, board, onMove);

        piece.setOnMouseReleased(e -> {
            int newX = conversionToSquareMiddle(piece.getLayoutX());
            int newY = conversionToSquareMiddle(piece.getLayoutY());

            int oldX = conversionToSquareMiddle(piece.getOldX());
            int oldY = conversionToSquareMiddle(piece.getOldY());

            Move result = tryMove(piece, newX, newY);
            if (result != null) {
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
                piece.move(newX, newY);
                board[oldX][oldY].setPiece(null);
                board[newX][newY].setPiece(piece);
                Pair<Note, Note> annotation = new Pair(firstMoved, secondMoved);
                gameSupervisor.add(annotation);
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


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Fischer Chess");
        Scene scene = new Scene(createContent(true));
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
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {

        Application.launch(args);
    }
}