package uno;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
    private final Hand hand;

    private int id;
    private int numPlayers;
    private int[] cardCounts;
    private String[] nameList;
    private Direction direction;
    private Card topCard;

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

    private void nameHandshake() throws IOException {
        // send name
        JsonObject nameJson = new JsonObject();
        nameJson.add("id", new JsonPrimitive(id));
        nameJson.add("name", new JsonPrimitive(name));
        writer.println(nameJson);
        // get name list
        String line = reader.readLine();
        JsonObject nameListJson = GSON.fromJson(line, JsonObject.class);
        JsonArray nameArray = nameListJson.getAsJsonArray("nameList");
        System.out.println(nameListJson);
        numPlayers = nameArray.size();
        nameList = new String[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            nameList[i] = nameArray.get(i).getAsString();
        }
    }
}
