package uno;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class UnoClient {
    private static final Gson GSON = new Gson();

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final String name;

    private int id;
    private String[] names;

    public UnoClient(String host, int port, String name) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.name = name;
    }

    public void start() {
        try {
            getId();
            nameHandshake();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getId() throws IOException {
        String line = reader.readLine();
        JsonObject idJson = GSON.fromJson(line, JsonObject.class);
        System.out.println(idJson);
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
        nameJson.add("name", new JsonPrimitive(name));
        writer.println(nameJson);
        // get name list
        String line = reader.readLine();
        JsonObject nameListJson = GSON.fromJson(line, JsonObject.class);
        JsonArray nameArray = nameListJson.getAsJsonArray("nameArray");
        System.out.println(nameListJson);
        int numPlayers = nameArray.size();
        names = new String[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            names[i] = nameArray.get(i).getAsString();
        }
        sendConfirmation();
    }

    private void gameLoop() throws IOException {
        while (true) {
            String line = reader.readLine();
            JsonObject json = GSON.fromJson(line, JsonObject.class);
            System.out.println(json);
        }
    }

    private void handleGame(GameData data) {
        printGame(data);
    }

    private void printGame(GameData data) {
        printPlayed(data);
        printSpecial(data);
        printDrawn(data);
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
        case CHALLENGE_UNO -> {
            String lastPlayedName =
                (id == lastPlayed) ? "You" : names[lastPlayed];
            String lastAttackedName =
                (id == lastPlayed) ? "you" : names[lastAttacked];
            System.out.println(
                lastPlayedName + " challenged " + lastAttackedName
                    + " for not calling Uno!");
        }
        }
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
            case DRAW_CARD, DRAW_FOUR_CHALLENGE_FAIL -> drew = lastPlayed;
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
        }
        }
    }
}
