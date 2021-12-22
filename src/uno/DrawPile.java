package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * A draw pile.
 */
class DrawPile {
    /*
     * Rep invariant:
     * - cardList is not null and does not contain null elements.
     */
    /**
     * Random number generator for drawing cards.
     */
    private static final Random RANDOM = new Random();
    /**
     * List representing the cards in the pile.
     */
    private final List<Card> cardList;

    /**
     * Create a new draw pile.
     */
    DrawPile() {
        cardList = new ArrayList<>();
        for (CardType type : CardType.values()) {
            for (int i = 0; i < type.getFreq(); i++) {
                if (type.isWild()) {
                    cardList.add(new Card(CardColor.WILD, type, i));
                } else {
                    cardList.add(new Card(CardColor.BLUE, type, i));
                    cardList.add(new Card(CardColor.GREEN, type, i));
                    cardList.add(new Card(CardColor.RED, type, i));
                    cardList.add(new Card(CardColor.YELLOW, type, i));
                }
            }
        }
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
     * @param card card to add, not null
     */
    void addCard(@NotNull Card card) {
        cardList.add(card);
    }

    /**
     * Add a collection of cards to the draw pile.
     *
     * @param cards cards to add, not null
     */
    void addCards(@NotNull Collection<Card> cards) {
        cardList.addAll(cards);
    }

    /**
     * Draw a random card from the pile, requires that the pile is not empty.
     *
     * @return the drawn card
     */
    Card drawCard() {
        int index = RANDOM.nextInt(cardList.size());
        Card selected = cardList.get(index);
        if (index < cardList.size() - 1) {
            Card last = cardList.remove(cardList.size() - 1);
            cardList.set(index, last);
        }
        return selected;
    }
}