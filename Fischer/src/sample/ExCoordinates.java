package sample;

import java.util.Random;

public class ExCoordinates {
    private PieceKind[] t;

    private int getPosition(PieceKind[] t, int place) {
        int i = 0;
        int counter = 0;
        while (true) {
            if (t [i] == null) {
                if (counter == place) {
                    break;
                } else {
                    i++;
                    counter++;
                }
            } else {
                i++;
            }
        }
        return i;
    }
    ExCoordinates(boolean FischerOnes) {
        t = new PieceKind[Main.SQUARE_NUMBER];
        if (FischerOnes) {
            Random randomBox = new Random();
            int lightSquaredBishop = randomBox.nextInt(Main.SQUARE_NUMBER / 2);
            t [lightSquaredBishop * 2 + 1] = PieceKind.BISHOP;
            int darkSquaredBishop = randomBox.nextInt(Main.SQUARE_NUMBER / 2);
            t [darkSquaredBishop * 2] = PieceKind.BISHOP;

            t [getPosition(t, randomBox.nextInt(6))] = PieceKind.QUEEN;
            t [getPosition(t, randomBox.nextInt(5))] = PieceKind.KNIGHT;
            t [getPosition(t, randomBox.nextInt(4))] = PieceKind.KNIGHT;
            t [getPosition(t, 0)] = PieceKind.ROOK;
            t [getPosition(t, 0)] = PieceKind.KING;
            t [getPosition(t, 0)] = PieceKind.ROOK;

        } else {
            t [0] = PieceKind.ROOK;
            t [1] = PieceKind.KNIGHT;
            t [2] = PieceKind.BISHOP;
            t [3] = PieceKind.QUEEN;
            t [4] = PieceKind.KING;
            t [5] = PieceKind.BISHOP;
            t [6] = PieceKind.KNIGHT;
            t [7] = PieceKind.ROOK;
        }

    }

    public PieceKind retrieveWhichPiece(int x) {
        return t [x];
    }

    public int yKing() {

        int i = 0;

        while (i < 8) {

            if (t [i] == PieceKind.KING) {

                return i;

            }

            i++;

        }

        return -1;

    }

    public int yLRook() {
        int i = 0;
        while (i < Main.SQUARE_NUMBER) {
            if (t [i] == PieceKind.ROOK) {
                return i;
            }
            i++;
        }
        return i;
    }

    public int yRRook() {
        int i = Main.SQUARE_NUMBER - 1;
        while (i >= 0) {
            if (t [i] == PieceKind.ROOK) {
                return i;
            }
            i--;
        }
        return i;
    }
}
