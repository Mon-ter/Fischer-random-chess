package sample;

public class Square {
    private int x;
    private int y;
    public final int charConstant = 97;

    Square(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    char getXasChar(){
        int n = x + charConstant;
        return (char)n;
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