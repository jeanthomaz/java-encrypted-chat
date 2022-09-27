package Entities;

import Crypto.GenerateKeys;
import Services.ClientService;
import java.io.*;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String channel;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Client(Socket socket, String username, String channel) {
        GenerateKeys gk;
        try {
            gk = new GenerateKeys(1024);
            gk.createKeys();
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
            this.channel = channel;
            this.privateKey = gk.getPrivateKey();
            this.publicKey = gk.getPublicKey();
        } catch (Exception e) {
            ClientService.closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public String getChannel() {
        return channel;
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

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
