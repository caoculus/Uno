package uno;

import org.jetbrains.annotations.NotNull;

/**
 * An Uno card.
 */
record Card(CardColor color, CardType type, int id)
    implements Comparable<Card> {
    @Override
    public @NotNull String toString() {
        return color.toString() + type.toString();
    }

    @Override
    public int compareTo(@NotNull Card o) {
        int diff1 = color.compareTo(o.color);
        if (diff1 != 0) {
            return diff1;
        }
        int diff2 = type.compareTo(o.type);
        if (diff2 != 0) {
            return diff2;
        }
        return Integer.compare(id, o.id);
    }
}
