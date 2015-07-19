package transporter;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileDecryptor {
    public static byte[] decryptFile(Path filepath, String password) throws Exception {
        byte[] fileBytes = Files.readAllBytes(filepath);
        return decrypt(fileBytes, password);
    }

    public static byte[] decrypt(byte[] input, String password) throws Exception {
        if (input.length <= 72)
            throw new Exception("Invalid input length");

        byte[] esalt = Arrays.copyOfRange(input, 0, 20);
        byte[] hsalt = Arrays.copyOfRange(input, 20, 40);
        byte[] cipherText = Arrays.copyOfRange(input, 40, input.length - 32);
        byte[] hmac = Arrays.copyOfRange(input, input.length - 32, input.length);

        // Regenerate HMAC key using Recovered Salt (hsalt)
        byte[] hmacKey = KeyGenerator.deriveKey(password, hsalt, 100000, 160);

        // Perform HMAC using SHA-256
        SecretKeySpec hmacKeySpec = new SecretKeySpec(hmacKey, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(hmacKeySpec);
        byte[] computedHmac = mac.doFinal(cipherText);

        // Compare Computed HMAC vs Recovered HMAC
        if (!Arrays.equals(hmac, computedHmac)) {
            throw new Exception("Failed HMAC verification");
        }

        // HMAC Verification Passed
        // Regenerate Encryption Key using Recovered Salt (esalt)
        byte[] secretKey = KeyGenerator.deriveKey(password, esalt, 100000, 128);

        // Perform Decryption
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[16]));
        return cipher.doFinal(cipherText);
    }
}
