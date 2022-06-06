package sample;

public class Move extends Square{

    MoveType type;

    Move(int x, int y, MoveType type) {
        super(x, y);
        this.type = type;
    }

    public MoveType getType() {
        return type;
    }

    public void setType(MoveType type) {
        this.type = type;
    }
}
