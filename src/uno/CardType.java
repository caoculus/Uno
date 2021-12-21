package uno;

/**
 * Enum for the type of card.
 */
enum CardType {
    ZERO("0", 1, false),
    ONE("1", 2, false),
    TWO("2", 2, false),
    THREE("3", 2, false),
    FOUR("4", 2, false),
    FIVE("5", 2, false),
    SIX("6", 2, false),
    SEVEN("7", 2, false),
    EIGHT("8", 2, false),
    NINE("9", 2, false),
    REVERSE("Reverse", 2, false),
    SKIP("Skip", 2, false),
    DRAW_TWO("Draw Two", 2, false),
    WILD("Wild", 4, true),
    WILD_DRAW_FOUR("Wild Draw Four", 4, true);

    private final String string;
    private final int freq;
    private final boolean isWild;

    /**
     * Create a new card type.
     *
     * @param string String to print for this card type
     * @param freq number of cards of this card type in a given color
     * @param isWild whether this card type is wild
     */
    CardType(String string, int freq, boolean isWild) {
        this.string = string;
        this.freq = freq;
        this.isWild = isWild;
    }

    @Override
    public String toString() {
        return string;
    }

    int getFreq() {
        return this.freq;
    }

    boolean isWild() {
        return this.isWild;
    }
}
