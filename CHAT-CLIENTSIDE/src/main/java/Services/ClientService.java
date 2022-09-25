package Services;

import Entities.AsymmetricCryptography;
import Entities.UtilsSystem;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class ClientService {
    public static Map<String, PublicKey> publicKeys = new HashMap<>();
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
    public static void listenForMessage (BufferedReader bufferedReader, Socket socket, PrivateKey privateKey, String username) {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    AsymmetricCryptography aC = new AsymmetricCryptography();
                    String typeData =  bufferedReader.readLine();
                    typeData = aC.decryptText(typeData, aC.getPublicServer("ServerPublicKey/publicKey"));

                    String usernameKey = bufferedReader.readLine();
                    usernameKey = aC.decryptText(usernameKey, aC.getPublicServer("ServerPublicKey/publicKey"));

                    if (typeData.equals("publicKey")) {
                        PublicKey publicKeySender = publicSenderDecrypt(bufferedReader);
                        if(!publicKeys.containsKey(usernameKey)) {
                            publicKeys.put(usernameKey, publicKeySender);
                        }
                    }

                    if(typeData.equals("message")) {
                        String usernameSender =  bufferedReader.readLine();
                        String message = bufferedReader.readLine();

                        if(username.equals(usernameKey)) {
                            usernameSender = aC.decryptText(usernameSender, aC.getPublicServer("ServerPublicKey/publicKey"));

                            String decryptMessage = aC.decryptText(message, privateKey);
                            System.out.println(usernameSender + ": " + decryptMessage);
                        }
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public static void sendMessage(BufferedWriter bufferedWriter, Socket socket, String username, String channel, PublicKey publicKey) {
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

                publicKeys.forEach((usernameKey, publicKeySender) -> {
                        try {
                            usernameKey = aC.encryptTextWithPublicKey(usernameKey,aC.getPublicServer("ServerPublicKey/publicKey"));
                            bufferedWriter.write(usernameKey);
                            bufferedWriter.newLine();
                            String encryptText = aC.encryptTextWithPublicKey(messageToSend,publicKeySender);
                            bufferedWriter.write(encryptText);
                            bufferedWriter.newLine();

                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchPaddingException e) {
                            throw new RuntimeException(e);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalBlockSizeException e) {
                            throw new RuntimeException(e);
                        } catch (BadPaddingException e) {
                            throw new RuntimeException(e);
                        } catch (InvalidKeyException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                });

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
