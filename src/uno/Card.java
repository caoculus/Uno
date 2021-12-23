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
}
