package uno;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class UnoClient {
    private static final Gson GSON = new Gson();

    private final Socket socket;
    private final BufferedReader userReader;
    private final BufferedReader serverReader;
    private final PrintWriter writer;
    private final String myName;

    private int id;
    private int numPlayers;
    private int maxNameLen;
    private String[] names;

    public UnoClient(String host, int port, String myName) {
        try {
            socket = new Socket(host, port);
            userReader = new BufferedReader(new InputStreamReader(System.in));
            serverReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.myName = myName;
    }

    public void start() {
        try {
            getId();
            nameHandshake();
            gameLoop();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getId() throws IOException {
        String line = serverReader.readLine();
        JsonObject idJson = GSON.fromJson(line, JsonObject.class);
        id = idJson.get("id").getAsInt();
    }

    private void sendConfirmation() {
        JsonObject confirmJson = new JsonObject();
        confirmJson.add("id", new JsonPrimitive(id));
        confirmJson.add("move", new JsonPrimitive("confirm"));
        writer.println(confirmJson);
    }

    private void nameHandshake() throws IOException {
        // send name
        JsonObject nameJson = new JsonObject();
        nameJson.add("id", new JsonPrimitive(id));
        nameJson.add("name", new JsonPrimitive(myName));
        writer.println(nameJson);
        // get name list
        String line = serverReader.readLine();
        JsonObject namesJson = GSON.fromJson(line, JsonObject.class);
        names =
            GSON.fromJson(namesJson.get("names").getAsString(), String[].class);
        numPlayers = names.length;
        maxNameLen = 0;
        for (int i = 0; i < numPlayers; i++) {
            if (i != id) {
                maxNameLen = Math.max(maxNameLen, names[i].length());
            }
        }
        sendConfirmation();
    }

    private void gameLoop() throws IOException {
        System.out.println("The game is starting.");
        while (true) {
            GameData data = getGameData();
            printGame(data);
            sendConfirmation();
            if (data.isGameOver()) {
                break;
            }
            if (data.state() != GameState.ROUND_OVER) {
                awaitStart();
                if ((id == data.activePlayer()) || data.canChallengeUno()) {
                    printMoves(data);
                    handleInput(data);
                }
            }
        }
    }

    private GameData getGameData() throws IOException {
        String line = serverReader.readLine();
        JsonObject json = GSON.fromJson(line, JsonObject.class);
        return GSON.fromJson(json.get("gameData").getAsString(),
            GameData.class);
    }

    private void printGame(GameData data) {
        printPlayed(data);
        printSpecial(data);
        printDrawn(data);
        if (data.state() == GameState.ROUND_OVER) {
            printScores(data);
        } else if (id == data.activePlayer()) {
            printBoard(data);
        }
    }

    private void printPlayed(@NotNull GameData data) {
        GameMove lastMove = data.lastMove();
        Card topCard = data.topCard();
        int lastPlayed = data.lastPlayed();
        switch (lastMove) {
        case PLAY_CARD, DRAW_TWO, SKIP, REVERSE -> {
            if (id == lastPlayed) {
                System.out.println("You played a " + topCard + ".");
            } else if (lastPlayed != -1) {
                System.out.println(
                    names[lastPlayed] + " played a " + topCard + ".");
            }
            System.out.println();
        }
        }
    }

    private void printSpecial(@NotNull GameData data) {
        GameMove lastMove = data.lastMove();
        CardColor wildColor = data.wildColor();
        Direction direction = data.direction();
        int lastPlayed = data.lastPlayed();
        int lastAttacked = data.lastAttacked();
        switch (lastMove) {
        case SKIP -> {
            if (id == lastAttacked) {
                System.out.println("You were skipped.");
            } else {
                System.out.println(names[lastAttacked] + " was skipped.");
            }
        }
        case REVERSE -> System.out.println(
            "The play direction is now " + direction + ".");
        case CHANGE_COLOR -> System.out.println(
            "The color has been changed to " + wildColor + ".");
        case DRAW_FOUR_CHALLENGE_FAIL, DRAW_FOUR_CHALLENGE_SUCCESS -> {
            String lastAttackedName =
                (id == lastAttacked) ? "You" : names[lastAttacked];
            String lastPlayedName =
                (id == lastPlayed) ? "your" : names[lastPlayed] + "'s";
            System.out.print(lastAttackedName + " challenged " + lastPlayedName
                + " draw four. ");
            switch (lastMove) {
            case DRAW_FOUR_CHALLENGE_FAIL -> System.out.println(
                "Challenge failed!");
            case DRAW_FOUR_CHALLENGE_SUCCESS -> System.out.println(
                "Challenge successful!");
            }
        }
        case CALL_UNO -> {
            String lastPlayedName =
                (id == lastPlayed) ? "You" : names[lastPlayed];
            System.out.println(lastPlayedName + " called Uno.");
        }
        case CHALLENGE_UNO -> {
            String lastPlayedName =
                (id == lastPlayed) ? "You" : names[lastPlayed];
            String lastAttackedName =
                (id == lastPlayed) ? "you" : names[lastAttacked];
            System.out.println(
                lastPlayedName + " challenged " + lastAttackedName
                    + " for not calling Uno!");
        }
        default -> {
            return;
        }
        }
        System.out.println();
    }

    private void printDrawn(@NotNull GameData data) {
        GameMove lastMove = data.lastMove();
        int lastPlayed = data.lastPlayed();
        int lastAttacked = data.lastAttacked();
        Card[] lastDrawnCards = data.lastDrawnCards();
        switch (lastMove) {
        case DRAW_CARD, DRAW_TWO, DRAW_FOUR, DRAW_FOUR_CHALLENGE_FAIL,
            DRAW_FOUR_CHALLENGE_SUCCESS, CHALLENGE_UNO -> {
            int drew;
            switch (lastMove) {
            case DRAW_CARD, DRAW_FOUR_CHALLENGE_SUCCESS -> drew = lastPlayed;
            default -> drew = lastAttacked;
            }
            if (id == drew) {
                for (Card card : lastDrawnCards) {
                    System.out.println("You drew a " + card + ".");
                }
            } else {
                int numCards = lastDrawnCards.length;
                String plural = (numCards == 1) ? "" : "s";
                System.out.println(
                    names[drew] + " drew " + numCards + " card" + plural + ".");
            }
            System.out.println();
        }
        }
    }

    private void printBoard(@NotNull GameData data) {
        Card[][] hands = data.hands();
        for (int i = 1; i < numPlayers; i++) {
            int otherId = (id + i) % numPlayers;
            String name = names[otherId];
            int numCards = hands[otherId].length;
            String plural = (numCards == 1) ? "" : "s";
//            for (int j = name.length(); j < maxNameLen; j++) {
//                System.out.print(" ");
//            }
            System.out.println(name + ": " + numCards + " card" + plural);
        }
        System.out.println("Top card: " + data.topCard());
        System.out.println("Direction: " + data.direction());
        System.out.println("Your cards: " + Arrays.toString(hands[id]));
        System.out.println();
    }

    private void printScores(@NotNull GameData data) {
        String winner = names[data.lastPlayed()];
        int[][] scores = data.scores();
        if (data.isGameOver()) {
            System.out.println(winner + " wins the game!");
        } else {
            System.out.println(winner + " wins this round.");
        }
        for (int i = 0; i < maxNameLen; i++) {
            System.out.print(" ");
        }
        System.out.println("     Prev Contrib   Added    Curr");
        for (int i = 0; i < numPlayers; i++) {
            String name = names[i];
            for (int j = name.length(); j < maxNameLen; j++) {
                System.out.print(" ");
            }
            System.out.printf("%s:%8d%8d%8d%8d\n", name, scores[i][0],
                scores[i][1], scores[i][2], scores[i][3]);
        }
        System.out.println();
        if (!data.isGameOver()) {
            System.out.println("The next round is starting.");
            System.out.println();
        }
    }

    private void awaitStart() throws IOException {
        String line;
        JsonObject json;
        do {
            line = serverReader.readLine();
            json = GSON.fromJson(line, JsonObject.class);
        } while (!json.get("type").getAsString().equals("start"));
    }

    private void printMoves(@NotNull GameData data) {
        GameState state = data.state();
        boolean canCallUno = data.canCallUno();
        boolean canChallengeUno = data.canChallengeUno();
        int activePlayer = data.activePlayer();
        int lastPlayed = data.lastPlayed();
        Card[] playableCards = data.playableCards();
        if (id == activePlayer) {
            switch (state) {
            case PLAY_CARD -> {
                for (int i = 0; i < playableCards.length; i++) {
                    System.out.printf("%3d - Play %s.\n", i + 1,
                        playableCards[i]);
                }
                System.out.println("  d - Draw a card.");
            }
            case PLAY_DRAWN_CARD -> {
                Card lastDrawn = data.lastDrawnCards()[0];
                System.out.println("  p - Play " + lastDrawn + ".");
                System.out.println("  k - Keep " + lastDrawn + ".");
            }
            case CHANGE_COLOR -> {
                System.out.println("Choose a new color:");
                System.out.println("  b - Blue");
                System.out.println("  g - Green");
                System.out.println("  y - Yellow");
                System.out.println("  r - Red");
            }
            case CHALLENGE_DRAW_FOUR -> {
                System.out.println("Challenge draw four?");
                System.out.println("  y - Yes");
                System.out.println("  n - No");
            }
            }
            if (canCallUno) {
                System.out.println("  u - Call Uno.");
            }
        }
        if (canChallengeUno) {
            if (id == lastPlayed) {
                System.out.println("  u - Call Uno (late).");
            } else {
                System.out.println("  c - Challenge Uno.");
            }
        }
        System.out.println();
    }

    private void handleInput(@NotNull GameData data) throws IOException {
        GameState state = data.state();
        boolean canCallUno = data.canCallUno();
        boolean canChallengeUno = data.canChallengeUno();
        int activePlayer = data.activePlayer();
        int lastPlayed = data.lastPlayed();
        Card[] playableCards = data.playableCards();
        JsonObject moveJson = new JsonObject();
        moveJson.add("id", new JsonPrimitive(id));
        inputLoop:
        while (true) {
            while (!userReader.ready() && !serverReader.ready()) {
                Thread.onSpinWait();
            }
            if (serverReader.ready()) {
                return;
            }
            String input = userReader.readLine();
            if (canCallUno && input.equals("u")) {
                moveJson.add("move", new JsonPrimitive("callUno"));
                break;
            }
            if (canChallengeUno) {
                if (id == lastPlayed) {
                    if (input.equals("u")) {
                        moveJson.add("move", new JsonPrimitive("callLateUno"));
                        break;
                    }
                } else {
                    if (input.equals("c")) {
                        moveJson.add("move", new JsonPrimitive("challengeUno"));
                        break;
                    }
                }
            }
            if (id == activePlayer) {
                switch (state) {
                case PLAY_CARD -> {
                    if (input.equals("d")) {
                        moveJson.add("move", new JsonPrimitive("drawCard"));
                        break inputLoop;
                    } else {
                        try {
                            int index = Integer.parseInt(input) - 1;
                            if (index >= 0 && index < playableCards.length) {
                                moveJson.add("move",
                                    new JsonPrimitive("playCard"));
                                moveJson.add("index", new JsonPrimitive(index));
                                break inputLoop;
                            }
                        } catch (NumberFormatException e) {
                            /* fall through */
                        }
                    }
                }
                case PLAY_DRAWN_CARD -> {
                    switch (input) {
                    case "p", "k" -> {
                        moveJson.add("move",
                            new JsonPrimitive("playDrawnCard"));
                        boolean play = input.equals("p");
                        moveJson.add("play", new JsonPrimitive(play));
                        break inputLoop;
                    }
                    }
                }
                case CHANGE_COLOR -> {
                    switch (input) {
                    case "b", "g", "r", "y" -> {
                        moveJson.add("move", new JsonPrimitive("changeColor"));
                        switch (input) {
                        case "b" -> moveJson.add("color",
                            new JsonPrimitive(GSON.toJson(CardColor.BLUE)));
                        case "g" -> moveJson.add("color",
                            new JsonPrimitive(GSON.toJson(CardColor.GREEN)));
                        case "r" -> moveJson.add("color",
                            new JsonPrimitive(GSON.toJson(CardColor.RED)));
                        case "y" -> moveJson.add("color",
                            new JsonPrimitive(GSON.toJson(CardColor.YELLOW)));
                        }
                        break inputLoop;
                    }
                    }
                }
                case CHALLENGE_DRAW_FOUR -> {
                    switch (input) {
                    case "y", "n" -> {
                        moveJson.add("move",
                            new JsonPrimitive("challengeDrawFour"));
                        boolean challenge = input.equals("y");
                        moveJson.add("challenge", new JsonPrimitive(challenge));
                        break inputLoop;
                    }
                    }
                }
                }
            }
        }
        writer.println(moveJson);
        System.out.println();
    }
}
