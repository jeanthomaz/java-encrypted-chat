import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClientHandler implements Runnable {

    public static Map<String, ArrayList<ClientHandler>> clientHandlers = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String keyAccess;
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            this.keyAccess = bufferedReader.readLine();

            putClientHandler(this.keyAccess, this);

            broadcastMessage("Server: " + this.clientUsername + " has entered the chat");
        }catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        clientHandlers.forEach((key, array) -> {
            String keyAccess = this.keyAccess;
            if(Objects.equals(key, keyAccess)) {
                for (ClientHandler clientHandler : array) {
                    try {
                        if(!clientHandler.clientUsername.equals(clientUsername)) {
                            clientHandler.bufferedWriter.write(messageToSend);
                            clientHandler.bufferedWriter.newLine();
                            clientHandler.bufferedWriter.flush();
                        }
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        });
    }
    public void putClientHandler(String keyAccess, ClientHandler clientHandler) {
        boolean keyAccessExists = clientHandlers.containsKey(keyAccess);

        if(!keyAccessExists) {
            ArrayList<ClientHandler> clientHandlerArray = new ArrayList<>();
            clientHandlerArray.add(clientHandler);
            clientHandlers.put(keyAccess, clientHandlerArray);
        }
        if(keyAccessExists) {
            ArrayList<ClientHandler> clientHandlerArray = clientHandlers.get(keyAccess);
            clientHandlerArray.add(clientHandler);
        }
    }
    public void removeClientHandler () {
        ArrayList<ClientHandler> clientHandlerArray = clientHandlers.get(this.keyAccess);
        clientHandlerArray.remove(this);
        broadcastMessage("SERVER: " + this.clientUsername + " has left the chat");
    }

    public void closeEverything (Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
