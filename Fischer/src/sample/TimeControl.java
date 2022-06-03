package sample;

public enum TimeControl {
    NONE("No control"), THREE("3 + 0"), FIVE("5 + 0"), FIFTEEN("15 + 0"), THREE_PLUS_TWO("3 + 2");
    String name;
    TimeControl(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return name;
    }
}
