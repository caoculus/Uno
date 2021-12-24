package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Game {
    static final int INITIAL_HAND_SIZE = 7;
    static final int MIN_PLAYERS = 2;
    static final int MAX_PLAYERS = 10;

    private static final Random RANDOM = new Random();

    private final int numPlayers;
    private final DrawPile drawPile;
    private final DiscardPile discardPile;
    private final Hand[] hands;
    private final Scoreboard scoreboard;
    private final PlayerCounter counter;
    private final List<Card> lastDrawnCards;

    private GameState state;
    private GameMove lastMove;
    private boolean isDrawFour;
    private boolean canCallUno;
    private boolean canChallengeUno;

    /**
     * Create a new Uno game.
     *
     * @param numPlayers number of players, between 2 and 10 inclusive
     */
    Game(int numPlayers) {
        this.numPlayers = numPlayers;
        drawPile = new DrawPile();
        discardPile = new DiscardPile();
        hands = new Hand[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            hands[i] = new Hand();
        }
        scoreboard = new Scoreboard(numPlayers);
        counter = new PlayerCounter(numPlayers);
        lastDrawnCards = new ArrayList<>();
        state = GameState.ROUND_START;
    }

    boolean startRound() {
        if (state != GameState.ROUND_START) {
            return false;
        }
        resetFlags();
        counter.reset(RANDOM.nextInt(numPlayers));
        dealCards();
        handleTopCard();
        return true;
    }

    boolean playCard(Card card) {
        if (state != GameState.PLAY_CARD) {
            return false;
        }
        Hand hand = hands[getActivePlayer()];
        if (!hand.contains(card) || discardPile.isPlayable(card)) {
            return false;
        }
        canChallengeUno = canCallUno;
        lastMove = GameMove.PLAY_CARD;
        hand.remove(card);
        discardPile.add(card);
        if (hand.isEmpty()) {
            state = GameState.ROUND_OVER;
            handleRoundOver();
        } else {
            handleTopCard();
        }
        return true;
    }

    boolean drawCard() {
        if (state != GameState.PLAY_CARD) {
            return false;
        }
        int activePlayer = getActivePlayer();
        canChallengeUno = false;
        lastMove = GameMove.DRAW_CARD;
        drawCards(activePlayer, 1);
        if (!lastDrawnCards.isEmpty() && discardPile.isPlayable(
            lastDrawnCards.get(0))) {
            // check if we need to call Uno again
            Hand hand = hands[activePlayer];
            canCallUno = (hand.size() == 2);
            state = GameState.PLAY_DRAWN_CARD;
        } else {
            counter.advancePlayer(1);
            startTurn();
        }
        return true;
    }

    boolean playDrawnCard(boolean play) {
        if (state != GameState.PLAY_DRAWN_CARD) {
            return false;
        }
        int activePlayer = getActivePlayer();
        if (play) {
            canChallengeUno = canCallUno;
            lastMove = GameMove.PLAY_CARD;
            Card drawnCard = lastDrawnCards.get(0);
            Hand hand = hands[activePlayer];
            hand.remove(drawnCard);
            discardPile.add(drawnCard);
            handleTopCard();
        } else {
            counter.advancePlayer(1);
            startTurn();
        }
        return true;
    }

    boolean callUno() {
        if (!(state == GameState.PLAY_CARD
            || state == GameState.PLAY_DRAWN_CARD) || !canCallUno) {
            return false;
        }
        lastMove = GameMove.CALL_UNO;
        canCallUno = false;
        return true;
    }

    boolean callLateUno() {
        if (state != GameState.PLAY_CARD || !canChallengeUno) {
            return false;
        }
        lastMove = GameMove.CALL_UNO;
        canChallengeUno = false;
        return true;
    }

    boolean challengeUno() {
        if (state != GameState.PLAY_CARD || !canChallengeUno) {
            return false;
        }
        int lastPlayed = counter.getNextPlayer(-1);
        drawCards(lastPlayed, 2);
        lastMove = GameMove.CHALLENGE_UNO;
        return true;
    }

    boolean changeColor(@NotNull CardColor color) {
        if (state != GameState.CHANGE_COLOR || color == CardColor.NONE) {
            return false;
        }
        discardPile.setWildColor(color);
        lastMove = GameMove.CHANGE_COLOR;
        counter.advancePlayer(1);
        if (isDrawFour) {
            isDrawFour = false;
            state = GameState.CHALLENGE_DRAW_FOUR;
        } else {
            startTurn();
        }
        return true;
    }

    boolean challengeDrawFour(boolean challenge) {
        if (state != GameState.CHALLENGE_DRAW_FOUR) {
            return false;
        }
        int lastPlayed = counter.getNextPlayer(-1);
        int activePlayer = getActivePlayer();
        if (challenge) {
            Hand lastHand = hands[lastPlayed];
            CardColor activeColor = discardPile.getWildColor();
            if (lastHand.containsColor(activeColor)) {
                // successful challenge
                drawCards(lastPlayed, 4);
                lastMove = GameMove.DRAW_FOUR_CHALLENGE_SUCCESS;
            } else {
                // failed challenge
                drawCards(activePlayer, 6);
                lastMove = GameMove.DRAW_FOUR_CHALLENGE_FAIL;
                counter.advancePlayer(1);
            }
        } else {
            // no challenge
            drawCards(activePlayer, 4);
            lastMove = GameMove.DRAW_FOUR;
            counter.advancePlayer(1);
        }
        startTurn();
        return true;
    }

    boolean resetRound() {
        if (state != GameState.ROUND_OVER) {
            return false;
        }
        scoreboard.newRound();
        collectCards();
        return true;
    }

    boolean resetGame() {
        if (state != GameState.ROUND_OVER) {
            return false;
        }
        scoreboard.reset();
        collectCards();
        return true;
    }

    int getNumPlayers() {
        return numPlayers;
    }

    GameState getState() {
        return state;
    }

    List<Card> getHand(int player) {
        return hands[player].getCards();
    }

    Direction getDirection() {
        return counter.getDirection();
    }

    int getActivePlayer() {
        return counter.getActivePlayer();
    }

    Card getTopCard() {
        return discardPile.peek();
    }

    boolean canCallUno() {
        return canCallUno;
    }

    boolean canChallengeUno() {
        return canChallengeUno;
    }

    GameMove getLastMove() {
        return lastMove;
    }

    boolean isGameOver() {
        return scoreboard.isGoalReached();
    }

    List<Card> getLastDrawnCards() {
        return new ArrayList<>(lastDrawnCards);
    }

    CardColor getActiveColor() {
        return discardPile.getWildColor();
    }

    List<List<Integer>> getScores() {
        return scoreboard.getScores();
    }

    private void resetFlags() {
        lastMove = GameMove.NONE;
        isDrawFour = false;
        canCallUno = false;
        canChallengeUno = false;
    }

    private void dealCards() {
        for (Hand hand : hands) {
            for (int i = 0; i < INITIAL_HAND_SIZE; i++) {
                hand.add(drawPile.drawCard());
            }
        }
        Card topCard;
        while ((topCard = drawPile.drawCard()).type()
            == CardType.WILD_DRAW_FOUR) {
            drawPile.add(topCard);
        }
        discardPile.addFirst(topCard);
    }

    private void handleTopCard() {
        Card topCard = discardPile.peek();
        switch (topCard.type()) {
        case DRAW_TWO -> {
            drawTwo();
            startTurn();
        }
        case REVERSE -> {
            if (numPlayers == MIN_PLAYERS) {
                skip();
            } else {
                reverse();
            }
            startTurn();
        }
        case SKIP -> {
            skip();
            startTurn();
        }
        case WILD -> state = GameState.CHANGE_COLOR;
        case WILD_DRAW_FOUR -> {
            state = GameState.CHANGE_COLOR;
            isDrawFour = true;
        }
        default -> {
            counter.advancePlayer(1);
            startTurn();
        }
        }
    }

    private void handleRoundOver() {
        Card topCard = discardPile.peek();
        if (topCard.type() == CardType.DRAW_TWO) {
            drawTwo();
        } else if (topCard.type() == CardType.WILD_DRAW_FOUR) {
            int nextPlayer = counter.getNextPlayer(1);
            drawCards(nextPlayer, 4);
        }
        updateScores();
    }

    private void drawTwo() {
        lastMove = GameMove.DRAW_TWO;
        int nextPlayer = counter.getNextPlayer(1);
        drawCards(nextPlayer, 2);
        counter.advancePlayer(2);
    }

    private void reverse() {
        lastMove = GameMove.REVERSE;
        counter.switchDirection();
    }

    private void skip() {
        lastMove = GameMove.SKIP;
        counter.advancePlayer(2);
    }

    private void startTurn() {
        int activePlayer = getActivePlayer();
        state = GameState.PLAY_CARD;
        Hand hand = hands[activePlayer];
        updateCanCallUno();
    }

    private void updateCanCallUno() {
        int activePlayer = getActivePlayer();
        Hand hand = hands[activePlayer];
        canCallUno = true;
        if (hand.size() != 2) {
            canCallUno = false;
        } else {
            Card topCard = discardPile.peek();
            CardColor wildColor = discardPile.getWildColor();
            for (Card card : hand.getCards()) {
                if (!card.isPlayable(topCard, wildColor)) {
                    canCallUno = false;
                }
            }
        }
    }

    private void drawCards(int player, int numCards) {
        lastDrawnCards.clear();
        Hand hand = hands[player];
        for (int i = 0; i < numCards; i++) {
            if (drawPile.isEmpty() && !replenishDrawPile()) {
                return;
            }
            Card drawnCard = drawPile.drawCard();
            hand.add(drawnCard);
            lastDrawnCards.add(drawnCard);
        }
    }

    private boolean replenishDrawPile() {
        List<Card> oldCards = discardPile.clearExceptTop();
        if (oldCards.isEmpty()) {
            return false;
        }
        drawPile.add(oldCards);
        return true;
    }

    private void updateScores() {
        int activePlayer = getActivePlayer();
        for (int i = 0; i < numPlayers; i++) {
            if (i != activePlayer) {
                int score = hands[i].getHandValue();
                scoreboard.addScore(activePlayer, i, score);
            }
        }
    }

    private void collectCards() {
        drawPile.add(discardPile.clear());
        for (Hand hand : hands) {
            drawPile.add(hand.clear());
        }
        state = GameState.ROUND_START;
    }
}
