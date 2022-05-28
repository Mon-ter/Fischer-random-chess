package sample;

import javafx.util.Pair;

import java.util.ArrayList;

public class GameSupervisor {
    ArrayList<Pair<Note, Note>> game;
    int counter = 0;
    Tile [] [] board;

    GameSupervisor(Tile [][] board) {
        this.board = board;
        game = new ArrayList<>();
    }

    public void add(Pair<Note, Note> note) {
        game.add(note);
        counter++;
    }

    public String toString() {
        int limit = game.size();
        String result = "";
        for (int i = 0; i < limit; i++) {
            result += (i + ". " + game.get(i).getKey().from.getX() + game.get(i).getKey().from.getY()) + "\n";
        }
        return result;
    }

    public int realSize() {
        return game.size() - 1;
    }
    public void setCounter() {
        counter = game.size() - 1;
    }

    public void retraceMove() {
        if (counter >= 0) {
            Note noteOne = game.get(counter).getKey();
            Note noteTwo = game.get(counter).getValue();
            if (noteOne != null) {
                noteOne.piece.move(noteOne.from.getX(), noteOne.from.getY());
                board[noteOne.from.getX()][noteOne.from.getY()].setPiece(noteOne.piece);
                if (noteOne.to.getX() != -1) {
                    board[noteOne.to.getX()][noteOne.to.getY()].setPiece(null);
                }
            }
            if (noteTwo != null) {
                noteTwo.piece.move(noteTwo.from.getX(), noteTwo.from.getY());
                board[noteTwo.from.getX()][noteTwo.from.getY()].setPiece(noteTwo.piece);
            }
            counter--;
        }
    }

    public boolean repeatMove() {
        if (counter + 1 <= realSize()) {
            counter++;
            Note noteOne = game.get(counter).getKey();
            Note noteTwo = game.get(counter).getValue();
            if (noteTwo != null) {
                noteTwo.piece.move(noteTwo.to.getX(), noteTwo.to.getY());
                board[noteTwo.from.getX()][noteTwo.from.getY()].setPiece(null);
                if (noteTwo.to.getX() != -1) {
                    board[noteTwo.to.getX()][noteTwo.to.getY()].setPiece(noteTwo.piece);

                }
            }
            noteOne.piece.move(noteOne.to.getX(), noteOne.to.getY());
            board[noteOne.to.getX()][noteOne.to.getY()].setPiece(noteOne.piece);
            if (noteOne.from.getX() != -1) {
                board[noteOne.from.getX()][noteOne.from.getY()].setPiece(null);
            }

        }
        return counter == realSize();
    }

}
