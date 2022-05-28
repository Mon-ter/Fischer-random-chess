package sample;

import java.util.ArrayList;

public class Army {

    private ArrayList<Piece> pieces;

    public Army() {

        pieces = new ArrayList<Piece>();

    }

    public void hire(Piece newOne) {

        pieces.add(newOne);

    }

    public void kill(Piece takenPiece) {

        int limit = pieces.size();

        int i = 0;

        while (i < limit) {

            if (pieces.get(i) == takenPiece) {

                pieces.remove(i);
                break;

            }

            i++;

        }

        takenPiece.removeFromBoard();
    }

    public void makeThemReady(Tile [] [] board, int enPassantXPossibility, Piece ourKing, Piece enemyKing) {

        for (Piece piece : pieces) {
            piece.findPossibleMoves(board, enPassantXPossibility, ourKing, enemyKing);
        }
    }

}
