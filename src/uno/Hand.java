package uno;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * A player hand.
 */
public class Hand {
    /*
     * Rep invariant:
     * - cardSet is not null and does not contain null elements.
     */

    /**
     * Set of cards in the player hand.
     */
    private final Set<Card> cardSet;

    /**
     * Create a new player hand.
     */
    Hand() {
        cardSet = new TreeSet<>();
    }

    /**
     * @return the set of cards in the player hand
     */
    Set<Card> getCardSet() {
        return Collections.unmodifiableSet(cardSet);
    }

    /**
     * Add a card to the player hand if it is not present.
     *
     * @param card card to add, not null
     */
    void addCard(@NotNull Card card) {
        cardSet.add(card);
    }

    /**
     * Remove a card from the player hand if it is present.
     *
     * @param card card to remove, not null
     */
    void removeCard(@NotNull Card card) {
        cardSet.remove(card);
    }

    /**
     * Check if the player hand contains a card of a specified color.
     *
     * @param color color to check for
     * @return true if the player hand contains a card of color {@code color}
     * and false otherwise
     */
    boolean containsColor(@NotNull CardColor color) {
        for (Card card : cardSet) {
            if (card.color() == color) {
                return true;
            }
        }
        return false;
    }
}
