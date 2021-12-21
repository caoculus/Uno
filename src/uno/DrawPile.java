package uno;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Stack;

/**
 * A draw pile.
 */
class DrawPile {
    /**
     * Stack representing the cards in the pile.
     */
    private final Stack<Card> cardStack;

    /**
     * Create a new draw pile.
     */
    DrawPile() {
        cardStack = new Stack<>();
        for (CardType type : CardType.values()) {
            for (int i = 0; i < type.getFreq(); i++) {
                if (type.isWild()) {
                    cardStack.push(new Card(CardColor.WILD, type, i));
                } else {
                    cardStack.push(new Card(CardColor.BLUE, type, i));
                    cardStack.push(new Card(CardColor.GREEN, type, i));
                    cardStack.push(new Card(CardColor.RED, type, i));
                    cardStack.push(new Card(CardColor.YELLOW, type, i));
                }
            }
        }
    }

    /**
     * @return true if the draw pile is empty, and false otherwise
     */
    boolean isEmpty() {
        return cardStack.isEmpty();
    }

    /**
     * Add a card to the draw pile.
     *
     * @param card card to add, not null
     */
    void addCard(@NotNull Card card) {
        cardStack.push(card);
    }

    /**
     * Add a collection of cards to the draw pile.
     *
     * @param cards cards to add, not null
     */
    void addCards(@NotNull Collection<Card> cards) {
        cardStack.addAll(cards);
    }

    /**
     * Draw a card from the pile, requires that the pile is not empty.
     *
     * @return the drawn card
     */
    Card drawCard() {
        return cardStack.pop();
    }

    /**
     * Shuffle the cards in the draw pile.
     */
    void shuffle() {
        Collections.shuffle(cardStack);
    }
}