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

    public void addOurKing(Piece king) {
        for (Piece piece : pieces) {
            piece.setOurKing(king);
        }
    }

    public void addEnemyArmy(Army enemyArmy) {
        for (Piece piece : pieces) {
            piece.setEnemyArmy(enemyArmy);
        }
    }
    public ArrayList<Piece> getPieces() {
        return pieces;
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

    public void makeThemReady(int enPassantXPossibility) {
        for (Piece piece : pieces) {
            piece.findPossibleMoves(enPassantXPossibility, false, false);
        }
    }

    public boolean doTheyHaveAnyMoves() {
        for (Piece piece : pieces) {
            if (piece.getPossibleMoves().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean lookForChecks(int enPassantXPossibility, Square enemyKingCoords) {
        for (Piece piece : pieces) {
            piece.findPossibleMoves(enPassantXPossibility, true, false);
        }

        for (Piece piece : pieces) {
            if (!piece.getIsTargeted()) {
                for (Move move : piece.getPossibleMoves()) {
                    if (move.areTheyTheSame(enemyKingCoords)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}