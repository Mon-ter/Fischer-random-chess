package sample;

import javafx.util.Pair;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class GameFile {

    static class IntPair{
        final int x;
        final int y;
        IntPair(int x, int y){this.x = x; this.y = y;}
    }

    private GameSupervisor supervisor = null;

    private Square graveyard = new Square(-1,-1);

    private Piece whiteKing = null;
    private Piece blackKing = null;
    private Army whitePieces = null;
    private Army blackPieces = null;

    GameFile(GameSupervisor supervisor, Piece whiteKing, Piece blackKing, Army whitePieces, Army blackPieces){
        this.supervisor = supervisor;
        this.whiteKing = whiteKing;
        this.blackKing = blackKing;
        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
    }

    private IntPair squareDecryptor(char oldX, char oldY){
        int x = oldX - 97;
        int y = 8 - (oldY - 48);
        return new IntPair(x,y);
    }

    private int intDecryptor(char oldX){
        return oldX - 97;
    }

    private int absol(int x){
        return x > 0 ? x : -x;
    }

    private void movePawn(char[] ch){
        IntPair pair = squareDecryptor(ch[ch.length-2], ch[ch.length-1]);
        int fromY = supervisor.counter % 2 == 0 ? pair.y +1 : pair.y-1;
        int fromX;
        if(ch.length >= 3){
            fromX = ch[0] < ch[3] ? pair.x-1 : pair.x+1;
            if(supervisor.board[pair.x][pair.y].getPiece() == null){
                supervisor.board[pair.x][fromY].setPiece(null); //en passant
            }
        } else{
            fromX = pair.x;
            fromY = supervisor.board[pair.x][fromY].getPiece() != null ? fromY : fromY + fromY - pair.y;
        }
        Note note2 = supervisor.board[pair.x][pair.y].getPiece() == null ? null : new Note(supervisor.board[pair.x][pair.y].getPiece(), pair.x, pair.y, -1,-1);
        Note note = new Note(supervisor.board[fromX][fromY].getPiece(),fromX,fromY, pair.x, pair.y);
        Pair<Note,Note> annotation = new Pair(note, note2);
        supervisor.board[fromX][fromY].getPiece().move(pair.x, pair.y);
        supervisor.board[fromX][fromY].setPiece(null);
        supervisor.add(annotation,false,0);
    }

    private void movePiece(char[] ch, int conflict, PieceKind kind){
        IntPair pair = squareDecryptor(ch[ch.length-2], ch[ch.length-1]);
        Note note2 = supervisor.board[pair.x][pair.y].getPiece() == null ? null : new Note(supervisor.board[pair.x][pair.y].getPiece(), pair.x, pair.y, -1,-1);
        Piece piece = new Piece(supervisor.counter % 2 == 1 ? PieceColour.WHITE : PieceColour.BLACK,
                kind, pair.x, pair.y, supervisor.board, null);
        piece.setOurKing(supervisor.counter % 2 == 1 ? whiteKing : blackKing);
        piece.setEnemyArmy(supervisor.counter % 2 == 1 ? blackPieces : whitePieces);
        piece.findPossibleMoves(0,false);
        boolean theOne = false;
        for(Move move : piece.getPossibleMoves()){
            Piece temp = supervisor.board[move.getX()][move.getY()].getPiece();
            if(move.type == MoveType.KILL && temp.getKind() == kind){
                if (conflict == 0){
                    theOne = true;
                }else if((conflict == 1 && intDecryptor(ch[1]) == move.getX()) || (conflict == 2 && (int)ch[1] - 40 == move.getY())){
                    theOne = true;
                }
            }
            if(theOne) {
                int fromX = move.getX();
                int fromY = move.getY();
                piece = null;
                Note note = new Note(supervisor.board[fromX][fromY].getPiece(), fromX, fromY, pair.x, pair.y);
                supervisor.board[fromX][fromY].getPiece().move(pair.x, pair.y);
                Pair<Note, Note> p = new Pair(note, note2);
                supervisor.add(p, false, 0);
                break;
            }
        }
    }

    private void moveKing(char[] ch){
        IntPair pair = squareDecryptor(ch[ch.length-2], ch[ch.length-1]);
        Piece piece =  supervisor.counter % 2 == 0 ? whiteKing : blackKing;
        Square from = piece.getCoordinates();
        Note note2 = supervisor.board[pair.x][pair.y].getPiece() == null ? null : new Note(supervisor.board[pair.x][pair.y].getPiece(), pair.x, pair.y, -1,-1);
        Note note = new Note(piece, from.getX(), from.getY(), pair.x, pair.y);
        Pair<Note, Note> p = new Pair(note, note2);
        piece.move(pair.x, pair.y);
        supervisor.add(p,false,0);
    }

    private void castle (char[] ch){
        boolean sup = supervisor.counter % 2 == 0;
        if(sup){
            if(ch.length == 3){

            }
        }
    }

    private void resolveMove(String move){
        move = move.replace("+", "");
        char[] ch = move.toCharArray();
        int conflict = 0;
        if(!(move.contains("x")) && move.length() >3){
            conflict = Character.isLowerCase(ch[1]) ? 1 : 2;
        }else if(move.length() > 4){
            conflict = Character.isLowerCase(ch[1]) ? 1 : 2;
        }
        if(Character.isLowerCase(ch[0])){ //pawn
           movePawn(ch);
        }else{
            switch(ch[0]){
                case 'N' -> movePiece(ch,conflict, PieceKind.KNIGHT);
                case 'Q' -> movePiece(ch,conflict, PieceKind.QUEEN);
                case 'K' -> moveKing(ch);
                case 'R' -> movePiece(ch,conflict, PieceKind.ROOK);
                case 'B' -> movePiece(ch,conflict, PieceKind.BISHOP);
                case '0' -> castle(ch);
                case 'O' -> castle(ch);
            }
        }
    }

    public void readMoves(String filePath){
        try(FileReader reader = new FileReader(filePath)){
            Scanner sc = new Scanner(reader);
            for(int i = 0; sc.hasNext(); i++){
                String line = sc.next();
                if(i%3 != 0) {
                    resolveMove(line);
                }
            }
            supervisor.counter--;
        }catch(IOException err){
            System.err.format("IOException: %s%n", err);
        }
    }

}
