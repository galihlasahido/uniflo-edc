package id.uniflo.uniedc.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class DatabaseKeyManager {
    
    private static final String KEYSTORE_ALIAS = "UnifloEDCDatabaseKey";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String PREFS_NAME = "secure_db_prefs";
    private static final String ENCRYPTED_KEY_PREF = "encrypted_db_key";
    private static final String KEY_IV_PREF = "db_key_iv";
    private static final int GCM_TAG_LENGTH = 128;
    
    private final Context context;
    private static DatabaseKeyManager instance;
    
    private DatabaseKeyManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized DatabaseKeyManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseKeyManager(context);
        }
        return instance;
    }
    
    /**
     * Get or generate the database encryption key
     */
    public String getDatabaseKey() {
        try {
            // Check if we already have a stored key
            String existingKey = getStoredKey();
            if (existingKey != null) {
                return existingKey;
            }
            
            // Generate new key
            return generateAndStoreKey();
        } catch (Exception e) {
            // Fallback to device-specific key if keystore fails
            return getFallbackKey();
        }
    }
    
    private String getStoredKey() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String encryptedKey = prefs.getString(ENCRYPTED_KEY_PREF, null);
            String ivString = prefs.getString(KEY_IV_PREF, null);
            
            if (encryptedKey == null || ivString == null) {
                return null;
            }
            
            // Decrypt the key using Android Keystore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
                keyStore.load(null);
                
                SecretKey secretKey = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
                if (secretKey == null) {
                    return null;
                }
                
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                byte[] iv = Base64.decode(ivString, Base64.NO_WRAP);
                GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
                
                byte[] encryptedData = Base64.decode(encryptedKey, Base64.NO_WRAP);
                byte[] decryptedData = cipher.doFinal(encryptedData);
                
                return new String(decryptedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private String generateAndStoreKey() {
        try {
            // Generate a random database key
            SecureRandom random = new SecureRandom();
            byte[] keyBytes = new byte[32]; // 256-bit key
            random.nextBytes(keyBytes);
            String dbKey = Base64.encodeToString(keyBytes, Base64.NO_WRAP);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Generate or get keystore key
                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
                keyStore.load(null);
                
                if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                    
                    KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setUserAuthenticationRequired(false)
                        .build();
                    
                    keyGenerator.init(keySpec);
                    keyGenerator.generateKey();
                }
                
                // Encrypt the database key
                SecretKey secretKey = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                
                byte[] iv = cipher.getIV();
                byte[] encryptedData = cipher.doFinal(dbKey.getBytes());
                
                // Store encrypted key and IV
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                    .putString(ENCRYPTED_KEY_PREF, Base64.encodeToString(encryptedData, Base64.NO_WRAP))
                    .putString(KEY_IV_PREF, Base64.encodeToString(iv, Base64.NO_WRAP))
                    .apply();
            } else {
                // For older devices, store in shared preferences (less secure)
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                    .putString(ENCRYPTED_KEY_PREF, dbKey)
                    .apply();
            }
            
            return dbKey;
        } catch (Exception e) {
            e.printStackTrace();
            return getFallbackKey();
        }
    }
    
    /**
     * Fallback key generation based on device-specific information
     */
    private String getFallbackKey() {
        String androidId = android.provider.Settings.Secure.getString(
            context.getContentResolver(),
            android.provider.Settings.Secure.ANDROID_ID
        );
        
        String packageName = context.getPackageName();
        String combined = androidId + packageName + "UnifloEDC2024";
        
        // Create a deterministic key from device info
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(combined.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            // Last resort - hardcoded key (least secure)
            return "DefaultUnifloEDCKey2024Secure!!";
        }
    }
    
    /**
     * Clear stored keys (for security reset)
     */
    public void clearKeys() {
        try {
            // Clear shared preferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            
            // Clear keystore entry
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
                keyStore.load(null);
                if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                    keyStore.deleteEntry(KEYSTORE_ALIAS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}