package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

class Game {
    private static final int INITIAL_HAND_SIZE = 7;
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 10;
    private static final Random RANDOM = new Random();

    private final int numPlayers;
    private final DrawPile drawPile;
    private final DiscardPile discardPile;
    private final Hand[] hands;
    private final Integer[] scoreBoard;

    private GameState state;
    private int activePlayer;
    private int direction;
    private boolean isDrawFour;
    private boolean canUno;
    private boolean canChallenge;

    Game(int numPlayers) {
        this.numPlayers = numPlayers;
        drawPile = new DrawPile();
        discardPile = new DiscardPile();
        hands = new Hand[numPlayers];
        scoreBoard = new Integer[numPlayers];
        state = GameState.ROUND_START;
    }

    void startRound() {
        if (state != GameState.ROUND_START) {
            throw new IllegalStateException("State is not ROUND_START.");
        }
        drawPile.shuffle();
        dealCards();
        Card topCard;
        while ((topCard = drawPile.drawCard()).type()
            == CardType.WILD_DRAW_FOUR) {
            drawPile.addCard(topCard);
            drawPile.shuffle();
        }
        discardPile.addCard(topCard);
        activePlayer = RANDOM.nextInt(numPlayers);
        direction = 1;
        isDrawFour = false;
        canUno = false;
        canChallenge = false;
        handleCard(discardPile.peek());
    }

    private void dealCards() {
        for (Hand hand : hands) {
            drawCards(hand, INITIAL_HAND_SIZE);
        }
    }

    private void handleCard(@NotNull Card card) {
        switch (card.type()) {
        case DRAW_TWO -> {
            advancePlayer();
            drawCards(hands[activePlayer], 2);
            advancePlayer();
            state = GameState.MOVE;
        }
        case REVERSE -> {
            if (numPlayers > 2) {
                direction = -direction;
                advancePlayer();
            }
            state = GameState.MOVE;
        }
        case SKIP -> {
            advancePlayer();
            advancePlayer();
            state = GameState.MOVE;
        }
        case WILD -> state = GameState.CHANGE_COLOR;
        case WILD_DRAW_FOUR -> {
            state = GameState.CHANGE_COLOR;
            isDrawFour = true;
        }
        default -> advancePlayer();
        }
    }

    private void drawCards(Hand hand, int numCards) {
        for (int i = 0; i < numCards; i++) {
            if (drawPile.isEmpty() && !replenishDrawPile()) {
                return;
            }
            hand.addCard(drawPile.drawCard());
        }
    }

    private boolean replenishDrawPile() {
        Collection<Card> oldCards = discardPile.clearExceptTop();
        if (oldCards.isEmpty()) {
            return false;
        }
        drawPile.addCards(oldCards);
        drawPile.shuffle();
        return true;
    }

    private void advancePlayer() {
        activePlayer = (activePlayer + direction) % numPlayers;
    }

    int getActivePlayer() {
        return activePlayer;
    }

    List<Card> getPlayableCards() {
        List<Card> playableCards = new ArrayList<>();
        for (Card card : hands[activePlayer].getCards()) {
            if (discardPile.isPlayable(card)) {
                playableCards.add(card);
            }
        }
        return playableCards;
    }
}
