package Services;

import Crypto.AsymmetricCryptography;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClientService {
    private static final Map<String, PublicKey> publicKeys = new HashMap<>();
    private static final AsymmetricCryptography aC;

    static {
        try {
            aC = new AsymmetricCryptography();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

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
    public static void listenForMessage (BufferedReader bufferedReader, Socket socket, String username, PrivateKey privateKeyClient) {
        new Thread(() -> {
            while (socket.isConnected()) {
                String typeData;
                String usernameKey;
                String usernameSender;
                String message;
                String decryptMessage;

                try {
                    AsymmetricCryptography aC = new AsymmetricCryptography();
                    typeData = bufferedReader.readLine();

                    typeData = aC.decryptText(typeData, aC.getPublicServer());

                    usernameKey = bufferedReader.readLine();
                    usernameKey = aC.decryptText(usernameKey, aC.getPublicServer());

                    if (typeData.equals("publicKey")) {
                        PublicKey publicKeySender = publicSenderDecrypt(bufferedReader);
                        if (!publicKeys.containsKey(usernameKey)) {
                            publicKeys.put(usernameKey, publicKeySender);
                        }
                    }

                    if (typeData.equals("message")) {
                        usernameSender = bufferedReader.readLine();
                        message = bufferedReader.readLine();

                        if (username.equals(usernameKey)) {
                            usernameSender = aC.decryptText(usernameSender, aC.getPublicServer());

                            decryptMessage = aC.decryptText(message, privateKeyClient);
                            System.out.println(usernameSender + ": " + decryptMessage);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static void sendMessage(BufferedWriter bufferedWriter, Socket socket, String username, String channel, PublicKey publicKeyClient) throws Exception {
        String usernameEncrypt = aC.encryptTextWithPublicKey(username, aC.getPublicServer());
        String channelEncrypt = aC.encryptTextWithPublicKey(channel, aC.getPublicServer());
        try {
            bufferedWriter.write(usernameEncrypt);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.write(channelEncrypt);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            publicKeySend(bufferedWriter, publicKeyClient);


            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();

                for (Map.Entry<String, PublicKey> entry : publicKeys.entrySet()) {
                    String usernameKey = entry.getKey();
                    PublicKey publicKeySender = entry.getValue();
                    try {
                        usernameKey = aC.encryptTextWithPublicKey(usernameKey, aC.getPublicServer());
                        bufferedWriter.write(usernameKey);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();

                        String encryptText = aC.encryptTextWithPublicKey(messageToSend, publicKeySender);
                        bufferedWriter.write(encryptText);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void publicKeySend(BufferedWriter bufferedWriter, PublicKey publicKey) throws Exception {
        AsymmetricCryptography aC = new AsymmetricCryptography();
        PublicKey publicKeyServer = aC.getPublicServer();
        int count = 1;
        int parts = 18;
        int startIndex = 0;
        while (count <= parts){
            int lastIndex = count * 9;
            byte[] newArr = Arrays.copyOfRange(publicKey.getEncoded(), startIndex,lastIndex);
            String encryptPublicKey = aC.encryptTextWithPublicKey(Arrays.toString(newArr), publicKeyServer);

            startIndex = lastIndex;
            bufferedWriter.write(encryptPublicKey);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            count++;
        }
    }
    public static PublicKey publicSenderDecrypt(BufferedReader bufferedReader) throws Exception {

        String decryptText;
        String encryptText;
        StringBuilder publicKeyString = new StringBuilder();
        byte[] publicKey;

        int count = 0;
        AsymmetricCryptography aC = new AsymmetricCryptography();
        while (count < 18) {
            encryptText = bufferedReader.readLine();
            decryptText = aC.decryptText(encryptText, aC.getPublicServer());

            decryptText = decryptText.substring(1, decryptText.length() - 1) + ',';

            publicKeyString.append(decryptText.replaceAll("\\s", ""));

            count++;
        }

        String[] test = publicKeyString.toString().split(",");

        publicKey = new byte[test.length];

        for (int i = 0; i < test.length; i++) {
            publicKey[i] = Byte.parseByte(test[i]);
        }

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);
    }
}
