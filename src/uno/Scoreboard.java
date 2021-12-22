package uno;

import java.util.Arrays;
import java.util.List;

/**
 * Scoreboard for an Uno game.
 */
class Scoreboard {
    /*
     * Rep invariant:
     * - scores is not null and contains only non-negative values
     * - For each row in scores, the first two elements add to the third.
     */

    public final static int GOAL_SCORE = 500;

    /**
     * Number of players in this scoreboard.
     */
    private final int numPlayers;
    /**
     * Array for scores. Each row represents for a given player: the score in
     * the previous round, the added score in the current round, and the
     * score in the current round.
     */
    private final int[][] scores;
    /**
     * Represents whether a player's score has reached {@code GOAL_SCORE}.
     */
    private boolean isGameOver;

    /**
     * Create a new scoreboard.
     *
     * @param numPlayers number of players, positive
     */
    Scoreboard(int numPlayers) {
        assert numPlayers > 0 : "Invalid number of players";
        this.numPlayers = numPlayers;
        scores = new int[numPlayers][3];
        isGameOver = false;
    }

    /**
     * @return number of players for this scoreboard
     */
    int getNumPlayers() {
        return numPlayers;
    }

    /**
     * @return contents of the scoreboard. Each row represents for a given
     * player: the score in the previous round, the added score in the
     * current round, and the score in the current round.
     */
    List<int[]> getScores() {
        return List.of(scores);
    }

    /**
     * @return true if a player's score has reached {@code GOAL_SCORE} and
     * false otherwise
     */
    boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Reset the scoreboard.
     */
    void reset() {
        for (int[] row : scores) {
            Arrays.fill(row, 0);
        }
        isGameOver = false;
    }

}
