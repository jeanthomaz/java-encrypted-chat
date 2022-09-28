package Crypto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;

public class AsymmetricCryptography {
    private Cipher cipher;
    public AsymmetricCryptography() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("RSA");
    }
    // https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
    public String encryptTextWithPublicKey(String msg, PublicKey key)
            throws UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encodeBase64String(cipher.doFinal(msg.getBytes("UTF-8")));
    }
    public String decryptText(String msg, PublicKey key)
            throws InvalidKeyException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.decodeBase64(msg)), "UTF-8");
    }
    public String decryptText(String msg, PrivateKey key)
            throws InvalidKeyException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.decodeBase64(msg)), "UTF-8");
    }
    public PublicKey getPublicServer() throws Exception {
        byte[] keyBytes = new byte[] {48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -102, -109, 116, -4, -72, 18, -121, -109, 37, 116, 84, -35, 15, -34, 76, -42, -58, -97, -100, 91, -70, 24, 100, 61, -68, -84, 119, 119, -83, 111, 36, 24, -79, -127, -118, -77, 126, 94, 123, 28, -24, -44, -36, -42, -12, 99, -2, 69, 3, -67, -56, 55, 35, 121, 7, -97, -36, 42, 87, -70, -61, -124, -50, -123, 41, 115, 92, 99, -52, 75, 19, 110, 75, -11, -107, 14, 51, -56, -119, -77, 84, 109, -107, 117, -2, -123, 53, 114, -79, 47, -19, -45, -14, -20, 45, -107, 124, 10, 108, 32, -24, 56, 42, 14, -30, -115, -78, 37, -76, -10, 40, 88, -82, 39, 14, 47, 94, 126, 103, 110, 112, 81, -101, -63, 108, 116, -68, 119, 2, 3, 1, 0, 1};
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
