package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    private final Scoreboard scoreboard;
    private final List<Card> playableCards;
    private final List<Card> lastDrawnCards;

    private GameState state;
    private GameMove lastMove;
    private Direction direction;
    private int activePlayer;
    private int lastPlayed;
    private int lastAttacked;
    private boolean isDrawFour;
    private boolean canCallUno;
    private boolean canChallengeUno;

    Game(int numPlayers) {
        if (numPlayers < MIN_PLAYERS || numPlayers > MAX_PLAYERS) {
            throw new IllegalArgumentException("Invalid number of players.");
        }
        this.numPlayers = numPlayers;
        drawPile = new DrawPile();
        discardPile = new DiscardPile();
        hands = new Hand[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            hands[i] = new Hand();
        }
        scoreboard = new Scoreboard(numPlayers);
        scoreboard.reset();
        playableCards = new ArrayList<>();
        lastDrawnCards = new ArrayList<>();
        state = GameState.ROUND_START;
    }

    boolean startRound() {
        if (state != GameState.ROUND_START) {
            return false;
        }
        resetFlags();
        activePlayer = RANDOM.nextInt(numPlayers);
        dealCards();
        handleTopCard();
        return true;
    }

    boolean playCard(int index) {
        if (state != GameState.PLAY_CARD || index < 0
            || index >= playableCards.size()) {
            return false;
        }
        canChallengeUno = canCallUno;
        lastPlayed = activePlayer;
        lastMove = GameMove.PLAY_CARD;
        Card card = playableCards.get(index);
        Hand hand = hands[activePlayer];
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
        canChallengeUno = false;
        lastPlayed = activePlayer;
        lastMove = GameMove.DRAW_CARD;
        drawCards(activePlayer, 1);
        if (!lastDrawnCards.isEmpty() && discardPile.isPlayable(
            lastDrawnCards.get(0))) {
            state = GameState.PLAY_DRAWN_CARD;
        } else {
            advancePlayer();
            startTurn();
        }
        return true;
    }

    boolean playDrawnCard(boolean play) {
        if (state != GameState.PLAY_DRAWN_CARD) {
            return false;
        }
        if (play) {
            lastMove = GameMove.PLAY_CARD;
            Card drawnCard = lastDrawnCards.get(0);
            Hand hand = hands[activePlayer];
            hand.remove(drawnCard);
            discardPile.add(drawnCard);
            handleTopCard();
        } else {
            advancePlayer();
            startTurn();
        }
        return true;
    }

    boolean callUno() {
        if (state != GameState.PLAY_CARD || !canCallUno) {
            return false;
        }
        lastMove = GameMove.CALL_UNO;
        lastPlayed = activePlayer;
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
        drawCards(lastPlayed, 2);
        lastMove = GameMove.CHALLENGE_UNO;
        lastAttacked = lastPlayed;
        lastPlayed = activePlayer;
        return true;
    }

    boolean changeColor(@NotNull CardColor color) {
        if (state != GameState.CHANGE_COLOR || color == CardColor.NONE) {
            return false;
        }
        discardPile.setActiveColor(color);
        lastMove = GameMove.CHANGE_COLOR;
        advancePlayer();
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
        lastAttacked = activePlayer;
        if (challenge) {
            Hand lastHand = hands[lastPlayed];
            CardColor activeColor = discardPile.getActiveColor();
            if (lastHand.containsColor(activeColor)) {
                // successful challenge
                drawCards(lastPlayed, 4);
                lastMove = GameMove.DRAW_FOUR_CHALLENGE_SUCCESS;
            } else {
                // failed challenge
                drawCards(activePlayer, 6);
                lastMove = GameMove.DRAW_FOUR_CHALLENGE_FAIL;
                advancePlayer();
            }
        } else {
            // no challenge
            drawCards(activePlayer, 4);
            lastMove = GameMove.DRAW_FOUR;
            advancePlayer();
        }
        startTurn();
        return true;
    }

    boolean resetRound() {
        if (state != GameState.ROUND_OVER) {
            return false;
        }
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

    GameState getState() {
        return state;
    }

    List<Card> getHand(int player) {
        Hand hand = hands[player];
        return hand.getCards();
    }

    Direction getDirection() {
        return direction;
    }

    int getActivePlayer() {
        return activePlayer;
    }

    List<Card> getPlayableCards() {
        return new ArrayList<>(playableCards);
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

    int getLastPlayed() {
        return lastPlayed;
    }

    int getLastAttacked() {
        return lastAttacked;
    }

    GameMove getLastMove() {
        return lastMove;
    }

    boolean isGameOver() {
        return scoreboard.isGoalReached();
    }

    private void resetFlags() {
        direction = Direction.CW;
        lastMove = GameMove.NONE;
        lastPlayed = -1;
        lastAttacked = -1;
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
        discardPile.add(topCard);
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
            advancePlayer();
            startTurn();
        }
        }
    }

    private void handleRoundOver() {
        Card topCard = discardPile.peek();
        if (topCard.type() == CardType.DRAW_TWO) {
            drawTwo();
        } else if (topCard.type() == CardType.WILD_DRAW_FOUR) {
            advancePlayer();
            drawCards(activePlayer, 4);
        }
        updateScores();
    }

    private void drawTwo() {
        advancePlayer();
        lastMove = GameMove.DRAW_TWO;
        drawCards(activePlayer, 2);
        lastAttacked = activePlayer;
        advancePlayer();
    }

    private void reverse() {
        direction = direction.opposite();
        lastMove = GameMove.REVERSE;
        advancePlayer();
    }

    private void skip() {
        advancePlayer();
        lastMove = GameMove.SKIP;
        lastAttacked = activePlayer;
        advancePlayer();
    }

    private void startTurn() {
        state = GameState.PLAY_CARD;
        updatePlayableCards();
        Hand hand = hands[activePlayer];
        canCallUno = (hand.size() == 2) && !playableCards.isEmpty();
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

    private void advancePlayer() {
        int step = (direction == Direction.CW) ? 1 : -1;
        activePlayer = Math.floorMod(activePlayer + step, numPlayers);
    }

    private void updatePlayableCards() {
        playableCards.clear();
        for (Card card : hands[activePlayer].getCards()) {
            if (discardPile.isPlayable(card)) {
                playableCards.add(card);
            }
        }
    }

    private void updateScores() {
        for (int i = 0; i < numPlayers; i++) {
            if (i != lastPlayed) {
                int score = hands[i].getHandValue();
                scoreboard.addScore(lastPlayed, i, score);
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
