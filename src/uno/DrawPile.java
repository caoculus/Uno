package uno;

import java.util.Collections;
import java.util.Stack;

/**
 * A draw pile.
 */
class DrawPile {
    /**
     * Stack representing the cards in the draw pile.
     */
    private final Stack<Card> cardList;

    /**
     * Create a new draw pile.
     */
    DrawPile() {
        cardList = new Stack<>();
    }

    /**
     * @return true if the draw pile is empty, and false otherwise
     */
    boolean isEmpty() {
        return cardList.isEmpty();
    }

    /**
     * Add a card to the draw pile.
     *
     * @param card card to add
     */
    void addCard(Card card) {
        cardList.push(card);
    }

    /**
     * Add a list of cards to the draw pile.
     *
     * @param addList list of cards to add
     */
    void addCards(Stack<Card> addList) {
        cardList.addAll(addList);
    }

    /**
     * Draw a card from the pile, requires that the pile is not empty.
     *
     * @return the drawn card
     */
    Card drawCard() {
        return cardList.pop();
    }

    /**
     * Shuffle the cards in the draw pile.
     */
    void shuffle() {
        Collections.shuffle(cardList);
    }
}