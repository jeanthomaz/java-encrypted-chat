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
                System.out.println("Enter your password: ");
                String password = scanner.nextLine();

                Socket socket = new Socket("localhost", 1234);
                Client client = new Client(socket, username, password);

                ClientService.listenForMessage(client.getBufferedReader(), client.getSocket());
                ClientService.sendMessage(client.getBufferedWriter(), client.getSocket(), client.getUsername(), client.getKeyAccess());
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
