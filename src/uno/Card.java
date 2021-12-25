package uno;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An Uno card.
 */
class Card implements Comparable<Card> {
    private final CardColor color;
    private final CardType type;
    private final int id;

    /**
     * Create a new card.
     *
     * @param color color of the card
     * @param type type of the card
     * @param id id to differentiate between identical cards
     */
    Card(CardColor color, CardType type, int id) {
        this.color = color;
        this.type = type;
        this.id = id;
    }

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

    public CardColor color() {
        return color;
    }

    public CardType type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Card) obj;
        return Objects.equals(this.color, that.color) && Objects.equals(
            this.type, that.type) && this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type, id);
    }

}
