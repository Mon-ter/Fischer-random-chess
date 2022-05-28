package sample;

public class TurnIndicator {

    private PieceColour pieceColour;
    private boolean gameMode;

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
