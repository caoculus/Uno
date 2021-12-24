package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A discard pile.
 */
class DiscardPile {
    /**
     * Stack representing the cards in the pile.
     */
    private final Stack<Card> cardStack;
    /**
     * The wild color of the pile, only applies when the color of the top
     * card is {@code CardColor.NONE}.
     */
    private CardColor wildColor;

    /**
     * Create a new discard pile.
     */
    DiscardPile() {
        cardStack = new Stack<>();
        wildColor = CardColor.NONE;
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
    CardColor getWildColor() {
        return wildColor;
    }

    /**
     * @param wildColor the new wild color of the pile, not null
     */
    void setWildColor(@NotNull CardColor wildColor) {
        this.wildColor = wildColor;
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
