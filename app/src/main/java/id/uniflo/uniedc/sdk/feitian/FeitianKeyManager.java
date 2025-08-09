package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.IKeyManager;

/**
 * Feitian Key Manager Wrapper using Reflection
 */
public class FeitianKeyManager implements IKeyManager {
    
    private static final String TAG = "FeitianKeyManager";
    private final Context context;
    private Object keyManager;
    private boolean initialized = false;
    
    public FeitianKeyManager(Context context) {
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
            
            // Load key manager class
            Class<?> keyManagerClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.KEY_MANAGER);
            
            if (keyManagerClass == null) {
                Log.e(TAG, "KeyManager class not found");
                return -1;
            }
            
            // Get key manager instance
            keyManager = FeitianReflectionHelper.invokeStaticMethod(
                keyManagerClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            if (keyManager == null) {
                Log.e(TAG, "Failed to get KeyManager instance");
                return -1;
            }
            
            initialized = true;
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize key manager", e);
            return -1;
        }
    }
    
    @Override
    public int setKeyGroupName(String groupName) {
        if (!initialized || keyManager == null || groupName == null) {
            return -1;
        }
        
        try {
            FeitianReflectionHelper.invokeMethod(
                keyManager, "setKeyGroupName",
                new Class<?>[] { String.class },
                new Object[] { groupName }
            );
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set key group name", e);
            return -1;
        }
    }
    
    @Override
    public int loadPlainKey(int keyIndex, int keyType, byte[] keyData) {
        if (!initialized || keyManager == null || keyData == null) {
            return -1;
        }
        
        try {
            // Feitian SDK loadMainKey for plain text key
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager, "downloadMainKey",
                new Class<?>[] { int.class, byte[].class, int.class, int.class },
                new Object[] { keyIndex, keyData, keyData.length, 0 }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to load plain key: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load plain key", e);
            return -1;
        }
    }
    
    @Override
    public int loadEncryptedKey(int keyIndex, int keyType, int encKeyIndex, byte[] encryptedKey) {
        if (!initialized || keyManager == null || encryptedKey == null) {
            return -1;
        }
        
        try {
            // Use downloadWorkKey for encrypted key
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager, "downloadWorkKey",
                new Class<?>[] { int.class, int.class, int.class, byte[].class, int.class },
                new Object[] { encKeyIndex, keyIndex, keyType, encryptedKey, encryptedKey.length }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to load encrypted key: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load encrypted key", e);
            return -1;
        }
    }
    
    @Override
    public int loadMasterKey(int keyIndex, byte[] keyData) {
        return loadPlainKey(keyIndex, KEY_TYPE_3DES, keyData);
    }
    
    @Override
    public int loadWorkKey(int masterKeyIndex, int workKeyIndex, int keyUsage, byte[] encryptedKey) {
        return loadEncryptedKey(workKeyIndex, keyUsage, masterKeyIndex, encryptedKey);
    }
    
    @Override
    public int generateKeyPair(int publicKeyIndex, int privateKeyIndex, int keySize) {
        if (!initialized || keyManager == null) {
            return -1;
        }
        
        try {
            // Feitian SDK RSA key generation
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager, "generateRsaKeyPair",
                new Class<?>[] { int.class, int.class, int.class },
                new Object[] { publicKeyIndex, privateKeyIndex, keySize }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to generate key pair: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate key pair", e);
            return -1;
        }
    }
    
    @Override
    public byte[] exportPublicKey(int keyIndex) {
        if (!initialized || keyManager == null) {
            return null;
        }
        
        try {
            byte[] publicKey = new byte[512]; // Max RSA key size
            int[] keyLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager, "exportRsaPublicKey",
                new Class<?>[] { int.class, byte[].class, int[].class },
                new Object[] { keyIndex, publicKey, keyLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to export public key: " + ret);
                return null;
            }
            
            // Return key with correct length
            byte[] result = new byte[keyLen[0]];
            System.arraycopy(publicKey, 0, result, 0, keyLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to export public key", e);
            return null;
        }
    }
    
    @Override
    public boolean keyExists(int keyIndex) {
        if (!initialized || keyManager == null) {
            return false;
        }
        
        try {
            // Try to get key check value
            byte[] kcv = getKcv(keyIndex);
            return kcv != null && kcv.length > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to check key existence", e);
            return false;
        }
    }
    
    @Override
    public int getKeyType(int keyIndex) {
        // Feitian SDK doesn't provide direct key type query
        Log.w(TAG, "Key type query not supported");
        return -1;
    }
    
    @Override
    public int getKeyLength(int keyIndex) {
        // Feitian SDK doesn't provide direct key length query
        Log.w(TAG, "Key length query not supported");
        return -1;
    }
    
    @Override
    public int deleteKey(int keyIndex) {
        if (!initialized || keyManager == null) {
            return -1;
        }
        
        try {
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager, "deleteKey",
                new Class<?>[] { int.class },
                new Object[] { keyIndex }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to delete key: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete key", e);
            return -1;
        }
    }
    
    @Override
    public int deleteAllKeys() {
        if (!initialized || keyManager == null) {
            return -1;
        }
        
        try {
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager, "deleteAllKeys",
                new Class<?>[0],
                new Object[0]
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to delete all keys: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete all keys", e);
            return -1;
        }
    }
    
    @Override
    public byte[] getKcv(int keyIndex) {
        if (!initialized || keyManager == null) {
            return null;
        }
        
        try {
            byte[] kcv = new byte[8];
            int[] kcvLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager, "getKcv",
                new Class<?>[] { int.class, byte[].class, int[].class },
                new Object[] { keyIndex, kcv, kcvLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to get KCV: " + ret);
                return null;
            }
            
            // Return KCV with correct length (usually 3 bytes)
            byte[] result = new byte[kcvLen[0]];
            System.arraycopy(kcv, 0, result, 0, kcvLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get KCV", e);
            return null;
        }
    }
    
    @Override
    public int setKeyAttributes(int keyIndex, int attributes) {
        // Feitian SDK doesn't support key attributes
        Log.w(TAG, "Key attributes not supported");
        return -1;
    }
    
    @Override
    public int getMaxKeyCount() {
        // Default max key count for Feitian
        return 100;
    }
    
    @Override
    public void release() {
        keyManager = null;
        initialized = false;
    }
}