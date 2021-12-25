package uno;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class UnoServer {
    private static final Gson GSON = new Gson();

    private final int numPlayers;
    private final ServerSocket serverSocket;
    private final List<Socket> sockets;
    private final List<BufferedReader> readers;
    private final List<PrintWriter> writers;
    private final BlockingQueue<String> input;
    private final ExecutorService executor;
    private final AtomicBoolean errorFlag;
    private final Game game;

    public UnoServer(int port, int numPlayers) {
        if (numPlayers < Game.MIN_PLAYERS || numPlayers > Game.MAX_PLAYERS) {
            throw new IllegalArgumentException("Invalid number of players.");
        }
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.numPlayers = numPlayers;
        sockets = new ArrayList<>();
        readers = new ArrayList<>();
        writers = new ArrayList<>();
        input = new LinkedBlockingQueue<>();
        executor = Executors.newFixedThreadPool(numPlayers);
        errorFlag = new AtomicBoolean(false);
        game = new Game(numPlayers);
    }

    public void start() {
        try {
            waitForConnections();
            sendIds();
            nameHandshake();
            awaitConfirmation();
            gameLoop();
            executor.shutdown();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForConnections() throws IOException {
        for (int i = 0; i < numPlayers; i++) {
            sockets.add(serverSocket.accept());
        }
        Collections.shuffle(sockets);
        for (Socket socket : sockets) {
            readers.add(new BufferedReader(
                new InputStreamReader(socket.getInputStream())));
            writers.add(new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true));
        }
        for (BufferedReader reader : readers) {
            executor.submit(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        input.add(line);
                    }
                } catch (IOException e) {
                    errorFlag.set(true);
                }
            });
        }
    }

    private void sendIds() {
        for (int i = 0; i < numPlayers; i++) {
            PrintWriter writer = writers.get(i);
            JsonObject idJson = new JsonObject();
            idJson.add("id", new JsonPrimitive(i));
            writer.println(idJson);
        }
    }

    private void nameHandshake() throws InterruptedException {
        String[] names = new String[numPlayers];
        // get names
        for (int i = 0; i < numPlayers; i++) {
            JsonObject nameJson = GSON.fromJson(input.take(), JsonObject.class);
            System.out.println(nameJson);
            int id = nameJson.get("id").getAsInt();
            String name = nameJson.get("name").getAsString();
            names[id] = name;
        }
        // send name list
        JsonObject nameListJson = new JsonObject();
        nameListJson.add("nameArray", GSON.toJsonTree(names));
        for (PrintWriter writer : writers) {
            writer.println(nameListJson);
        }
    }

    private void gameLoop() throws InterruptedException {
        while (!game.isGameOver()) {
            game.startRound();
            while (game.getState() != GameState.ROUND_OVER) {
                sendGameData();
                awaitConfirmation();
                sendStart();
                awaitMove();
            }
            sendGameData();
            awaitConfirmation();
        }
    }

    private void awaitConfirmation() throws InterruptedException {
        for (int i = 0; i < numPlayers; i++) {
            while (true) {
                String line = input.take();
                JsonObject json = GSON.fromJson(line, JsonObject.class);
                if (json.get("move").getAsString().equals("confirm")) {
                    break;
                }
            }
        }
    }

    private void sendGameData() {
        JsonObject gameJson = new JsonObject();
        gameJson.add("type", new JsonPrimitive("game"));
        gameJson.add("gameData",
            new JsonPrimitive(GSON.toJson(new GameData(game))));
        for (PrintWriter writer : writers) {
            writer.println(gameJson);
        }
    }

    private void sendStart() {
        JsonObject startJson = new JsonObject();
        startJson.add("type", new JsonPrimitive("start"));
        for (PrintWriter writer : writers) {
            writer.println(startJson);
        }
    }

    private void awaitMove() {

    }
}
