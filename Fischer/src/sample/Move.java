package sample;

public class Move extends Square{

    MoveType type;

    Move(int x, int y, MoveType type) {
        super(x, y);
        this.type = type;
    }
}
