package uno;

import java.util.Arrays;

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
    private final int[] prevScores;
    /**
     * Point contributions from each player in the current round.
     */
    private final int[] contribScores;
    /**
     * Points added to winning player in the current round.
     */
    private final int[] addedScores;
    /**
     * Scores for current round.
     */
    private final int[] currScores;
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
        prevScores = new int[numPlayers];
        contribScores = new int[numPlayers];
        addedScores = new int[numPlayers];
        currScores = new int[numPlayers];
        goalReached = false;
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
    int[][] getScores() {
        int[][] scores = new int[numPlayers][4];
        for (int i = 0; i < numPlayers; i++) {
            scores[i][0] = prevScores[i];
            scores[i][1] = contribScores[i];
            scores[i][2] = addedScores[i];
            scores[i][3] = currScores[i];
        }
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
