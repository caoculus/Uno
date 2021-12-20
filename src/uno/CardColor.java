package uno;

/**
 * Enum for the color of a card.
 */
enum CardColor {
    BLUE, GREEN, RED, YELLOW, BLACK;

    @Override
    public String toString() {
        return switch (this) {
            case BLUE -> "Blue ";
            case GREEN -> "Green ";
            case RED -> "Red ";
            case YELLOW -> "Yellow ";
            case BLACK -> "";
        };
    }
}
