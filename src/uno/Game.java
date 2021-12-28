package uno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Game {
    /**
     * Number of cards dealt to each player at the start of the round.
     */
    static final int INITIAL_HAND_SIZE = 7;
    /**
     * Minimum number of players in a game.
     */
    static final int MIN_PLAYERS = 2;
    /**
     * Maximum number of players in a game.
     */
    static final int MAX_PLAYERS = 10;

    /**
     * Random number generator for choosing starting player.
     */
    private static final Random RANDOM = new Random();

    /**
     * Number of players in the game.
     */
    public final int numPlayers;

    /**
     * Draw pile for the game.
     */
    private final DrawPile drawPile;
    /**
     * Discard pile for the game.
     */
    private final DiscardPile discardPile;
    /**
     * Hands for each player.
     */
    private final Hand[] hands;
    /**
     * Scoreboard for the game.
     */
    private final Scoreboard scoreboard;
    /**
     * List of playable cards for the current turn.
     */
    private final List<Card> playableCards;
    /**
     * List of cards last drawn.
     */
    private final List<Card> lastDrawnCards;

    /**
     * The state of the game.
     */
    private GameState state;
    /**
     * The type of the most recent move in the game.
     */
    private GameMove lastMove;
    /**
     * The play direction.
     */
    private Direction direction;
    /**
     * The index of the active player (the player who currently has the turn).
     */
    private int activePlayer;
    /**
     * The index of the player who last played.
     */
    private int lastPlayed;
    /**
     * The index of the player who was last attacked (by a skip, draw two,
     * draw four, or Uno challenge).
     */
    private int lastAttacked;
    /**
     * Whether the most recent wild card play was a draw four.
     */
    private boolean isDrawFour;
    /**
     * Whether the active player can call Uno.
     */
    private boolean canCallUno;
    /**
     * Whether a player can be challenged for not calling Uno.
     */
    private boolean canChallengeUno;

    /**
     * Create a new game.
     *
     * @param numPlayers between 2 and 10 inclusive
     */
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

    /**
     * Start a new round.
     *
     * @throws IllegalStateException if the current state is not ROUND_START
     */
    void startRound() {
        if (state != GameState.ROUND_START) {
            throw new IllegalStateException("State is not ROUND_START");
        }
        resetFlags();
        activePlayer = RANDOM.nextInt(numPlayers);
        dealCards();
        handleTopCard();
    }

    /**
     * Have the active player play a card.
     *
     * @param index index of the card in the list of playable cards, must be
     *              in bounds
     * @throws IllegalStateException if the current state is not PLAY_CARD
     */
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

    /**
     * Have the active player draw a card.
     *
     * @throws IllegalStateException if the current state is not PLAY_CARD
     */
    void drawCard() {
        if (state != GameState.PLAY_CARD) {
            throw new IllegalStateException("State is not PLAY_CARD");
        }
        if (lastMove == GameMove.CALL_UNO) {
            throw new IllegalStateException(
                "Cannot draw card after calling Uno.");
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

    /**
     * Have the active player play or keep the card he/she just drew.
     *
     * @param play whether to play the most recently drawn card
     * @throws IllegalStateException if the current state is not PLAY_DRAWN_CARD
     */
    void playDrawnCard(boolean play) {
        if (state != GameState.PLAY_DRAWN_CARD) {
            throw new IllegalStateException("State is not PLAY_DRAWN_CARD");
        }
        if (lastMove == GameMove.CALL_UNO && !play) {
            throw new IllegalStateException(
                "Cannot keep card after calling Uno.");
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
            lastMove = GameMove.KEEP_CARD;
            advancePlayer();
            startTurn();
        }
    }

    /**
     * Have the active player call Uno.
     *
     * @throws IllegalStateException if the current state is not PLAY_CARD or
     *                               PLAY_DRAWN_CARD, or if {@code canCallUno
     *                               ()} is false
     */
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

    /**
     * Have the most recently played player call Uno.
     *
     * @throws IllegalStateException if the state is not PLAY_CARD or if
     *                               {@code canChallengeUno()} is false
     */
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

    /**
     * Have a player challenge the most recently played player for not calling
     * Uno.
     *
     * @param id index of the player who is challenging
     * @throws IllegalStateException if the state is not PLAY_CARD or if
     *                               {@code canChallengeUno} is false
     */
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

    /**
     * Change the color after playing a wild card.
     *
     * @param color the new color, not NONE
     * @throws IllegalStateException if the state is not CHANGE_COLOR
     */
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
        } else if (lastPlayed != -1) {
            startTurn();
        }
    }

    /**
     * Have the active player choose to challenge draw four.
     *
     * @param challenge whether to challenge
     * @throws IllegalStateException if the state is not CHALLENGE_DRAW_FOUR
     */
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

    /**
     * Reset the round.
     */
    void resetRound() {
        if (state != GameState.ROUND_OVER) {
            throw new IllegalStateException("State is not ROUND_OVER");
        }
        scoreboard.newRound();
        collectCards();
    }

    /**
     * Reset the game.
     */
    void resetGame() {
        if (state != GameState.ROUND_OVER) {
            throw new IllegalStateException("State is not ROUND_OVER");
        }
        scoreboard.reset();
        collectCards();
    }

    /**
     * @return the index of the active player
     */
    int getActivePlayer() {
        return activePlayer;
    }

    /**
     * @return the index of the player who played most recently
     */
    int getLastPlayed() {
        return lastPlayed;
    }

    /**
     * @return the index of the player who was attacked most recently
     */
    int getLastAttacked() {
        return lastAttacked;
    }

    /**
     * @return get the contents of the scoreboard
     */
    int[][] getScores() {
        return scoreboard.getScores();
    }

    /**
     * @return whether the active player can call Uno
     */
    boolean canCallUno() {
        return canCallUno;
    }

    /**
     * @return whether a player can be challenged for not calling Uno
     */
    boolean canChallengeUno() {
        return canChallengeUno;
    }

    /**
     * @return whether a player has reached the target score
     */
    boolean isGameOver() {
        return scoreboard.isGoalReached();
    }

    /**
     * @return the wild color of the discard pile
     */
    CardColor getWildColor() {
        return discardPile.getWildColor();
    }

    /**
     * @return the top card of the discard pile
     */
    Card getTopCard() {
        return discardPile.peek();
    }

    /**
     * @param player the index of the player
     * @return the cards in the player's hand
     */
    Card[] getHand(int player) {
        return hands[player].getCards().toArray(new Card[0]);
    }

    /**
     * @return the list of playable cards for the active player
     */
    Card[] getPlayableCards() {
        return playableCards.toArray(new Card[0]);
    }

    /**
     * @return the list of cards last drawn
     */
    Card[] getLastDrawnCards() {
        return lastDrawnCards.toArray(new Card[0]);
    }

    /**
     * @return the cards held by each player, by player index
     */
    Card[][] getHands() {
        Card[][] hands = new Card[numPlayers][];
        for (int i = 0; i < numPlayers; i++) {
            hands[i] = getHand(i);
        }
        return hands;
    }

    /**
     * @return the current play direction
     */
    Direction getDirection() {
        return direction;
    }

    /**
     * @return the current game state
     */
    GameState getState() {
        return state;
    }

    /**
     * @return the last game move
     */
    GameMove getLastMove() {
        return lastMove;
    }

    /**
     * At the start of the round, deal cards to each player and flip the
     * first card onto the discard pile.
     */
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

    /**
     * Reset flags for the start of the round.
     */
    private void resetFlags() {
        direction = Direction.CW;
        lastMove = GameMove.PLAY_CARD;
        lastPlayed = -1;
        lastAttacked = -1;
        isDrawFour = false;
        canCallUno = false;
        canChallengeUno = false;
    }

    /**
     * Change the game state according the top card of the discard pile.
     */
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

    /**
     * Change the game state according to the top card of the discard pile at
     * the end of the round.
     */
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

    /**
     * Perform the effects of a draw two card.
     */
    private void drawTwo() {
        advancePlayer();
        lastMove = GameMove.DRAW_TWO;
        drawCards(activePlayer, 2);
        lastAttacked = activePlayer;
        advancePlayer();
    }

    /**
     * Perform the effects of a reverse card.
     */
    private void reverse() {
        direction = direction.opposite();
        lastMove = GameMove.REVERSE;
        advancePlayer();
    }

    /**
     * Perform the effects of a skip card.
     */
    private void skip() {
        advancePlayer();
        lastMove = GameMove.SKIP;
        lastAttacked = activePlayer;
        advancePlayer();
    }

    /**
     * Update the game state before starting a player's turn.
     */
    private void startTurn() {
        state = GameState.PLAY_CARD;
        updatePlayableCards();
        Hand hand = hands[activePlayer];
        canCallUno = (hand.size() == 2) && !playableCards.isEmpty();
    }

    /**
     * Have a specified player draw a specified number of cards.
     *
     * @param player   index of the player
     * @param numCards number of cards
     */
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

    /**
     * Replenish the draw pile with cards from the discard pile after it is
     * exhausted.
     *
     * @return true if the discard pile had more than one card, and false
     * otherwise
     */
    private boolean replenishDrawPile() {
        List<Card> oldCards = discardPile.clearExceptTop();
        if (oldCards.isEmpty()) {
            return false;
        }
        drawPile.add(oldCards);
        return true;
    }

    /**
     * Advance the active player.
     */
    private void advancePlayer() {
        int step = (direction == Direction.CW) ? 1 : -1;
        activePlayer = Math.floorMod(activePlayer + step, numPlayers);
    }

    /**
     * Update the list of playable cards.
     */
    private void updatePlayableCards() {
        playableCards.clear();
        for (Card card : hands[activePlayer].getCards()) {
            if (discardPile.isPlayable(card)) {
                playableCards.add(card);
            }
        }
    }

    /**
     * Update the scoreboard.
     */
    private void updateScores() {
        for (int i = 0; i < numPlayers; i++) {
            if (i != lastPlayed) {
                int score = hands[i].getHandValue();
                scoreboard.addScore(lastPlayed, i, score);
            }
        }
    }

    /**
     * Collect cards from each player and the discard pile, and return them
     * to the draw pile.
     */
    private void collectCards() {
        drawPile.add(discardPile.clear());
        for (Hand hand : hands) {
            drawPile.add(hand.clear());
        }
        state = GameState.ROUND_START;
    }
}
