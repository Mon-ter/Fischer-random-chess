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

    private final GameSupervisor supervisor;

    private final Piece whiteKing;
    private final Piece blackKing;
    private final Army whitePieces;
    private final Army blackPieces;

    GameFile(GameSupervisor supervisor, Piece whiteKing, Piece blackKing, Army whitePieces, Army blackPieces){
        this.supervisor = supervisor;
        this.whiteKing = whiteKing;
        this.blackKing = blackKing;
        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
    }

    private IntPair squareDecryptor(char oldX, char oldY){
        int asciForLetter = 97, asciForNumber = 48;
        int x = oldX - asciForLetter;
        int y = Main.SQUARE_NUMBER - (oldY - asciForNumber);
        return new IntPair(x,y);
    }

    private int intDecryptor(char oldX){
        int asciForLetter = 97;
        return oldX - asciForLetter;
    }

    private PieceKind kindDecryptor(char c){
        PieceKind k = PieceKind.PAWN;
        switch (c){
            case 'Q' -> k = PieceKind.QUEEN;
            case 'N' -> k = PieceKind.KNIGHT;
            case 'R' -> k = PieceKind.ROOK;
            case 'B' -> k = PieceKind.BISHOP;
        }
        return k;
    }

    private void movePawn(char[] ch, PieceKind promotion, boolean takes){
        IntPair pair = squareDecryptor(ch[ch.length-2], ch[ch.length-1]);
        Note note2 = supervisor.board[pair.x][pair.y].getPiece() == null ? null : new Note(supervisor.board[pair.x][pair.y].getPiece(), pair.x, pair.y, -1,-1);
        int fromY = supervisor.counter % 2 == 0 ? pair.y +1 : pair.y-1;
        int fromX;
        if(takes){
            fromX = ch[0] < ch[ch.length-2] ? pair.x-1 : pair.x+1;
            if(supervisor.board[pair.x][pair.y].getPiece() == null){ //en passant
                note2 = new Note(supervisor.board[pair.x][fromY].getPiece(), pair.x, pair.y, -1,-1);
                supervisor.board[pair.x][fromY].setPiece(null);
            }
        } else{
            fromX = pair.x;
            fromY = supervisor.board[pair.x][fromY].getPiece() != null ? fromY : fromY + fromY - pair.y;
        }
        Note note = new Note(supervisor.board[fromX][fromY].getPiece(),fromX,fromY, pair.x, pair.y);
        Pair<Note,Note> annotation = new Pair(note, note2);
        if(note2 != null){
            if(note2.getPiece().getColour() == PieceColour.BLACK){
                blackPieces.kill(note2.getPiece());
            }else{
                whitePieces.kill(note2.getPiece());
            }
        }
        Piece piece = supervisor.board[fromX][fromY].getPiece();
        piece.move(pair.x, pair.y);
        if(promotion != PieceKind.PAWN) {
            piece.promote(promotion);
            piece.setPromotionMoveNumber(supervisor.realSize() + 1);
        }
        supervisor.board[pair.x][pair.y].setPiece(supervisor.board[fromX][fromY].getPiece());
        supervisor.board[fromX][fromY].setPiece(null);
        supervisor.add(annotation,0, null);
    }

    private void movePiece(char[] ch, int conflict, PieceKind kind){
        IntPair pair = squareDecryptor(ch[ch.length-2], ch[ch.length-1]);
        Note note2 = supervisor.board[pair.x][pair.y].getPiece() == null ? null : new Note(supervisor.board[pair.x][pair.y].getPiece(), pair.x, pair.y, -1,-1);
        Piece piece = new Piece(supervisor.counter % 2 == 1 ? PieceColour.WHITE : PieceColour.BLACK,
                kind, pair.x, pair.y, supervisor.board, null);
        piece.setOurKing(supervisor.counter % 2 == 1 ? whiteKing : blackKing);
        piece.setEnemyArmy(supervisor.counter % 2 == 1 ? blackPieces : whitePieces);
        piece.findPossibleMoves(0,false, true);
        boolean theOne = false;
        for(Move move : piece.getPossibleMoves()){
            Piece temp = supervisor.board[move.getX()][move.getY()].getPiece();
            if(move.type == MoveType.KILL && temp.getKind() == kind){
                if (conflict == 0){
                    theOne = true;
                }else if((conflict == 1 && intDecryptor(ch[1]) == move.getX()) || (conflict == 2 && 8 - ((int)ch[1] - 48) == move.getY())){
                    theOne = true;
                }
            }
            if(theOne) {
                int fromX = move.getX();
                int fromY = move.getY();
                piece = null;
                Piece piece1 = supervisor.board[fromX][fromY].getPiece();
                Note note = new Note(piece1, fromX, fromY, pair.x, pair.y);
                piece1.move(pair.x, pair.y);
                Pair<Note, Note> p = new Pair(note, note2);
                supervisor.add(p, 0, null);
                if(note2 != null){
                    if(piece1.getColour() == PieceColour.BLACK){
                        whitePieces.kill(note2.getPiece());
                    }else{
                        blackPieces.kill(note2.getPiece());
                    }
                }
                supervisor.board[pair.x][pair.y].setPiece(piece1);
                supervisor.board[fromX][fromY].setPiece(null);
                break;
            }
        }
    }

    private void moveKing(char[] ch){
        IntPair pair = squareDecryptor(ch[ch.length-2], ch[ch.length-1]);
        Piece piece =  supervisor.counter % 2 == 0 ? whiteKing : blackKing;
        Square from = piece.getCoordinates();
        int fromX = from.getX();
        int fromY = from.getY();
        Note note2 = supervisor.board[pair.x][pair.y].getPiece() == null ? null : new Note(supervisor.board[pair.x][pair.y].getPiece(), pair.x, pair.y, -1,-1);
        Note note = new Note(piece, fromX, fromY, pair.x, pair.y);
        Pair<Note, Note> p = new Pair(note, note2);
        if(note2 != null){
            if(piece.getColour() != PieceColour.BLACK){
                blackPieces.kill(note2.getPiece());
            }else{
                whitePieces.kill(note2.getPiece());
            }
        }
        piece.move(pair.x, pair.y);
        supervisor.add(p,0,null);
        supervisor.board[pair.x][pair.y].setPiece(piece);
        supervisor.board[fromX][fromY].setPiece(null);
    }

    private void castle (char[] ch){
        boolean sup = supervisor.counter % 2 == 0;
        Piece king = sup ? whiteKing : blackKing;
        int kingX = king.getCoordinates().getX();
        int kingY = king.getCoordinates().getY();
        int side, mult;
        if(ch.length == 3){
            side = 3;
            mult = 1;
        }else{
            side = -4;
            mult = -1;
        }
        Piece rook = supervisor.board[kingX + side][kingY].getPiece();
        king.move(kingX + mult * 2, kingY);
        rook.move(king.getCoordinates().getX()-mult, kingY);
        supervisor.board[king.getCoordinates().getX()][kingY].setPiece(king);
        supervisor.board[king.getCoordinates().getX()-mult][kingY].setPiece(rook);
        supervisor.board[kingX][kingY].setPiece(null);
        supervisor.board[kingX+side][kingY].setPiece(null);
        Note note = new Note(king, kingX, kingY, king.getCoordinates().getX(), king.getCoordinates().getY());
        Note note2 = new Note(rook, kingX+side, kingY, king.getCoordinates().getX()-mult, king.getCoordinates().getY());
        Pair<Note, Note> p = new Pair(note,note2);
        supervisor.add(p,0,null);
    }

    private void resolveMove(String move){
        move = move.replace("+", "").replace("=", "");
        System.out.println(move + " " + supervisor.counter);
        char[] ch = move.toCharArray();
        int conflict = 0;
        if(!(move.contains("x")) && move.length() >3){
            conflict = Character.isLowerCase(ch[1]) ? 1 : 2;
        }else if(move.length() > 4){
            conflict = Character.isLowerCase(ch[1]) ? 1 : 2;
        }
        if(Character.isLowerCase(ch[0])){ //pawn
            PieceKind prom = kindDecryptor(move.charAt(move.length() - 1));
            if(Character.isUpperCase(ch[ch.length-1])) {
                move = move.substring(0, move.length() - 1);
                ch = move.toCharArray();
            }
            movePawn(ch, prom,move.contains("x"));
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
            int sup = supervisor.counter;
            Scanner sc = new Scanner(reader);
            for(int i = 0; sc.hasNext(); i++){
                String line = sc.next();
                if(i%3 != 0) {
                    resolveMove(line);
                    if(sup == supervisor.counter){
                        System.out.println("ERROR");
                    }
                }
                sup = supervisor.counter;
            }
            supervisor.counter--;
        }catch(IOException err){
            System.err.format("IOException: %s%n", err);
        }
    }

}