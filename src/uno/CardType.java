package uno;

/**
 * Enum for the type of card.
 */
public enum CardType {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, REVERSE, SKIP,
    DRAW_TWO, WILD, WILD_DRAW_FOUR;

    @Override
    public String toString() {
        return switch (this) {
            case ZERO -> "0";
            case ONE -> "1";
            case TWO -> "2";
            case THREE -> "3";
            case FOUR -> "4";
            case FIVE -> "5";
            case SIX -> "6";
            case SEVEN -> "7";
            case EIGHT -> "8";
            case NINE -> "9";
            case SKIP -> "Skip";
            case REVERSE -> "Reverse";
            case DRAW_TWO -> "Draw Two";
            case WILD -> "Wild";
            case WILD_DRAW_FOUR -> "Wild Draw Four";
        };
    }
}
