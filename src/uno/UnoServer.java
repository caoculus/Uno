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
    }

    public void start() {
        try {
            waitForConnections();
            sendIds();
            nameHandshake();
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
        JsonArray nameArray = GSON.toJsonTree(names).getAsJsonArray();
        nameListJson.add("nameList", nameArray);
        for (PrintWriter writer : writers) {
            writer.println(nameListJson);
        }
    }
}
