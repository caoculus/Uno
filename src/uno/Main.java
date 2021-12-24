package uno;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 9000;
        int numPlayers = 2;
        UnoServer server = new UnoServer(port, numPlayers);
        UnoClient client1 = new UnoClient(host, port, "1");
        UnoClient client2 = new UnoClient(host, port, "2");
        Thread t1 = new Thread(server::start);
        Thread t2 = new Thread(client1::start);
        Thread t3 = new Thread(client2::start);
        t1.start();
        t2.start();
        t3.start();
    }
}