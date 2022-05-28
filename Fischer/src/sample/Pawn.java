package sample;

public class Pawn extends Piece{

    public Pawn(PieceColour colour, PieceKind kind, int x, int y, Tile[][] board, TurnIndicator onMove) {
        super(colour, kind, x, y, board, onMove);
    }

    @Override
    public void findPossibleMoves(Tile [] [] board, int enPassantXPossibility, Piece ourKing, Piece enemyKing) {

    }
}
