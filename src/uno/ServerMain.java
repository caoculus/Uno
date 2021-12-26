package uno;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter number of players: ");
                String input = reader.readLine();
                int numPlayers = Integer.parseInt(input);
                UnoServer server = new UnoServer(9000, numPlayers);
                server.start();
                break;
            } catch (NumberFormatException e) {
                /* fall through */
            }
        }
    }
}
