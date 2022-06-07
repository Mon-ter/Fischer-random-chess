package sample;

public class Note {

    Piece piece;
    Square from;
    Square to;

    Note(Piece piece, int xFrom, int yFrom, int xTo, int yTo) {
        this.piece = piece;
        this.from = new Square(xFrom, yFrom);
        this.to = new Square(xTo, yTo);
    }

    public Piece getPiece() {
        return piece;
    }

    public Square getFrom() {
        return from;
    }

    public Square getTo() {
        return to;
    }
}
