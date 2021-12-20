package uno;

import org.jetbrains.annotations.NotNull;

/**
 * An Uno card.
 */
record Card(CardColor color, CardType type) {
    @Override
    public @NotNull
    String toString() {
        return color.toString() + type.toString();
    }
}
