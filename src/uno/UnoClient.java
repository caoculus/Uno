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
import java.util.Arrays;

public class UnoClient {
    private static final Gson GSON = new Gson();

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final String name;
    private final Hand hand;

    private int id;
    private int numPlayers;
    private int[] cardCounts;
    private String[] names;
    private Card topCard;
    private CardColor wildColor;

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
        hand = new Hand();
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
        numPlayers = nameArray.size();
        names = new String[numPlayers];
        cardCounts = new int[numPlayers];
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
            String type = json.get("type").getAsString();
            switch (type) {
            case "dealCards" -> getStartCards(json);
            case "roundStart" -> getRoundStart(json);
            }
        }
    }

    private void getStartCards(@NotNull JsonObject json) {
        JsonArray cardArray = json.get("cardArray").getAsJsonArray();
        for (int i = 0; i < cardArray.size(); i++) {
            Card card = GSON.fromJson(cardArray.get(i), Card.class);
            hand.add(card);
        }
        Arrays.fill(cardCounts, Game.INITIAL_HAND_SIZE);
        sendConfirmation();
    }

    private void getRoundStart(@NotNull JsonObject json) {
        topCard = GSON.fromJson(json.get("topCard"), Card.class);
        switch (topCard.type()) {
        case REVERSE -> reverse(json);
        case SKIP -> skip(json);
        case DRAW_TWO -> draw(json);
        }
        sendConfirmation();
    }

    private void reverse(@NotNull JsonObject json) {
        Direction direction =
            GSON.fromJson(json.get("direction"), Direction.class);
        System.out.println("The direction of play is now " + direction + ".");
    }

    private void skip(@NotNull JsonObject json) {
        int skipped = json.get("skipped").getAsInt();
        String skippedName = names[skipped];
        System.out.println(skippedName + " was skipped.");
    }

    private void draw(@NotNull JsonObject json) {
        int drew = json.get("drew").getAsInt();
        Card[] drawnCards = GSON.fromJson(json.get("drawnCards"), Card[].class);
        if (id == drew) {
            for (Card card : drawnCards) {
                System.out.println("You drew a " + card + ".");
            }
        } else {
            int numCards = drawnCards.length;
            String drewName = names[drew];
            if (numCards == 1) {
                System.out.println(drewName + " drew a card.");
            } else {
                System.out.println(
                    drewName + " drew " + numCards + " cards.");
            }
        }
    }

    private void handlePlayCard() {

    }
}
