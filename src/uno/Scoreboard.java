package uno;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Scoreboard for an Uno game.
 */
class Scoreboard {
    /*
     * Rep invariant:
     * - All arrays are not null.
     * - numPlayers > 0.
     */

    /**
     * Score needed to win the game.
     */
    static final int GOAL_SCORE = 500;

    /**
     * Number of players.
     */
    private final int numPlayers;
    /**
     * Scores for previous round.
     */
    private final Integer[] prevScores;
    /**
     * Point contributions from each player for current round.
     */
    private final Integer[] addedScores;
    /**
     * Scores for current round.
     */
    private final Integer[] currScores;
    /**
     * Whether a player has reached {@code GOAL_SCORE}.
     */
    private boolean goalReached;

    /**
     * Create a new scoreboard.
     *
     * @param numPlayers number of players, positive
     */
    Scoreboard(int numPlayers) {
        this.numPlayers = numPlayers;
        prevScores = new Integer[numPlayers];
        addedScores = new Integer[numPlayers];
        currScores = new Integer[numPlayers];
        reset();
    }

    /**
     * Reset the scoreboard.
     */
    void reset() {
        Arrays.fill(prevScores, 0);
        Arrays.fill(addedScores, 0);
        Arrays.fill(currScores, 0);
        goalReached = false;
    }

    /**
     * Update the scoreboard.
     *
     * @param winner index of the player receiving the points
     * @param loser  index of the player giving the points
     * @param score  number of points
     */
    void addScore(int winner, int loser, int score) {
        addedScores[loser] += score;
        currScores[winner] += score;
        if (currScores[winner] >= GOAL_SCORE) {
            goalReached = true;
        }
    }

    /**
     * Copies the current round scores to the previous round and clears the
     * point contributions for the current round.
     */
    void newRound() {
        System.arraycopy(currScores, 0, prevScores, 0, numPlayers);
        Arrays.fill(addedScores, 0);
    }

    /**
     * @return list of scores for the previous round
     */
    List<Integer> getPrevScores() {
        return new ArrayList<>(List.of(prevScores));
    }

    /**
     * @return list of point contributions for the current round
     */
    List<Integer> getAddedScores() {
        return new ArrayList<>(List.of(addedScores));
    }

    /**
     * @return list of scores for the current round
     */
    List<Integer> getCurrScores() {
        return new ArrayList<>(List.of(currScores));
    }

    /**
     * @return true if a player has reached {@code GOAL_SCORE}, and false
     * otherwise
     */
    boolean isGoalReached() {
        return goalReached;
    }
}
