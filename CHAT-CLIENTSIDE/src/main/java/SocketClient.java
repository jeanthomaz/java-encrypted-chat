import Entities.Client;
import Services.ClientService;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {
    public static void main(String[] args) {
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter your UserName: ");
                String username = scanner.nextLine();
                System.out.println("Enter your channel: ");
                String channel = scanner.nextLine();
                System.out.println("Enter your host");
                String host = scanner.nextLine();

                Socket socket = new Socket(host, 1234);
                Client client = new Client(socket, username, channel);

                ClientService.listenForMessage(client.getBufferedReader(), client.getSocket(), client.getUsername(), client.getPrivateKey());
                ClientService.sendMessage(client.getBufferedWriter(), client.getSocket(), client.getUsername(), client.getChannel(), client.getPublicKey());
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
