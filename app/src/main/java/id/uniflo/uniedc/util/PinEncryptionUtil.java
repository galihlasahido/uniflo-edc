package id.uniflo.uniedc.util;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PinEncryptionUtil {
    
    public static String encryptPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return pin; // Fallback to plain text (not recommended for production)
        }
    }
    
    public static boolean verifyPin(String inputPin, String encryptedPin) {
        String encryptedInput = encryptPin(inputPin);
        return encryptedInput.equals(encryptedPin);
    }
}