package uno;

import org.jetbrains.annotations.NotNull;

import java.util.Stack;

/**
 * A discard pile. Only allows valid plays to be placed on top of the pile.
 */
public class DiscardPile {
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
     *
     * @param topCard first card on the top of the discard pile, not null
     */
    DiscardPile(@NotNull Card topCard) {
        cardStack = new Stack<>();
        cardStack.push(topCard);
        activeColor = CardColor.WILD;
    }

    /**
     * @return the card on the top of the discard pile
     */
    Card peek() {
        return cardStack.peek();
    }

    /**
     * @param newColor the new active color of the pile, not null or {@code
     *                 CardColor.WILD}
     */
    void setActiveColor(@NotNull CardColor newColor) {
        if (newColor == CardColor.WILD) {
            throw new IllegalArgumentException("New color cannot be wild.");
        }
        activeColor = newColor;
    }

    /**
     * Attempt to play a card to the discard pile.
     *
     * @param card the card to play
     * @return true if the card was successfully played and added to the discard
     * pile, and false otherwise.
     */
    boolean playCard(@NotNull Card card) {
        Card topCard = peek();
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
        if (playable) {
            cardStack.push(card);
        }
        return playable;
    }
}
