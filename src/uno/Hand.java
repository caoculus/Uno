package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
     * @return list of cards in the player hand, in sorted order
     */
    List<Card> getCards() {
        return new ArrayList<>(cardSet);
    }

    /**
     * Add a card to the player hand.
     *
     * @param card card to add, not null
     * @throws IllegalStateException if the hand already contained {@code card}
     */
    void add(@NotNull Card card) {
        if (!cardSet.add(card)) {
            throw new IllegalStateException("Hand already contains card.");
        }
    }

    /**
     * Remove a card from the player hand.
     *
     * @param card card to remove, not null
     * @throws IllegalStateException if the player hand did not contain
     *                               {@code card}
     */
    void remove(@NotNull Card card) {
        if (!cardSet.remove(card)) {
            throw new IllegalStateException("Hand did not contain card.");
        }
    }

    /**
     * Check if the player hand contains a specified card.
     *
     * @param card card to check for
     * @return true if the hand contains the card, and false otherwise
     */
    boolean contains(Card card) {
        return cardSet.contains(card);
    }

    /**
     * Clear all cards from the hand.
     *
     * @return cards that were removed
     */
    List<Card> clear() {
        List<Card> oldCards = new ArrayList<>(cardSet);
        cardSet.clear();
        return oldCards;
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

    /**
     * @return the total point value of all cards in the hand
     */
    int getHandValue() {
        int value = 0;
        for (Card card : cardSet) {
            value += card.type().getValue();
        }
        return value;
    }

    /**
     * @return the number of cards in this hand
     */
    int size() {
        return cardSet.size();
    }

    /**
     * @return true if this hand is empty, and false otherwise
     */
    boolean isEmpty() {
        return cardSet.size() == 0;
    }

    @Override
    public String toString() {
        return cardSet.toString();
    }
}
