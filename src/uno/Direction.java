package uno;

public enum Direction {
    CW,
    CCW;

    public Direction opposite() {
        return switch (this) {
            case CW -> CCW;
            case CCW -> CW;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case CW -> "clockwise";
            case CCW -> "counterclockwise";
        };
    }
}
