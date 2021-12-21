package uno;

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
    private List<Card> playableCards;


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
    }

    private void dealCards() {
        for (Hand hand : hands) {
            drawCards(hand, INITIAL_HAND_SIZE);
        }
    }

    private void handleTopCard() {
        activePlayer = RANDOM.nextInt(numPlayers);
        direction = 1;
        state = GameState.MOVE;
        switch (discardPile.peek().type()) {
        case DRAW_TWO -> {
            drawCards(hands[activePlayer], 2);
            advancePlayer();
        }
        case REVERSE -> direction = -direction;
        case WILD -> state = GameState.CHANGE_COLOR;
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
