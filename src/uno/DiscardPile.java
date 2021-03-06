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
     * The color of the pile (of the top card, or the wild color if the top
     * card was wild, or {@code CardColor.NONE} if the pile was empty) before
     * the last wild card was played.
     */
    private CardColor beforeWildColor;

    /**
     * Create a new discard pile.
     */
    DiscardPile() {
        cardStack = new Stack<>();
        wildColor = CardColor.NONE;
        beforeWildColor = CardColor.NONE;
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
     * @return the wild color of the pile
     */
    CardColor getWildColor() {
        return wildColor;
    }

    /**
     * @return the color of the pile before the last wild card was played
     */
    CardColor getBeforeWildColor() {
        return beforeWildColor;
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
        if (card.type().isWild()) {
            if (cardStack.isEmpty()) {
                beforeWildColor = CardColor.NONE;
            } else {
                Card topCard = cardStack.peek();
                if (topCard.type().isWild()) {
                    beforeWildColor = wildColor;
                } else {
                    beforeWildColor = topCard.color();
                }
            }
        }
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


    /**
     * Check if a card is playable on the discard pile.
     *
     * @param card card to check
     * @return true if the card is playable and false otherwise
     */
    boolean isPlayable(@NotNull Card card) {
        return card.isPlayable(cardStack.peek(), wildColor);
    }
}
