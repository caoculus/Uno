package uno;

/**
 * An Uno card.
 */
class Card {
    /**
     * The color of the card.
     */
    private final CardColor color;
    /**
     * The type of the card.
     */
    private final CardType type;

    /**
     * Create a new card.
     *
     * @param color the color of the card
     * @param type  the type of the card
     */
    Card(CardColor color, CardType type) {
        this.color = color;
        this.type = type;
    }

    @Override
    public String toString() {
        return color.toString() + type.toString();
    }
}
