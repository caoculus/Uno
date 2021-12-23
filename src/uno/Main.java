package uno;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws IOException {
        int numPlayers = 4;
        Game game = new Game(numPlayers);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));
        while (!game.isGameOver()) {
            game.startRound();
            while (game.getState() != GameState.ROUND_OVER) {
                printMoves(game);
                handleInput(game, reader);
            }
        }
    }

    private static void printMoves(Game game) {
        int activePlayer = game.getActivePlayer();
        List<Card> lastDrawnCards = game.getLastDrawnCards();
        List<Card> playableCards = game.getPlayableCards();
        GameState state = game.getState();
        switch (state) {
        case PLAY_CARD -> {
            printGame(game);
            System.out.println(
                "Player " + activePlayer + " can play: " + playableCards);
            System.out.println("d to draw a card.");
            if (game.canCallUno()) {
                System.out.println("u to call Uno.");
            }
            if (game.canChallengeUno()) {
                System.out.println("c to challenge Uno.");
                System.out.println("l to call late Uno.");
            }
        }
        case PLAY_DRAWN_CARD -> {
            System.out.println("Play " + lastDrawnCards.get(0) + "? (y/n)");
            if (game.canCallUno()) {
                System.out.println("u to call Uno.");
            }
        }
        case CHANGE_COLOR -> System.out.println("Change color: b/g/r/y");
        case CHALLENGE_DRAW_FOUR -> System.out.println(
            "Player " + activePlayer + " : challenge draw four? (y/n)");
        }
    }

    private static void printGame(Game game) {
        int lastPlayed = game.getLastPlayed();
        int lastAttacked = game.getLastAttacked();
        int numPlayers = game.getNumPlayers();
        GameMove lastMove = game.getLastMove();
        Card topCard = game.getTopCard();
        List<Card> lastDrawnCards = game.getLastDrawnCards();
        Direction direction = game.getDirection();
        CardColor activeColor = game.getActiveColor();
        switch (lastMove) {
        case PLAY_CARD, DRAW_TWO, SKIP, REVERSE, CHANGE_COLOR, DRAW_FOUR,
            DRAW_FOUR_CHALLENGE_FAIL, DRAW_FOUR_CHALLENGE_SUCCESS -> {
            if (lastPlayed != -1) {
                System.out.println(
                    "Player " + lastPlayed + " played a " + topCard + ".");
            }
        }
        }
        switch (lastMove) {
        case CHANGE_COLOR, DRAW_FOUR, DRAW_FOUR_CHALLENGE_FAIL,
            DRAW_FOUR_CHALLENGE_SUCCESS -> System.out.println(
            "The active color is now " + activeColor + ".");
        }
        switch (lastMove) {
        case DRAW_FOUR_CHALLENGE_FAIL -> System.out.println(
            "Player " + lastAttacked + " challenged. Challenge failed!");
        case DRAW_FOUR_CHALLENGE_SUCCESS -> System.out.println(
            "Player " + lastAttacked + " challenged. Challenge successful!");
        case CALL_UNO -> System.out.println(
            "Player " + lastPlayed + "called Uno.");
        case CHALLENGE_UNO -> System.out.println(
            "Player " + lastPlayed + " caught player" + lastAttacked + "! ");
        }
        switch (lastMove) {
        case DRAW_CARD, DRAW_FOUR_CHALLENGE_SUCCESS -> System.out.println(
            "Player " + lastPlayed + " drew: " + lastDrawnCards + ".");
        case DRAW_TWO, DRAW_FOUR, DRAW_FOUR_CHALLENGE_FAIL, CHALLENGE_UNO -> System.out.println(
            "Player " + lastAttacked + " drew: " + lastDrawnCards + ".");
        case SKIP -> System.out.println(
            "Player " + lastAttacked + " was skipped.");
        case REVERSE -> System.out.println(
            "The play direction is now " + direction + ".");
        }
        for (int i = 0; i < numPlayers; i++) {
            System.out.println("Player " + i + ": " + game.getHand(i));
        }
        if (topCard.type().isWild()) {
            System.out.println(
                "The top card is: " + topCard + " (" + activeColor + ").");
        } else {
            System.out.println("The top card is: " + topCard + ".");
        }
    }

    private static void handleInput(Game game, BufferedReader reader)
        throws IOException {
        boolean done = false;
        GameState state = game.getState();
        while (!done) {
            String input = reader.readLine().toLowerCase(Locale.ROOT);
            done = true;
            switch (state) {
            case PLAY_CARD -> {
                switch (input) {
                case "d" -> game.drawCard();
                case "u" -> {
                    if (game.canCallUno()) {
                        game.callUno();
                    } else {
                        done = false;
                    }
                }
                case "c" -> {
                    if (game.canChallengeUno()) {
                        game.challengeUno();
                    } else {
                        done = false;
                    }
                }
                case "l" -> {
                    if (game.canChallengeUno()) {
                        game.callLateUno();
                    } else {
                        done = false;
                    }
                }
                default -> {
                    try {
                        int choice = Integer.parseInt(input) - 1;
                        if (!game.playCard(choice)) {
                            done = false;
                        }
                    } catch (NumberFormatException e) {
                        done = false;
                    }
                }
                }
            }
            case PLAY_DRAWN_CARD -> {
                switch (input) {
                case "n" -> game.playDrawnCard(false);
                case "y" -> game.playDrawnCard(true);
                default -> done = false;
                }
            }
            case CHANGE_COLOR -> {
                switch (input) {
                case "b" -> game.changeColor(CardColor.BLUE);
                case "g" -> game.changeColor(CardColor.GREEN);
                case "r" -> game.changeColor(CardColor.RED);
                case "y" -> game.changeColor(CardColor.YELLOW);
                default -> done = false;
                }
            }
            case CHALLENGE_DRAW_FOUR -> {
                switch (input) {
                case "n" -> game.challengeDrawFour(false);
                case "y" -> game.challengeDrawFour(true);
                default -> done = false;
                }
            }
            }
        }
    }
}