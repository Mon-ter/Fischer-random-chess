package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {

    private Piece piece;
    private final Color highlightColour = Color.YELLOW;
    Color colour;

    public boolean hasPiece() {
        return piece != null;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Tile(boolean isDark, int x, int y) {
        setWidth(Main.TILE_SIZE);
        setHeight(Main.TILE_SIZE);
        relocate(x * Main.TILE_SIZE, y * Main.TILE_SIZE);
        colour = isDark ? Main.darkTileColour : Main.lightTileColour;
        setFill(colour);
        piece = null;
    }

    public void highlight() {
        setFill(highlightColour);
    }

    public void repaint(){
        setFill(colour);
    }
}
