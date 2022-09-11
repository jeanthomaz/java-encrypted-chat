package Services;

import Entities.AsymmetricCryptography;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Scanner;

public class ClientService {
    public static void closeEverything (Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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
    public static void closeEverything (Socket socket, BufferedWriter bufferedWriter) {
        try {
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
    public static void closeEverything (Socket socket, BufferedReader bufferedReader) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void listenForMessage (BufferedReader bufferedReader, Socket socket) {
        new Thread(() -> {
            String messageFromGroupChat;

            while (socket.isConnected()) {
                try {
                    messageFromGroupChat = bufferedReader.readLine();
                    System.out.println(messageFromGroupChat);
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader);
                }
            }
        }).start();
    }
    public static void sendMessage(BufferedWriter bufferedWriter, Socket socket, String username, String channel, byte[] publicKey) {
        try {
            AsymmetricCryptography aC = new AsymmetricCryptography();
            PublicKey publicKeyServer = aC.getPublicServer("ServerPublicKey/publicKey");

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.write(channel);
            bufferedWriter.newLine();
            int count = 0;
            int startIndex = 0;
            int endIndex = 27;
            while (count < 6) {
                byte[] newArray = Arrays.copyOfRange(publicKey, startIndex, endIndex);
                String encrypted_publicKey = aC.encryptText(Arrays.toString(newArray), publicKeyServer);
                bufferedWriter.write(encrypted_publicKey);
                bufferedWriter.newLine();
                startIndex = endIndex;
                endIndex += 27;
                count++;
            }
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();

                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
