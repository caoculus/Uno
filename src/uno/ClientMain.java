package uno;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter host: ");
        String host = reader.readLine();
        System.out.print("Enter port: ");
        int port = Integer.parseInt(reader.readLine());
        System.out.print("Enter your name: ");
        String name = reader.readLine();
        UnoClient client = new UnoClient(host, port, name);
        client.start();
    }
}
