package uno;

/**
 * Enum for the color of a card.
 */
enum CardColor {
    BLUE("Blue "),
    GREEN("Green "),
    RED("Red "),
    YELLOW("Yellow "),
    NONE("");

    private final String string;

    /**
     * Create a new card color.
     * @param string String to print for this card color
     */
    CardColor(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
