package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.util.Log;

import java.security.MessageDigest;
import java.security.SecureRandom;

import id.uniflo.uniedc.sdk.interfaces.ICrypto;

/**
 * Feitian Crypto Wrapper using Reflection
 */
public class FeitianCrypto implements ICrypto {
    
    private static final String TAG = "FeitianCrypto";
    private final Context context;
    private Object crypto;
    private boolean initialized = false;
    
    public FeitianCrypto(Context context) {
        this.context = context;
    }
    
    @Override
    public int init() {
        try {
            // Check if Feitian SDK is available
            if (!FeitianReflectionHelper.isFeitianSDKAvailable()) {
                Log.e(TAG, "Feitian SDK not available");
                return -1;
            }
            
            // Load crypto class
            Class<?> cryptoClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.CRYPTO);
            
            if (cryptoClass == null) {
                Log.e(TAG, "Crypto class not found");
                return -1;
            }
            
            // Get crypto instance
            crypto = FeitianReflectionHelper.invokeStaticMethod(
                cryptoClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            if (crypto == null) {
                Log.e(TAG, "Failed to get Crypto instance");
                return -1;
            }
            
            initialized = true;
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize crypto", e);
            return -1;
        }
    }
    
    @Override
    public byte[] encrypt(int algorithm, int mode, int keyIndex, byte[] iv, byte[] data) {
        if (!initialized || crypto == null || data == null) {
            return null;
        }
        
        try {
            // Prepare output buffer
            byte[] encrypted = new byte[data.length + 16]; // Add padding space
            int[] encLen = new int[1];
            
            // Feitian SDK uses simpler API - algorithm and mode are combined
            int feitianAlg = convertAlgorithm(algorithm, mode);
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto, "encrypt",
                new Class<?>[] { int.class, int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, feitianAlg, data, data.length, encrypted, encLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to encrypt data: " + ret);
                return null;
            }
            
            // Return encrypted data with correct length
            byte[] result = new byte[encLen[0]];
            System.arraycopy(encrypted, 0, result, 0, encLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt data", e);
            return null;
        }
    }
    
    @Override
    public byte[] decrypt(int algorithm, int mode, int keyIndex, byte[] iv, byte[] data) {
        if (!initialized || crypto == null || data == null) {
            return null;
        }
        
        try {
            // Prepare output buffer
            byte[] decrypted = new byte[data.length];
            int[] decLen = new int[1];
            
            // Feitian SDK uses simpler API - algorithm and mode are combined
            int feitianAlg = convertAlgorithm(algorithm, mode);
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto, "decrypt",
                new Class<?>[] { int.class, int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, feitianAlg, data, data.length, decrypted, decLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to decrypt data: " + ret);
                return null;
            }
            
            // Return decrypted data with correct length
            byte[] result = new byte[decLen[0]];
            System.arraycopy(decrypted, 0, result, 0, decLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt data", e);
            return null;
        }
    }
    
    @Override
    public byte[] hash(int algorithm, byte[] data) {
        if (data == null) {
            return null;
        }
        
        try {
            // Use Java's built-in hash algorithms
            String algName;
            switch (algorithm) {
                case HASH_SHA1:
                    algName = "SHA-1";
                    break;
                case HASH_SHA256:
                    algName = "SHA-256";
                    break;
                case HASH_SHA384:
                    algName = "SHA-384";
                    break;
                case HASH_SHA512:
                    algName = "SHA-512";
                    break;
                case HASH_MD5:
                    algName = "MD5";
                    break;
                default:
                    Log.e(TAG, "Unsupported hash algorithm: " + algorithm);
                    return null;
            }
            
            MessageDigest digest = MessageDigest.getInstance(algName);
            return digest.digest(data);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate hash", e);
            return null;
        }
    }
    
    @Override
    public byte[] calculateMac(int algorithm, int keyIndex, byte[] data) {
        if (!initialized || crypto == null || data == null) {
            return null;
        }
        
        try {
            // Prepare output buffer
            byte[] mac = new byte[8];
            int[] macLen = new int[1];
            
            // Convert algorithm
            int feitianAlg = convertMacAlgorithm(algorithm);
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto, "calcMac",
                new Class<?>[] { int.class, int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, feitianAlg, data, data.length, mac, macLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to calculate MAC: " + ret);
                return null;
            }
            
            // Return MAC with correct length
            byte[] result = new byte[macLen[0]];
            System.arraycopy(mac, 0, result, 0, macLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate MAC", e);
            return null;
        }
    }
    
    @Override
    public boolean verifyMac(int algorithm, int keyIndex, byte[] data, byte[] mac) {
        if (data == null || mac == null) {
            return false;
        }
        
        // Calculate MAC and compare
        byte[] calculatedMac = calculateMac(algorithm, keyIndex, data);
        if (calculatedMac == null || calculatedMac.length != mac.length) {
            return false;
        }
        
        // Constant-time comparison
        int result = 0;
        for (int i = 0; i < mac.length; i++) {
            result |= calculatedMac[i] ^ mac[i];
        }
        
        return result == 0;
    }
    
    @Override
    public byte[] getRandom(int length) {
        if (length <= 0) {
            return null;
        }
        
        try {
            // Use Java's SecureRandom for random number generation
            SecureRandom random = new SecureRandom();
            byte[] data = new byte[length];
            random.nextBytes(data);
            return data;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate random data", e);
            return null;
        }
    }
    
    @Override
    public byte[] rsaEncrypt(int keyIndex, byte[] data) {
        if (!initialized || crypto == null || data == null) {
            return null;
        }
        
        try {
            // Prepare output buffer
            byte[] encrypted = new byte[256]; // RSA 2048 output size
            int[] encLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto, "rsaEncrypt",
                new Class<?>[] { int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, data, data.length, encrypted, encLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to RSA encrypt: " + ret);
                return null;
            }
            
            // Return encrypted data with correct length
            byte[] result = new byte[encLen[0]];
            System.arraycopy(encrypted, 0, result, 0, encLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to RSA encrypt", e);
            return null;
        }
    }
    
    @Override
    public byte[] rsaDecrypt(int keyIndex, byte[] data) {
        if (!initialized || crypto == null || data == null) {
            return null;
        }
        
        try {
            // Prepare output buffer
            byte[] decrypted = new byte[256];
            int[] decLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto, "rsaDecrypt",
                new Class<?>[] { int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, data, data.length, decrypted, decLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to RSA decrypt: " + ret);
                return null;
            }
            
            // Return decrypted data with correct length
            byte[] result = new byte[decLen[0]];
            System.arraycopy(decrypted, 0, result, 0, decLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to RSA decrypt", e);
            return null;
        }
    }
    
    @Override
    public byte[] rsaSign(int keyIndex, int hashAlgorithm, byte[] data) {
        // Feitian SDK doesn't directly support RSA signing with hash
        // Would need to hash first, then use RSA private key operation
        Log.w(TAG, "RSA signing not directly supported");
        return null;
    }
    
    @Override
    public boolean rsaVerify(int keyIndex, int hashAlgorithm, byte[] data, byte[] signature) {
        // Feitian SDK doesn't directly support RSA verification
        Log.w(TAG, "RSA verification not directly supported");
        return false;
    }
    
    @Override
    public void release() {
        crypto = null;
        initialized = false;
    }
    
    // Helper method to convert algorithm and mode to Feitian format
    private int convertAlgorithm(int algorithm, int mode) {
        // Feitian SDK uses simple algorithm identifiers
        // 0 = DES/3DES ECB
        // 1 = DES/3DES CBC
        // 2 = AES ECB
        // 3 = AES CBC
        
        int base = 0;
        switch (algorithm) {
            case ALG_DES:
            case ALG_3DES:
                base = 0;
                break;
            case ALG_AES:
                base = 2;
                break;
            default:
                return 0;
        }
        
        if (mode == MODE_CBC) {
            base += 1;
        }
        
        return base;
    }
    
    // Helper method to convert MAC algorithm
    private int convertMacAlgorithm(int algorithm) {
        // Feitian SDK MAC algorithms
        // 0 = X9.19
        // 1 = ECB
        // 2 = CBC
        
        switch (algorithm) {
            case MAC_X919:
            case MAC_X9_19:
                return 0;
            case MAC_ECB:
                return 1;
            case MAC_CBC:
                return 2;
            default:
                return 0;
        }
    }
}