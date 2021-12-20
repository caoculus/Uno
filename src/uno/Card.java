package uno;

/**
 * An Uno card.
 */
record Card(CardColor color, CardType type) {
    @Override
    public String toString() {
        return color.toString() + type.toString();
    }
}
