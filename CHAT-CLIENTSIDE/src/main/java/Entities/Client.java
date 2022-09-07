package Entities;

import Services.ClientService;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    private String keyAccess;

    public Client(Socket socket, String username, String keyAccess) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
            this.keyAccess = keyAccess;
        } catch (Exception e) {
            ClientService.closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public String getKeyAccess() {
        return keyAccess;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    public BufferedReader getBufferedReader() {
        return this.bufferedReader;
    }

    public BufferedWriter getBufferedWriter() {
        return this.bufferedWriter;
    }
}
