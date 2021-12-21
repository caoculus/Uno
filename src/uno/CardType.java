package uno;

/**
 * Enum for the type of card.
 */
enum CardType {
    ZERO("0", 1, false, 0),
    ONE("1", 2, false, 1),
    TWO("2", 2, false, 2),
    THREE("3", 2, false, 3),
    FOUR("4", 2, false, 4),
    FIVE("5", 2, false, 5),
    SIX("6", 2, false, 6),
    SEVEN("7", 2, false, 7),
    EIGHT("8", 2, false, 8),
    NINE("9", 2, false, 9),
    DRAW_TWO("Draw Two", 2, false, 20),
    REVERSE("Reverse", 2, false, 20),
    SKIP("Skip", 2, false, 20),
    WILD("Wild", 4, true, 50),
    WILD_DRAW_FOUR("Wild Draw Four", 4, true, 50);

    private final String string;
    private final int freq;
    private final boolean isWild;
    private final int value;

    /**
     * Create a new card type.
     *
     * @param string String to print for this card type
     * @param freq   number of cards of this card type in a given color
     * @param isWild whether this card type is wild
     * @param value  the point value of this card type
     */
    CardType(String string, int freq, boolean isWild, int value) {
        this.string = string;
        this.freq = freq;
        this.isWild = isWild;
        this.value = value;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * @return the number of cards of this card type in a given color
     */
    int getFreq() {
        return this.freq;
    }

    /**
     * @return true if this card type is wild, and false otherwise
     */
    boolean isWild() {
        return this.isWild;
    }

    /**
     * @return the point value of this card type
     */
    int getValue() {
        return value;
    }
}
