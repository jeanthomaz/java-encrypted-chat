package Services;

import Entities.AsymmetricCryptography;
import Entities.UtilsSystem;

import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
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
            while (socket.isConnected()) {
                try {
                    AsymmetricCryptography aC = new AsymmetricCryptography();
                    String messageFromGroupChatEncrypt;
                    String messageFromGroupChatDecrypt;
                    PublicKey publicKeySender;

                    messageFromGroupChatEncrypt = bufferedReader.readLine();
                    publicKeySender = publicSenderDecrypt(bufferedReader);
                    messageFromGroupChatDecrypt = aC.decryptText(messageFromGroupChatEncrypt, publicKeySender);

                    System.out.println(messageFromGroupChatDecrypt);
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public static void sendMessage(BufferedWriter bufferedWriter, Socket socket, String username, String channel, PublicKey publicKey, PrivateKey privateKey) {
        try {
            AsymmetricCryptography aC = new AsymmetricCryptography();
            username = aC.encryptTextWithPublicKey(username, aC.getPublicServer("ServerPublicKey/publicKey"));
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            channel = aC.encryptTextWithPublicKey(channel, aC.getPublicServer("ServerPublicKey/publicKey"));
            bufferedWriter.write(channel);
            bufferedWriter.newLine();
            publicKeySend(bufferedWriter, publicKey);
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();

                String encryptText = aC.encryptTextWithPrivateKey(messageToSend,privateKey);

                bufferedWriter.write(encryptText);
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
    public static PublicKey publicSenderDecrypt(BufferedReader bufferedReader) throws Exception {

        String decryptText;
        String encryptText;
        String publicKeyString = "";
        byte[] publicKey;

        int count = 0;
        AsymmetricCryptography aC = new AsymmetricCryptography();
        while (count < 18) {
            encryptText = bufferedReader.readLine();
            decryptText = aC.decryptText(encryptText, aC.getPublicServer("ServerPublicKey/publicKey"));

            decryptText = decryptText.substring(1, decryptText.length() - 1) + ',';

            publicKeyString += decryptText.replaceAll("\\s", "");

            count++;
        }

        String[] test = publicKeyString.split(",");

        publicKey = new byte[test.length];

        for (int i = 0; i < test.length; i++) {
            publicKey[i] = Byte.parseByte(test[i]);
        }

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);
    }
    public static void publicKeySend(BufferedWriter bufferedWriter, PublicKey publicKey) throws Exception {
        AsymmetricCryptography aC = new AsymmetricCryptography();
        PublicKey publicKeyServer = aC.getPublicServer("ServerPublicKey/publicKey");
        int count = 1;
        int parts = 18;
        int startIndex = 0;
        while (count <= parts){
            int lastIndex = count * 9;
            byte[] newArr = UtilsSystem.getSliceOfArray(publicKey.getEncoded(), startIndex,lastIndex);
            String encryptPublicKey = aC.encryptTextWithPublicKey(Arrays.toString(newArr), publicKeyServer);

            startIndex = lastIndex;
            bufferedWriter.write(encryptPublicKey);
            bufferedWriter.newLine();

            count++;
        }
    }
}
