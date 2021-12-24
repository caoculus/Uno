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
     * The state of the discard pile.
     */
    private GameState state;

    /**
     * Create a new discard pile.
     */
    DiscardPile() {
        cardStack = new Stack<>();
        wildColor = CardColor.NONE;
        state = GameState.ROUND_START;
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
     * @param wildColor the new wild color of the pile, not null or {@code
     *                  CardColor.NONE}.
     * @throws IllegalStateException if the state is not {@code GameState
     *                               .CHANGE_COLOR}
     */
    void setWildColor(CardColor wildColor) {
        if (state != GameState.CHANGE_COLOR) {
            throw new IllegalStateException("Not in CHANGE_COLOR state!");
        }
        this.wildColor = wildColor;
        state = GameState.PLAY_CARD;
    }

    /**
     * Add the first card to the discard pile.
     *
     * @param card the card to add, not null
     * @return true if the card was successfully added, and false otherwise
     * @throws IllegalStateException if the state is not ({@code GameState
     *                               .ROUND_START}
     */
    boolean addFirst(Card card) {
        if (state != GameState.ROUND_START) {
            throw new IllegalStateException("Not in ROUND_START state!");
        }
        cardStack.push(card);
        state = GameState.PLAY_CARD;
        return true;
    }

    /**
     * Play a card to the discard pile.
     *
     * @param card the card to add, not null
     * @return true if the card was successfully played, and false otherwise
     * @throws IllegalStateException if the state is not {@code GameState
     * .PLAY_CARD}
     */
    boolean add(Card card) {
        if (state != GameState.PLAY_CARD) {
            throw new IllegalStateException("Not in PLAY_CARD state!");
        }
        if (!isPlayable(card)) {
            return false;
        }
        cardStack.push(card);
        if (card.type().isWild()) {
            state = GameState.CHANGE_COLOR;
        }
        return true;
    }

    /**
     * Clears all cards from the discard pile.
     *
     * @return cards that were removed
     */
    List<Card> clear() {
        List<Card> oldCards = new ArrayList<>(cardStack);
        cardStack.clear();
        state = GameState.ROUND_START;
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

    /**
     * @return the state of the discard pile
     */
    GameState getState() {
        return state;
    }
}
