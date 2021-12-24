package uno;

import org.jetbrains.annotations.NotNull;

/**
 * An Uno card.
 */
record Card(CardColor color, CardType type, int id)
    implements Comparable<Card> {
    @Override
    public @NotNull String toString() {
        if (color == CardColor.NONE) {
            return type.toString();
        } else {
            return color.toString() + " " + type.toString();
        }
    }

    @Override
    public int compareTo(@NotNull Card o) {
        int colorDiff = color.compareTo(o.color);
        if (colorDiff != 0) {
            return colorDiff;
        }
        int typeDiff = type.compareTo(o.type);
        if (typeDiff != 0) {
            return typeDiff;
        }
        return Integer.compare(id, o.id);
    }

    /**
     * Check if this card is playable for a given top card and active color.
     *
     * @param topCard the top card of the discard pile
     * @param wildColor the wild color of the discard pile
     * @return true if this card is playable and false otherwise
     */
    boolean isPlayable(Card topCard, CardColor wildColor) {
        boolean playable = false;
        if (color == CardColor.NONE) {
            playable = true;
        } else if (topCard.color() == CardColor.NONE) {
            if (color == wildColor) {
                playable = true;
            }
        } else {
            if (color == topCard.color() || type == topCard.type()) {
                playable = true;
            }
        }
        return playable;
    }
}
