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
     * Point contributions from each player in the current round.
     */
    private final Integer[] contribScores;
    /**
     * Points added to winning player in the current round.
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
        contribScores = new Integer[numPlayers];
        addedScores = new Integer[numPlayers];
        currScores = new Integer[numPlayers];
        reset();
    }

    /**
     * Reset the scoreboard.
     */
    void reset() {
        Arrays.fill(prevScores, 0);
        Arrays.fill(contribScores, 0);
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
        contribScores[loser] += score;
        addedScores[winner] += score;
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
        Arrays.fill(contribScores, 0);
        Arrays.fill(addedScores, 0);
    }

    /**
     * @return contents of the scoreboard. The four entries of the list
     * represent: (1) scores for the previous round, (2) contributed points
     * for the current round, (3) added points for the current round, and (4)
     * scores for the current round.
     */
    List<List<Integer>> getScores() {
        List<List<Integer>> scores = new ArrayList<>();
        scores.add(new ArrayList<>(List.of(prevScores)));
        scores.add(new ArrayList<>(List.of(contribScores)));
        scores.add(new ArrayList<>(List.of(addedScores)));
        scores.add(new ArrayList<>(List.of(currScores)));
        return scores;
    }

    /**
     * @return true if a player has reached {@code GOAL_SCORE}, and false
     * otherwise
     */
    boolean isGoalReached() {
        return goalReached;
    }
}
