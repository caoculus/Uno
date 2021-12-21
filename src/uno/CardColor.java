package uno;

/**
 * Enum for the color of a card.
 */
enum CardColor {
    BLUE("Blue "),
    GREEN("Green "),
    RED("Red "),
    YELLOW("Yellow "),
    WILD("");

    private final String string;

    CardColor(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
