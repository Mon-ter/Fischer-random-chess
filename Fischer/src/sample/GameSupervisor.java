package sample;

import javafx.util.Pair;

import java.util.ArrayList;

public class GameSupervisor {
    ArrayList<Pair<Note, Note>> game;
    int counter = 0;
    Tile [] [] board;

    String pgnNotation = "";

    GameSupervisor(Tile [][] board) {
        this.board = board;
        game = new ArrayList<>();
    }

    public void add(Pair<Note, Note> note,boolean check, int pieceDuplication) {
        game.add(note);
        if(counter % 2 == 0){
            pgnNotation += (counter/2+1 + ". ");
        }
        if(note.getKey().piece.getKind() !=  PieceKind.PAWN){
            pgnNotation += note.getKey().piece.getKind().getName();
            if (pieceDuplication == 1){
                pgnNotation += note.getKey().from.getY();
            }
            if (pieceDuplication == 2){
                pgnNotation += note.getKey().from.getXasChar();
            }
        }
        if(check){
            if(note.getKey().piece.getKind() ==  PieceKind.PAWN) {
                pgnNotation += note.getKey().from.getXasChar();
            }
            pgnNotation += "x";
        }
        pgnNotation += note.getKey().to.getXasChar();
        pgnNotation += (7-note.getKey().to.getY()+1) + " ";
        counter++;
    }

    public String toString() {
        int limit = game.size();
        String result = "";
        for (int i = 0; i < limit; i++) {
            result += ((i+1) + ". " + game.get(i).getKey().from.getX() + game.get(i).getKey().from.getY()) + " ";
        }
        return result;
    }

    public String pgnSaver(){
        int limit = game.size();
        String pgn = "";
        for(int i = 0; i < limit; i++){
            if (i%2 == 0){
                pgn += (i/2+1);
            }
            pgn += " " + game.get(i).getKey().piece.getKind().getName();
            int n = game.get(i).getKey().to.getX();
            char ch = (char)(n+65);
            pgn += (ch + game.get(i).getKey().to.getY());
        }
        return pgn;
    }

    public int realSize() {
        return game.size() - 1;
    }

    public boolean anyMovesMade() {
        return game.size() != 0;
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
