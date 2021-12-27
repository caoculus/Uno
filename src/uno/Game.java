package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Game {
    // TODO: fix logic for wild card first
    static final int INITIAL_HAND_SIZE = 7;
    static final int MIN_PLAYERS = 2;
    static final int MAX_PLAYERS = 10;

    private static final Random RANDOM = new Random();

    public final int numPlayers;

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

    void startRound() {
        if (state != GameState.ROUND_START) {
            throw new IllegalStateException("State is not ROUND_START");
        }
        resetFlags();
        activePlayer = RANDOM.nextInt(numPlayers);
        dealCards();
        handleTopCard();
    }


    void playCard(int index) {
        if (state != GameState.PLAY_CARD) {
            throw new IllegalStateException("State is not PLAY_CARD");
        }
        if (index < 0 || index >= playableCards.size()) {
            throw new IllegalArgumentException("Invalid index");
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
    }

    void drawCard() {
        if (state != GameState.PLAY_CARD) {
            throw new IllegalStateException("State is not PLAY_CARD");
        }
        canChallengeUno = false;
        lastPlayed = activePlayer;
        lastMove = GameMove.DRAW_CARD;
        drawCards(activePlayer, 1);
        if (!lastDrawnCards.isEmpty() && discardPile.isPlayable(
            lastDrawnCards.get(0))) {
            // check if we need to call Uno again
            Hand hand = hands[activePlayer];
            canCallUno = (hand.size() == 2);
            state = GameState.PLAY_DRAWN_CARD;
        } else {
            advancePlayer();
            startTurn();
        }
    }

    void playDrawnCard(boolean play) {
        if (state != GameState.PLAY_DRAWN_CARD) {
            throw new IllegalStateException("State is not PLAY_DRAWN_CARD");
        }
        if (play) {
            canChallengeUno = canCallUno;
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
    }

    void callUno() {
        if (!(state == GameState.PLAY_CARD
            || state == GameState.PLAY_DRAWN_CARD)) {
            throw new IllegalStateException(
                "State is not PLAY_CARD or PLAY_DRAWN_CARD");
        }
        if (!canCallUno) {
            throw new IllegalStateException("canCallUno is false");
        }
        lastMove = GameMove.CALL_UNO;
        lastPlayed = activePlayer;
        canCallUno = false;
    }

    void callLateUno() {
        if (state != GameState.PLAY_CARD) {
            throw new IllegalStateException("State is not PLAY_CARD");
        }
        if (!canChallengeUno) {
            throw new IllegalStateException("canChallengeUno is false");
        }
        lastMove = GameMove.CALL_UNO;
        canChallengeUno = false;
    }

    void challengeUno(int id) {
        if (state != GameState.PLAY_CARD) {
            throw new IllegalStateException("State is not PLAY_CARD");
        }
        if (!canChallengeUno) {
            throw new IllegalStateException("canChallengeUno is false");
        }
        drawCards(lastPlayed, 2);
        lastMove = GameMove.CHALLENGE_UNO;
        lastAttacked = lastPlayed;
        lastPlayed = id;
        canChallengeUno = false;
    }

    void changeColor(@NotNull CardColor color) {
        if (state != GameState.CHANGE_COLOR) {
            throw new IllegalStateException("State is not CHANGE_COLOR");
        }
        if (color == CardColor.NONE) {
            throw new IllegalArgumentException("Color is NONE");
        }
        discardPile.setWildColor(color);
        lastMove = GameMove.CHANGE_COLOR;
        advancePlayer();
        if (isDrawFour) {
            isDrawFour = false;
            state = GameState.CHALLENGE_DRAW_FOUR;
        } else {
            startTurn();
        }
    }

    void challengeDrawFour(boolean challenge) {
        if (state != GameState.CHALLENGE_DRAW_FOUR) {
            throw new IllegalStateException("State is not CHALLENGE_DRAW_FOUR");
        }
        lastAttacked = activePlayer;
        if (challenge) {
            Hand lastHand = hands[lastPlayed];
            CardColor beforeWildColor = discardPile.getBeforeWildColor();
            if (lastHand.containsColor(beforeWildColor)) {
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
    }

    void resetRound() {
        if (state != GameState.ROUND_OVER) {
            throw new IllegalStateException("State is not ROUND_OVER");
        }
        scoreboard.newRound();
        collectCards();
    }

    void resetGame() {
        if (state != GameState.ROUND_OVER) {
            throw new IllegalStateException("State is not ROUND_OVER");
        }
        scoreboard.reset();
        collectCards();
    }

    int getActivePlayer() {
        return activePlayer;
    }

    int getLastPlayed() {
        return lastPlayed;
    }

    int getLastAttacked() {
        return lastAttacked;
    }

    int[][] getScores() {
        return scoreboard.getScores();
    }

    boolean canCallUno() {
        return canCallUno;
    }

    boolean canChallengeUno() {
        return canChallengeUno;
    }

    boolean isGameOver() {
        return scoreboard.isGoalReached();
    }

    CardColor getWildColor() {
        return discardPile.getWildColor();
    }

    Card getTopCard() {
        return discardPile.peek();
    }

    Card[] getHand(int player) {
        return hands[player].getCards().toArray(new Card[0]);
    }

    Card[] getPlayableCards() {
        return playableCards.toArray(new Card[0]);
    }

    Card[] getLastDrawnCards() {
        return lastDrawnCards.toArray(new Card[0]);
    }

    Card[][] getHands() {
        Card[][] hands = new Card[numPlayers][];
        for (int i = 0; i < numPlayers; i++) {
            hands[i] = getHand(i);
        }
        return hands;
    }

    Direction getDirection() {
        return direction;
    }

    GameState getState() {
        return state;
    }

    GameMove getLastMove() {
        return lastMove;
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

    private void resetFlags() {
        direction = Direction.CW;
        lastMove = GameMove.PLAY_CARD;
        lastPlayed = -1;
        lastAttacked = -1;
        isDrawFour = false;
        canCallUno = false;
        canChallengeUno = false;
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
        case WILD, WILD_DRAW_FOUR -> {
            canCallUno = false;
            state = GameState.CHANGE_COLOR;
            isDrawFour = (topCard.type() == CardType.WILD_DRAW_FOUR);
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
