package uno;

/**
 * Enum for the type of card.
 */
public enum CardType {
    ZERO("0"),
    ONE_0("1"),
    ONE_1("1"),
    TWO_0("2"),
    TWO_1("2"),
    THREE_0("3"),
    THREE_1("3"),
    FOUR_0("4"),
    FOUR_1("4"),
    FIVE_0("5"),
    FIVE_1("5"),
    SIX_0("6"),
    SIX_1("6"),
    SEVEN_0("7"),
    SEVEN_1("7"),
    EIGHT_0("8"),
    EIGHT_1("8"),
    NINE_0("9"),
    NINE_1("9"),
    REVERSE_0("Reverse"),
    REVERSE_1("Reverse"),
    SKIP_0("Skip"),
    SKIP_1("Skip"),
    DRAW_TWO_0("Draw Two"),
    DRAW_TWO_1("Draw Two"),
    WILD_0("Wild"),
    WILD_1("Wild"),
    WILD_2("Wild"),
    WILD_3("Wild"),
    WILD_DRAW_FOUR_0("Wild Draw Four"),
    WILD_DRAW_FOUR_1("Wild Draw Four"),
    WILD_DRAW_FOUR_2("Wild Draw Four"),
    WILD_DRAW_FOUR_3("Wild Draw Four");

    private final String string;

    CardType(String string) {
        this.string = string;
    }


    @Override
    public String toString() {
        return string;
    }
}
