package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A discard pile.
 */
class DiscardPile {
    /*
     * Rep invariant:
     * - cardStack is not null and contains at least one Card.
     * - activeColor is not null.
     */

    /**
     * Stack representing the cards in the pile.
     */
    private final Stack<Card> cardStack;
    /**
     * The active color of the pile, only applies when the color of the top
     * card is {@code CardColor.NONE}.
     */
    private CardColor activeColor;

    /**
     * Create a new discard pile.
     */
    DiscardPile() {
        cardStack = new Stack<>();
        activeColor = CardColor.NONE;
    }

    /**
     * Check the top card of the discard pile, requires that the discard pile
     * is not empty.
     *
     * @return the card on the top of the discard pile
     */
    Card peek() {
        return cardStack.peek();
    }

    /**
     * @return the active color of the pile
     */
    CardColor getActiveColor() {
        return activeColor;
    }

    /**
     * @param newColor the new active color of the pile, not null
     */
    void setActiveColor(@NotNull CardColor newColor) {
        activeColor = newColor;
    }

    /**
     * Check if a card is playable to the discard pile, requires that the
     * discard pile is not empty.
     *
     * @param card the card to check, not null
     * @return true if the card is playable and false otherwise
     */
    boolean isPlayable(@NotNull Card card) {
        Card topCard = cardStack.peek();
        boolean playable = false;
        if (card.color() == CardColor.NONE) {
            playable = true;
        } else if (topCard.color() == CardColor.NONE) {
            if (card.color() == activeColor) {
                playable = true;
            }
        } else {
            if (card.color() == topCard.color()
                || card.type() == topCard.type()) {
                playable = true;
            }
        }
        return playable;
    }

    /**
     * Add a card to the discard pile.
     *
     * @param card the card to add, not null
     */
    void add(@NotNull Card card) {
        cardStack.push(card);
    }

    /**
     * Clears all cards from the discard pile.
     *
     * @return cards that were removed
     */
    List<Card> clear() {
        List<Card> oldCards = new ArrayList<>(cardStack);
        cardStack.clear();
        return oldCards;
    }

    /**
     * Clears all cards except the top card from the discard pile, requires
     * that the discard pile is not empty.
     *
     * @return cards that were removed
     */
    List<Card> clearExceptTop() {
        Card topCard = cardStack.pop();
        List<Card> oldCards = clear();
        cardStack.push(topCard);
        return oldCards;
    }
}
