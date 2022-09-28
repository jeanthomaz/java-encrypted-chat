import Entities.Crypto.GenerateKeys;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New client has connected");

                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);

                thread.start();
            }
        } catch (IOException e) {
            closeServer();
        }
    }
    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Path root = Paths.get("KeyPair/");
        if(!root.toFile().exists()) {
            GenerateKeys.main(args);
        }
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
