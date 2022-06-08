package sample;

public class TurnIndicator {

    private PieceColour pieceColour;
    private boolean gameMode;

    private Piece wLRook;
    private Piece wRRook;
    private Piece wKing;

    private Piece bLRook;
    private Piece bRRook;
    private Piece bKing;

    public Piece getWhiteLeftRook() {
        return wLRook;
    }

    public Piece getWhiteRightRook() {
        return wRRook;
    }

    public Piece getWhiteKing() {
        return wKing;
    }

    public Piece getDarkLeftRook() {
        return bLRook;
    }

    public Piece getDarkRightRook() {
        return bRRook;
    }

    public Piece getDarkKing() {
        return bKing;
    }

    private boolean wLRookNotMoved = true;
    private boolean wRRookNotMoved = true;
    private boolean wKingNotMoved = true;
    private boolean bLRookNotMoved = true;
    private boolean bRRookNotMoved = true;
    private boolean bKingNotMoved = true;

    public void unsetWhiteRightRookNotMoved() {
        wRRookNotMoved = false;
    }

    public void unsetWhiteLeftRookNotMoved() {
        wLRookNotMoved = false;
    }

    public void unsetWhiteKingNotMoved() {
        wKingNotMoved = false;
    }

    public void unsetDarkRightRookNotMoved() {
        bRRookNotMoved = false;
    }

    public void unsetDarkLeftRookNotMoved() {
        bLRookNotMoved = false;
    }

    public void unsetDarkKingNotMoved() {
        bKingNotMoved = false;
    }

    public boolean canWhiteCastleKingSide() {
        return wKingNotMoved && wRRookNotMoved;
    }

    public boolean canWhiteCastleQueenSide() {
        return wKingNotMoved && wLRookNotMoved;
    }

    public boolean canBlackCastleKingSide() {
        return bKingNotMoved && bRRookNotMoved;
    }

    public boolean canBlackCastleQueenSide() {
        return bKingNotMoved && bLRookNotMoved;
    }

    public void findRooksAndKings(Piece LWR, Piece WK, Piece RWR, Piece LBR, Piece BK, Piece RBR) {
        wKing = WK;
        wLRook = LWR;
        wRRook = RWR;

        bKing = BK;
        bLRook = LBR;
        bRRook = RBR;
    }
    TurnIndicator(boolean gameMode) {
        this.gameMode = gameMode;
        pieceColour = PieceColour.WHITE;
    }

    public PieceColour getPieceColour() {
        return pieceColour;
    }

    public boolean getGameMode() { return gameMode; }


    public void switchMode() { gameMode = !gameMode; }

    public void switchTurn() {
        pieceColour = (pieceColour == PieceColour.WHITE) ? PieceColour.BLACK : PieceColour.WHITE;
    }

}
