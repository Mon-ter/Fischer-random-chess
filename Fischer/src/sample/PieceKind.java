package sample;

public enum PieceKind {
    KING("king"), QUEEN("queen"), ROOK("rook"), BISHOP("bishop"), KNIGHT("knight"), PAWN("pawn");


    String name;

    PieceKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

