package id.uniflo.uniedc.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import id.uniflo.uniedc.model.OAuthToken;

/**
 * Secure Token Manager using Android Keystore for encryption
 */
public class TokenManager {
    
    private static final String TAG = "TokenManager";
    private static final String PREFS_NAME = "secure_token_prefs";
    private static final String KEY_ALIAS = "UniEDC_TokenKey";
    private static final String KEY_TOKEN = "encrypted_oauth_token";
    private static final String KEY_IV = "token_iv";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    
    private static TokenManager instance;
    private Context context;
    private SharedPreferences prefs;
    private KeyStore keyStore;
    
    private TokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initKeyStore();
    }
    
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }
    
    private void initKeyStore() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            
            // Generate key if it doesn't exist
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                generateSecretKey();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing keystore", e);
        }
    }
    
    private void generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build();
            
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
            
            Log.d(TAG, "Secret key generated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error generating secret key", e);
        }
    }
    
    /**
     * Save OAuth token securely
     */
    public boolean saveToken(OAuthToken token) {
        if (token == null) {
            Log.e(TAG, "Cannot save null token");
            return false;
        }
        
        try {
            String tokenJson = token.toJson();
            String encryptedToken = encrypt(tokenJson);
            
            if (encryptedToken != null) {
                prefs.edit()
                    .putString(KEY_TOKEN, encryptedToken)
                    .apply();
                
                Log.d(TAG, "Token saved successfully");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving token", e);
        }
        
        return false;
    }
    
    /**
     * Retrieve OAuth token
     */
    public OAuthToken getToken() {
        try {
            String encryptedToken = prefs.getString(KEY_TOKEN, null);
            if (encryptedToken == null) {
                Log.d(TAG, "No token found");
                return null;
            }
            
            String decryptedToken = decrypt(encryptedToken);
            if (decryptedToken != null) {
                return OAuthToken.fromJson(decryptedToken);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving token", e);
        }
        
        return null;
    }
    
    /**
     * Check if valid token exists
     */
    public boolean hasValidToken() {
        OAuthToken token = getToken();
        return token != null && !token.isExpired();
    }
    
    /**
     * Clear stored token (logout)
     */
    public void clearToken() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_IV)
            .apply();
        Log.d(TAG, "Token cleared");
    }
    
    /**
     * Refresh token if needed
     */
    public boolean shouldRefreshToken() {
        OAuthToken token = getToken();
        if (token == null) {
            return false;
        }
        
        // Refresh if less than 10 minutes remaining
        return token.getRemainingTime() < 600;
    }
    
    /**
     * Encrypt string using Android Keystore
     */
    private String encrypt(String plainText) {
        try {
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            
            // Save IV for decryption
            prefs.edit()
                .putString(KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT))
                .apply();
            
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data", e);
            return null;
        }
    }
    
    /**
     * Decrypt string using Android Keystore
     */
    private String decrypt(String encryptedText) {
        try {
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            
            String ivString = prefs.getString(KEY_IV, null);
            if (ivString == null) {
                Log.e(TAG, "IV not found");
                return null;
            }
            
            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
            byte[] encrypted = Base64.decode(encryptedText, Base64.DEFAULT);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data", e);
            return null;
        }
    }
    
    /**
     * Get token info for debugging (without sensitive data)
     */
    public String getTokenInfo() {
        OAuthToken token = getToken();
        if (token == null) {
            return "No token stored";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Token exists: Yes\n");
        info.append("User ID: ").append(token.getUserId()).append("\n");
        info.append("User Role: ").append(token.getUserRole()).append("\n");
        info.append("Expired: ").append(token.isExpired()).append("\n");
        info.append("Remaining time: ").append(token.getRemainingTime()).append(" seconds\n");
        info.append("Created: ").append(token.getCreatedAt()).append("\n");
        
        return info.toString();
    }
}