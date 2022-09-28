import Entities.Crypto.AsymmetricCryptography;
import Entities.UtilsSystem;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class ClientHandler implements Runnable {

    public static Map<String, ArrayList<ClientHandler>> clientHandlers = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String channel;
    private PublicKey publicKey;
    public ClientHandler(Socket socket) {
        try {
            AsymmetricCryptography aC = new AsymmetricCryptography();
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = aC.decryptText(bufferedReader.readLine(), aC.getPrivate());
            this.channel = aC.decryptText(bufferedReader.readLine(), aC.getPrivate());
            this.publicKey = publicClientDecrypt(bufferedReader);

            putClientHandler(this.channel, this);

            broadcastPublicKeyChannel();
        }catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String userNameKey;
        String messageFromClient;

        while(socket.isConnected()) {
            try {
                AsymmetricCryptography aC = new AsymmetricCryptography();
                userNameKey = bufferedReader.readLine();
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(aC.decryptText(userNameKey, aC.getPrivate()) , messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void broadcastPublicKeyChannel() {
        clientHandlers.forEach((key, array) -> {
            String channel = this.channel;
            if(Objects.equals(key, channel)) {
                for (ClientHandler clientHandler : array) {
                    array.forEach(( otherClientHandler ) -> {
                        try {
                            AsymmetricCryptography aC = new AsymmetricCryptography();
                            clientHandler.bufferedWriter.write(aC.encryptText("publicKey", aC.getPrivate()));
                            clientHandler.bufferedWriter.newLine();
                            clientHandler.bufferedWriter.write(aC.encryptText(otherClientHandler.clientUsername, aC.getPrivate()));
                            clientHandler.bufferedWriter.newLine();
                            publicKeySend(clientHandler.bufferedWriter, otherClientHandler.publicKey);
                            clientHandler.bufferedWriter.flush();
                        } catch (IOException e) {
                            closeEverything(socket, bufferedReader, bufferedWriter);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        });
    }
    public void broadcastMessage(String userNameKey, String messageToSend) {
        clientHandlers.forEach((key, array) -> {
            String channel = this.channel;
                if(Objects.equals(key, channel)) {
                        for (ClientHandler clientHandler : array) {
                            try {
                                if (!clientHandler.clientUsername.equals(clientUsername)) {
                                    AsymmetricCryptography aC = new AsymmetricCryptography();
                                    clientHandler.bufferedWriter.write(aC.encryptText("message", aC.getPrivate()));
                                    clientHandler.bufferedWriter.newLine();
                                    clientHandler.bufferedWriter.write(aC.encryptText(userNameKey, aC.getPrivate()));
                                    clientHandler.bufferedWriter.newLine();
                                    clientHandler.bufferedWriter.write(aC.encryptText(this.clientUsername, aC.getPrivate()));
                                    clientHandler.bufferedWriter.newLine();
                                    clientHandler.bufferedWriter.write(messageToSend);
                                    clientHandler.bufferedWriter.newLine();

                                    clientHandler.bufferedWriter.flush();
                                }
                            } catch (IOException e) {
                                closeEverything(socket, bufferedReader, bufferedWriter);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
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
        ArrayList<ClientHandler> clientHandlerArray = clientHandlers.get(this.channel);
        clientHandlerArray.remove(this);
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
    public PublicKey publicClientDecrypt(BufferedReader bufferedReader) throws Exception {

        String decryptText;
        String encryptText;
        StringBuilder publicKeyString = new StringBuilder();
        byte[] publicKey;

        int count = 0;
        AsymmetricCryptography aC = new AsymmetricCryptography();
        while (count < 18) {
            encryptText = bufferedReader.readLine();
            decryptText = aC.decryptText(encryptText, aC.getPrivate());

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
    public static void publicKeySend(BufferedWriter bufferedWriter, PublicKey publicKey) throws Exception {
        AsymmetricCryptography aC = new AsymmetricCryptography();
        PrivateKey privateKeyServer = aC.getPrivate();
        int count = 1;
        int parts = 18;
        int startIndex = 0;
        while (count <= parts){
            int lastIndex = count * 9;
            byte[] newArr = UtilsSystem.getSliceOfArray(publicKey.getEncoded(), startIndex,lastIndex);
            String encryptPublicKey = aC.encryptText(Arrays.toString(newArr), privateKeyServer);

            startIndex = lastIndex;
            bufferedWriter.write(encryptPublicKey);
            bufferedWriter.newLine();

            count++;
        }
    }
}
