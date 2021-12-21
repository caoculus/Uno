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
     * card is {@code CardColor.WILD}.
     */
    private CardColor activeColor;

    /**
     * Create a new discard pile.
     */
    DiscardPile() {
        cardStack = new Stack<>();
        activeColor = CardColor.WILD;
    }

    /**
     * Check the top card of the discard pile.
     *
     * @return the card on the top of the discard pile
     * @throws java.util.EmptyStackException if the discard pile is empty
     */
    Card peek() {
        return cardStack.peek();
    }

    /**
     * @param newColor the new active color of the pile, not null
     */
    void setActiveColor(@NotNull CardColor newColor) {
        activeColor = newColor;
    }

    /**
     * Check if a card is playable to the discard pile.
     *
     * @param card the card to check, not null
     * @return true if the card is playable and false otherwise
     */
    boolean isPlayable(@NotNull Card card) {
        Card topCard = cardStack.peek();
        boolean playable = false;
        if (card.color() == CardColor.WILD) {
            playable = true;
        } else if (topCard.color() == CardColor.WILD) {
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
    void addCard(@NotNull Card card) {
        cardStack.push(card);
    }

    /**
     * Clears all cards from the discard pile.
     *
     * @return list of cards that were removed
     */
    List<Card> clear() {
        List<Card> cardList = new ArrayList<>(cardStack);
        cardStack.clear();
        return cardList;
    }

    /**
     * Clears all cards except the top card from the discard pile.
     *
     * @return list of cards that were removed
     * @throws java.util.EmptyStackException if the discard pile was empty
     */
    List<Card> clearExceptTop() {
        Card topCard = cardStack.pop();
        List<Card> cardList = clear();
        cardStack.push(topCard);
        return cardList;
    }
}
