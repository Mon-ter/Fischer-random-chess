package sample;

import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.lang.Math;
import java.util.ArrayList;

public class Piece extends StackPane {
    private PieceColour colour;
    private PieceKind kind;
    private double mouseX, mouseY;
    private double oldX, oldY;
    private TurnIndicator onMove;
    private Square coordinates;
    private ArrayList<Move> possibleMoves;
    private Tile [] [] board;
    private boolean isTargeted;
    private Army enemyArmy;
    private Piece ourKing;
    private Rectangle back;
    public double getOldX() {
        return oldX;
    }
    public Square getCoordinates() {return coordinates; }
    public double getOldY() {
        return oldY;
    }
    public PieceKind getKind() {
        return kind;
    }

    public void setIsTargeted() {
        isTargeted = true;
    }

    public void unsetIsTargeted() {
        isTargeted = false;
    }

    public boolean getIsTargeted() {
        return isTargeted;
    }

    public void setEnemyArmy(Army enemyArmy) {
        this.enemyArmy = enemyArmy;
    }

    public void setOurKing(Piece ourKing) {
        this.ourKing = ourKing;
    }

    public ArrayList<Move> getPossibleMoves() {
        return possibleMoves;
    }
    public void setKind (PieceKind kind) {
        this.kind = kind;
        String src = getImageSource(colour, kind, 0);
        back.setFill(new ImagePattern(new Image(src)));
    }

    public PieceColour getColour() {
        return colour;
    }

    public void removeFromBoard() {
        board [coordinates.getX()] [coordinates.getY()].setPiece(null);
        this.move(-1, -1);
    }

    public Piece(PieceColour colour, PieceKind kind, int x, int y, Tile [] [] board, TurnIndicator onMove) {
        this.colour = colour;
        this.kind = kind;
        this.board = board;
        this.onMove = onMove;
        this.isTargeted = false;
        coordinates = new Square(x, y);
        possibleMoves = new ArrayList<>();
        move(x, y);
        this.back = new Rectangle(Main.PIECE_SIZE, Main.PIECE_SIZE);
        back.setTranslateX((Main.TILE_SIZE - Main.PIECE_SIZE) / 2);
        back.setTranslateY((Main.TILE_SIZE - Main.PIECE_SIZE) / 2);
        String src = getImageSource(colour, kind, 0);
        back.setFill(new ImagePattern(new Image(src)));
        getChildren().addAll(back);

        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();

            if (onMove.getGameMode() && onMove.getPieceColour() == colour) {
                for (Square square : possibleMoves) {
                    board[square.getX()][square.getY()].highlight();
                }
            }
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
        });
    }

    public void move(int x, int y) {
        oldX = x * Main.TILE_SIZE;
        oldY = y * Main.TILE_SIZE;
        coordinates.set(x, y);
        relocate(oldX, oldY);
    }

    public void doNotMove() {
        relocate(oldX, oldY);
    }

    public String getImageSource(PieceColour colour, PieceKind kind, int mode) {

        String prefix = (colour == PieceColour.WHITE) ? "w" : "b";
        String rest = kind.getName();
        String extension = ".png";
        return "sample/graphics/" + Main.graphicFolder + "/" + prefix + rest + extension;
    }

    public void findPossibleMoves(int enPassantXPossibility, boolean checkMode) {

        possibleMoves = new ArrayList<>();


        if (this.getKind() == PieceKind.PAWN) {
            int direction = (getColour() == PieceColour.WHITE) ? -1 : 1;
            if (checkSquareAndAddIt(getCoordinates().getX(), getCoordinates().getY() + direction, enPassantXPossibility, false, false, checkMode)) {
                if (getColour() == PieceColour.WHITE && getCoordinates().getY() == 6) {
                    checkSquareAndAddIt(getCoordinates().getX(), getCoordinates().getY() + 2 * direction, enPassantXPossibility,false, false, checkMode);
                } else if (getColour() == PieceColour.BLACK && getCoordinates().getY() == 1) {
                    checkSquareAndAddIt(getCoordinates().getX(), getCoordinates().getY() +  2 * direction, enPassantXPossibility, false, false, checkMode);
                }
            }
            if (getColour() == PieceColour.WHITE && getCoordinates().getY() == 3) {
                checkSquareAndAddIt(getCoordinates().getX() + 1, getCoordinates().getY() + direction, enPassantXPossibility, true, true, checkMode);
                checkSquareAndAddIt(getCoordinates().getX() - 1, getCoordinates().getY() + direction, enPassantXPossibility,true, true, checkMode);
            } else if (getColour() == PieceColour.BLACK && getCoordinates().getY() == 4) {
                checkSquareAndAddIt(getCoordinates().getX() + 1, getCoordinates().getY() + direction, enPassantXPossibility, true, true, checkMode);
                checkSquareAndAddIt(getCoordinates().getX() - 1, getCoordinates().getY() + direction, enPassantXPossibility,true, true, checkMode);
            }
            checkSquareAndAddIt(getCoordinates().getX() + direction, getCoordinates().getY() + direction, enPassantXPossibility,false, true, checkMode);
            checkSquareAndAddIt(getCoordinates().getX() - direction, getCoordinates().getY() + direction, enPassantXPossibility,false, true, checkMode);
        } else

        if (this.getKind() == PieceKind.KNIGHT) {
            checkSquareAndAddIt(coordinates.getX() + 1, coordinates.getY() + 2, enPassantXPossibility,true, false, checkMode);
            checkSquareAndAddIt( coordinates.getX() + 1, coordinates.getY() - 2, enPassantXPossibility,true, false, checkMode);
            checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() + 2, enPassantXPossibility,true, false, checkMode);
            checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() - 2, enPassantXPossibility,true, false, checkMode);
            checkSquareAndAddIt(coordinates.getX() + 2, coordinates.getY() + 1, enPassantXPossibility,true, false, checkMode);
            checkSquareAndAddIt(coordinates.getX() + 2, coordinates.getY() - 1, enPassantXPossibility,true, false, checkMode);
            checkSquareAndAddIt(coordinates.getX() - 2, coordinates.getY() + 1, enPassantXPossibility,true, false, checkMode);
            checkSquareAndAddIt(coordinates.getX() - 2, coordinates.getY() - 1, enPassantXPossibility,true, false, checkMode);
        } else {

            if (this.getKind() == PieceKind.BISHOP || this.getKind() == PieceKind.QUEEN) {
                int i = 1;
                while (checkSquareAndAddIt(coordinates.getX() + i, coordinates.getY() + i, enPassantXPossibility,true, false, checkMode)) {
                    i++;
                }
                i = 1;
                while (checkSquareAndAddIt(coordinates.getX() - i, coordinates.getY() - i, enPassantXPossibility,true, false, checkMode)) {
                    i++;
                }
                i = 1;
                while (checkSquareAndAddIt(coordinates.getX() + i, coordinates.getY() - i, enPassantXPossibility,true, false, checkMode)) {
                    i++;
                }
                i = 1;
                while (checkSquareAndAddIt(coordinates.getX() - i, coordinates.getY() + i, enPassantXPossibility, true, false, checkMode)) {
                    i++;
                }
            }

            if (this.getKind() == PieceKind.ROOK || this.getKind() == PieceKind.QUEEN) {
                int i = 1;
                while (checkSquareAndAddIt(coordinates.getX(), coordinates.getY() + i, enPassantXPossibility,true, false, checkMode)) {
                    i++;
                }
                i = 1;
                while (checkSquareAndAddIt(coordinates.getX(), coordinates.getY() - i, enPassantXPossibility,true, false, checkMode)) {
                    i++;
                }
                i = 1;
                while (checkSquareAndAddIt(coordinates.getX() + i, coordinates.getY(), enPassantXPossibility, true, false, checkMode)) {
                    i++;
                }
                i = 1;
                while (checkSquareAndAddIt(coordinates.getX() - i, coordinates.getY(), enPassantXPossibility, true, false, checkMode)) {
                    i++;
                }
            } else if (this.getKind() == PieceKind.KING) {
                checkSquareAndAddIt(coordinates.getX() + 1, coordinates.getY() + 1, enPassantXPossibility,true, false, checkMode);
                checkSquareAndAddIt( coordinates.getX() + 1, coordinates.getY() - 1, enPassantXPossibility,true, false, checkMode);
                checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() + 1, enPassantXPossibility,true, false, checkMode);
                checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() - 1, enPassantXPossibility,true, false, checkMode);
                checkSquareAndAddIt( coordinates.getX(), coordinates.getY() + 1, enPassantXPossibility,true, false, checkMode);
                checkSquareAndAddIt( coordinates.getX(), coordinates.getY() - 1, enPassantXPossibility,true, false, checkMode);
                checkSquareAndAddIt( coordinates.getX() - 1, coordinates.getY(), enPassantXPossibility,true, false, checkMode);
                checkSquareAndAddIt(coordinates.getX() + 1, coordinates.getY() , enPassantXPossibility,true, false, checkMode);
            }
        }

    }

    public Move isMoveLegit(Square attempt) {

        int i = 0;
        int limit = possibleMoves.size();

        while (i < limit) {
            if (attempt.areTheyTheSame(possibleMoves.get(i))) {
                return possibleMoves.get(i);
            }
            i++;
        }

        return null;
    }


    public boolean checkSquareAndAddIt(int x, int y, int enPassantXPossibility, boolean canTake, boolean mustTake, boolean checkMode) {
            if (0 <= x && x <= 7 && 0 <= y && y <= 7) {
                if (canTake && mustTake) {
                    if (x == enPassantXPossibility) {
                        addMove(new Move(x, y, MoveType.EN_PASSANT), checkMode);
                        return true;
                    } else {
                        return false;
                    }
                }
                if (mustTake) {
                    if (board[x][y].hasPiece() && board[x][y].getPiece().getColour() != this.getColour()) {
                        addMove(new Move(x, y, MoveType.KILL), checkMode);
                        return true;
                    } else {
                        return false;
                    }
                }
                if (board[x][y].hasPiece()) {
                    if (board[x][y].getPiece().getColour() != this.getColour() && canTake) {
                        addMove(new Move(x, y, MoveType.KILL), checkMode);
                    }
                    return false;
                } else {
                    if (this.getKind() == PieceKind.PAWN && Math.abs(this.coordinates.getY() - y) == 2) {
                        addMove(new Move(x, y, MoveType.PAWN_DOUBLE_MOVE), checkMode);
                    } else {
                        addMove(new Move(x, y, MoveType.SIMPLE), checkMode);
                    }

                    return true;
                }
            } else {
                return false;
            }
    }


    public void addMove(Move move, boolean checkMode) {


        if (!checkMode) {

            if (move.getType() == MoveType.SIMPLE) {
                board[this.coordinates.getX()][this.coordinates.getY()].setPiece(null);
                board[move.getX()][move.getY()].setPiece(this);
                if (!enemyArmy.lookForChecks(-2, (this.getKind() == PieceKind.KING) ? new Square(move.getX(), move.getY()) : this.ourKing.coordinates)) {
                    if (this.getKind() == PieceKind.PAWN && (move.getY() == 0 || move.getY() == 7)) {
                        move.setType(MoveType.SIMPLE_PROMOTION);
                    }
                    possibleMoves.add(move);
                }
                board[this.coordinates.getX()][this.coordinates.getY()].setPiece(this);
                board[move.getX()][move.getY()].setPiece(null);
            } else if (move.getType() == MoveType.KILL) {
                board[this.coordinates.getX()][this.coordinates.getY()].setPiece(null);
                Piece temp = board[move.getX()][move.getY()].getPiece();
                temp.setIsTargeted();
                board[move.getX()][move.getY()].setPiece(this);
                if (!enemyArmy.lookForChecks(-2, (this.getKind() == PieceKind.KING) ? new Square(move.getX(), move.getY()) : this.ourKing.coordinates)){
                    if (this.getKind() == PieceKind.PAWN && (move.getY() == 0 || move.getY() == 7)) {
                        move.setType(MoveType.KILL_PROMOTION);
                    }
                    possibleMoves.add(move);
                }
                board[this.coordinates.getX()][this.coordinates.getY()].setPiece(this);
                board[move.getX()][move.getY()].setPiece(temp);
                temp.unsetIsTargeted();
            } else if (move.getType() == MoveType.PAWN_DOUBLE_MOVE) {
                board[this.coordinates.getX()][this.coordinates.getY()].setPiece(null);
                board[move.getX()][move.getY()].setPiece(this);
                if (!enemyArmy.lookForChecks(-2, ourKing.coordinates)) {
                    possibleMoves.add(move);
                }
                board[this.coordinates.getX()][this.coordinates.getY()].setPiece(this);
                board[move.getX()][move.getY()].setPiece(null);
            } else {
                possibleMoves.add(move);
            }
        } else {
            possibleMoves.add(move);
        }
    }
    public void repaint() {

        for (Move move : possibleMoves) {

            board [move.getX()] [move.getY()] .repaint();

        }
    }
}
