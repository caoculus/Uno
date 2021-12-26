package uno;

public class ServerMain {
    public static void main(String[] args) {
        UnoServer server = new UnoServer(9000, 4);
        server.start();
    }
}
