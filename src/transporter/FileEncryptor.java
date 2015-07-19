package transporter;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class FileEncryptor {
    public static byte[] encryptFile(Path filepath, String password) throws Exception {
        byte[] fileBytes = Files.readAllBytes(filepath);
        return encrypt(fileBytes, password);
    }

    public static byte[] encrypt(byte[] input, String password) throws Exception {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        // Generate 160 bit Salt for Encryption Key
        byte[] esalt = new byte[20];
        random.nextBytes(esalt);

        // Generate 128 bit Encryption Key
        byte[] secretKey = KeyGenerator.deriveKey(password, esalt, 100000, 128);

        // Perform Encryption
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[16]));
        byte[] cipherText = cipher.doFinal(input);

        // Generate 160 bit Salt for HMAC Key
        byte[] hsalt = new byte[20];
        random.nextBytes(hsalt);
        
        // Generate 160 bit HMAC Key
        byte[] hmacKey = KeyGenerator.deriveKey(password, hsalt, 100000, 160);

        // Perform HMAC using SHA-256
        SecretKeySpec hmacKeySpec = new SecretKeySpec(hmacKey, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(hmacKeySpec);
        byte[] hmac = mac.doFinal(cipherText);

        // Construct Output as "ESALT + HSALT + CIPHERTEXT + HMAC"
        byte[] output = new byte[40 + cipherText.length + 32];
        System.arraycopy(esalt, 0, output, 0, 20);
        System.arraycopy(hsalt, 0, output, 20, 20);
        System.arraycopy(cipherText, 0, output, 40, cipherText.length);
        System.arraycopy(hmac, 0, output, 40 + cipherText.length, 32);

        return output;
    }
}
