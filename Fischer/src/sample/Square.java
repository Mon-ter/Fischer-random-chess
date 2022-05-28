package sample;

public class Square {
    private int x;
    private int y;

    Square(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean areTheyTheSame(Square candidate) {
        return candidate.getX() == this.x && candidate.getY() == this.y;
    }
}
