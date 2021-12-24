package uno;

class PlayerCounter {
    private final int numPlayers;

    private int activePlayer;
    private Direction direction;

    /**
     * Create a new player counter.
     *
     * @param numPlayers number of players, 2 or greater
     */
    PlayerCounter(int numPlayers) {
        this.numPlayers = numPlayers;
        activePlayer = 0;
        direction = Direction.CW;
    }

    int getActivePlayer() {
        return activePlayer;
    }

    Direction getDirection() {
        return direction;
    }

    /**
     * Reset the play direction and set the active player to a specified player.
     *
     * @param player new active player
     */
    void reset(int player) {
        activePlayer = player;
        direction = Direction.CW;
    }

    /**
     * Get the nth next player.
     *
     * @param n n
     * @return the {@code n}th next player in the direction of play
     */
    int getNextPlayer(int n) {
        int step = (direction == Direction.CW) ? n : -n;
        return Math.floorMod(activePlayer + step, numPlayers);
    }

    /**
     * Advance the active player by n steps.
     *
     * @param n n
     */
    void advancePlayer(int n) {
        activePlayer = getNextPlayer(n);
    }

    void switchDirection() {
        direction = direction.opposite();
    }
}
