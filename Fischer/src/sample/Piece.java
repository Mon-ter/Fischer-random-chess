package sample;

import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
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

    public void setKind (PieceKind kind) {
        this.kind = kind;
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
        coordinates = new Square(x, y);
        possibleMoves = new ArrayList<>();
        move(x, y);
        Circle back = new Circle(Main.PIECE_RADIUS);
        back.setTranslateX((Main.TILE_SIZE - 2.0 * Main.PIECE_RADIUS) / 2);
        back.setTranslateY((Main.TILE_SIZE - 2.0 * Main.PIECE_RADIUS) / 2);
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
        return "sample/graphics/" + prefix + rest + extension;

    }

    public void findPossibleMoves(Tile [] [] board, int enPassantXPossibility, Piece ourKing, Piece enemyKing) {

        possibleMoves = new ArrayList<>();

                if (this.getKind() == PieceKind.PAWN) {
                    int direction = (getColour() == PieceColour.WHITE) ? -1 : 1;
                    if (checkSquareAndAddIt(getCoordinates().getX(), getCoordinates().getY() + direction, enPassantXPossibility, false, false)) {
                        if (getColour() == PieceColour.WHITE && getCoordinates().getY() == 6) {
                            checkSquareAndAddIt(getCoordinates().getX(), getCoordinates().getY() + 2 * direction, enPassantXPossibility,false, false);
                        } else if (getColour() == PieceColour.BLACK && getCoordinates().getY() == 1) {
                            checkSquareAndAddIt(getCoordinates().getX(), getCoordinates().getY() +  2 * direction, enPassantXPossibility, false, false);
                        }
                    }
                    if (getColour() == PieceColour.WHITE && getCoordinates().getY() == 3) {
                        checkSquareAndAddIt(getCoordinates().getX() + 1, getCoordinates().getY() + direction, enPassantXPossibility, true, true);
                        checkSquareAndAddIt(getCoordinates().getX() - 1, getCoordinates().getY() + direction, enPassantXPossibility,true, true);
                    } else if (getColour() == PieceColour.BLACK && getCoordinates().getY() == 4) {
                        checkSquareAndAddIt(getCoordinates().getX() + 1, getCoordinates().getY() + direction, enPassantXPossibility, true, true);
                        checkSquareAndAddIt(getCoordinates().getX() - 1, getCoordinates().getY() + direction, enPassantXPossibility,true, true);
                    }
                    checkSquareAndAddIt(getCoordinates().getX() + direction, getCoordinates().getY() + direction, enPassantXPossibility,false, true);
                    checkSquareAndAddIt(getCoordinates().getX() - direction, getCoordinates().getY() + direction, enPassantXPossibility,false, true);
                } else

                if (this.getKind() == PieceKind.KNIGHT) {
                    checkSquareAndAddIt(coordinates.getX() + 1, coordinates.getY() + 2, enPassantXPossibility,true, false);
                    checkSquareAndAddIt( coordinates.getX() + 1, coordinates.getY() - 2, enPassantXPossibility,true, false);
                    checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() + 2, enPassantXPossibility,true, false);
                    checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() - 2, enPassantXPossibility,true, false);
                    checkSquareAndAddIt(coordinates.getX() + 2, coordinates.getY() + 1, enPassantXPossibility,true, false);
                    checkSquareAndAddIt(coordinates.getX() + 2, coordinates.getY() - 1, enPassantXPossibility,true, false);
                    checkSquareAndAddIt(coordinates.getX() - 2, coordinates.getY() + 1, enPassantXPossibility,true, false);
                    checkSquareAndAddIt(coordinates.getX() - 2, coordinates.getY() - 1, enPassantXPossibility,true, false);
                } else {

                    if (this.getKind() == PieceKind.BISHOP || this.getKind() == PieceKind.QUEEN) {
                        int i = 1;
                        while (checkSquareAndAddIt(coordinates.getX() + i, coordinates.getY() + i, enPassantXPossibility,true, false)) {
                            i++;
                        }
                        i = 1;
                        while (checkSquareAndAddIt(coordinates.getX() - i, coordinates.getY() - i, enPassantXPossibility,true, false)) {
                            i++;
                        }
                        i = 1;
                        while (checkSquareAndAddIt(coordinates.getX() + i, coordinates.getY() - i, enPassantXPossibility,true, false)) {
                            i++;
                        }
                        i = 1;
                        while (checkSquareAndAddIt(coordinates.getX() - i, coordinates.getY() + i, enPassantXPossibility, true, false)) {
                            i++;
                        }
                    }

                    if (this.getKind() == PieceKind.ROOK || this.getKind() == PieceKind.QUEEN) {
                        int i = 1;
                        while (checkSquareAndAddIt(coordinates.getX(), coordinates.getY() + i, enPassantXPossibility,true, false)) {
                            i++;
                        }
                        i = 1;
                        while (checkSquareAndAddIt(coordinates.getX(), coordinates.getY() - i, enPassantXPossibility,true, false)) {
                            i++;
                        }
                        i = 1;
                        while (checkSquareAndAddIt(coordinates.getX() + i, coordinates.getY(), enPassantXPossibility, true, false)) {
                            i++;
                        }
                        i = 1;
                        while (checkSquareAndAddIt(coordinates.getX() - i, coordinates.getY(), enPassantXPossibility, true, false)) {
                            i++;
                        }
                    } else if (this.getKind() == PieceKind.KING) {
                        checkSquareAndAddIt(coordinates.getX() + 1, coordinates.getY() + 1, enPassantXPossibility,true, false);
                        checkSquareAndAddIt( coordinates.getX() + 1, coordinates.getY() - 1, enPassantXPossibility,true, false);
                        checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() + 1, enPassantXPossibility,true, false);
                        checkSquareAndAddIt(coordinates.getX() - 1, coordinates.getY() - 1, enPassantXPossibility,true, false);
                        checkSquareAndAddIt( coordinates.getX(), coordinates.getY() + 1, enPassantXPossibility,true, false);
                        checkSquareAndAddIt( coordinates.getX(), coordinates.getY() - 1, enPassantXPossibility,true, false);
                        checkSquareAndAddIt( coordinates.getX() - 1, coordinates.getY(), enPassantXPossibility,true, false);
                        checkSquareAndAddIt(coordinates.getX() + 1, coordinates.getY() , enPassantXPossibility,true, false);
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


    public boolean checkSquareAndAddIt(int x, int y, int enPassantXPossibility, boolean canTake, boolean mustTake) {
        if (0 <= x && x <= 7 && 0 <= y && y <= 7) {
            if (canTake && mustTake) {
                if (x == enPassantXPossibility) {
                    possibleMoves.add(new Move(x, y, MoveType.EN_PASSANT));
                    return true;
                } else {
                    return false;
                }
            }
            if (mustTake) {
                if (board[x][y].hasPiece() && board[x][y].getPiece().getColour() != this.getColour()) {
                    possibleMoves.add(new Move(x, y, MoveType.KILL));
                    return true;
                } else {
                    return false;
                }
            }
            if (board[x][y].hasPiece()) {
                if (board[x][y].getPiece().getColour() != this.getColour() && canTake) {
                    possibleMoves.add(new Move(x, y, MoveType.KILL));
                }
                return false;
            } else {
                if (this.getKind() == PieceKind.PAWN && Math.abs(this.coordinates.getY() - y) == 2) {
                    possibleMoves.add(new Move(x, y, MoveType.PAWN_DOUBLE_MOVE));
                } else {
                    possibleMoves.add(new Move(x, y, MoveType.SIMPLE));
                }

                return true;
            }
        } else {
            return false;
        }
    }
    public void repaint() {

        for (Move move : possibleMoves) {

            board [move.getX()] [move.getY()] .repaint();

        }
    }
}
