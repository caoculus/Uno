package uno;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter port: ");
        int port = Integer.parseInt(reader.readLine());
        System.out.print("Enter number of players: ");
        int numPlayers = Integer.parseInt(reader.readLine());
        UnoServer server = new UnoServer(port, numPlayers);
        server.start();
    }
}
