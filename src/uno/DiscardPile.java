package uno;

import java.util.Stack;

/**
 * A discard pile. Only allows valid plays to be placed on top of the pile.
 */
public class DiscardPile {
    /*
     * Rep invariant:
     * - cardStack is not null and contains at least one Card.
     */

    /**
     * Stack representing the cards in the pile.
     */
    private final Stack<Card> cardStack;

    /**
     * Create a new discard pile.
     *
     * @param topCard first card on the top of the discard pile
     */
    DiscardPile(Card topCard) {
        cardStack = new Stack<>();
        cardStack.push(topCard);
    }

    /**
     * @return the card on the top of the discard pile
     */
    Card peek() {
        return cardStack.peek();
    }


}
