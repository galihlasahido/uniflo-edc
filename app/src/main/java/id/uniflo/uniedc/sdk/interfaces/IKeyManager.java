package id.uniflo.uniedc.sdk.interfaces;

/**
 * Key Manager interface for POS SDK abstraction
 * Manages cryptographic keys in secure storage
 */
public interface IKeyManager {
    
    // Key types
    int KEY_TYPE_DES = 0;
    int KEY_TYPE_3DES = 1;
    int KEY_TYPE_AES = 2;
    int KEY_TYPE_RSA_PUBLIC = 3;
    int KEY_TYPE_RSA_PRIVATE = 4;
    int KEY_TYPE_SM4 = 5;
    
    // Key usage
    int KEY_USAGE_PIN = 0;
    int KEY_USAGE_MAC = 1;
    int KEY_USAGE_DATA = 2;
    int KEY_USAGE_KEK = 3;  // Key Encryption Key
    int KEY_USAGE_DUKPT = 4;
    
    // Key attributes
    int KEY_ATTR_ENCRYPT = 0x01;
    int KEY_ATTR_DECRYPT = 0x02;
    int KEY_ATTR_SIGN = 0x04;
    int KEY_ATTR_VERIFY = 0x08;
    int KEY_ATTR_DERIVE = 0x10;
    
    /**
     * Initialize key manager
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Set key group name
     * @param groupName Key group name (usually package name)
     * @return 0 for success, negative for error
     */
    int setKeyGroupName(String groupName);
    
    /**
     * Load plain text key (for development only)
     * @param keyIndex Key index
     * @param keyType Key type
     * @param keyData Key data
     * @return 0 for success, negative for error
     */
    int loadPlainKey(int keyIndex, int keyType, byte[] keyData);
    
    /**
     * Load encrypted key
     * @param keyIndex Key index to store
     * @param keyType Key type
     * @param encKeyIndex Index of key used to decrypt
     * @param encryptedKey Encrypted key data
     * @return 0 for success, negative for error
     */
    int loadEncryptedKey(int keyIndex, int keyType, int encKeyIndex, byte[] encryptedKey);
    
    /**
     * Load master key
     * @param keyIndex Key index
     * @param keyData Master key data
     * @return 0 for success, negative for error
     */
    int loadMasterKey(int keyIndex, byte[] keyData);
    
    /**
     * Load work key
     * @param masterKeyIndex Master key index
     * @param workKeyIndex Work key index
     * @param keyUsage Key usage type
     * @param encryptedKey Encrypted work key
     * @return 0 for success, negative for error
     */
    int loadWorkKey(int masterKeyIndex, int workKeyIndex, int keyUsage, byte[] encryptedKey);
    
    /**
     * Generate key pair (for RSA)
     * @param publicKeyIndex Index to store public key
     * @param privateKeyIndex Index to store private key
     * @param keySize Key size in bits (1024, 2048, etc.)
     * @return 0 for success, negative for error
     */
    int generateKeyPair(int publicKeyIndex, int privateKeyIndex, int keySize);
    
    /**
     * Export public key
     * @param keyIndex Key index
     * @return Public key data or null if failed
     */
    byte[] exportPublicKey(int keyIndex);
    
    /**
     * Check if key exists
     * @param keyIndex Key index
     * @return true if exists, false otherwise
     */
    boolean keyExists(int keyIndex);
    
    /**
     * Get key type
     * @param keyIndex Key index
     * @return Key type or negative for error
     */
    int getKeyType(int keyIndex);
    
    /**
     * Get key length
     * @param keyIndex Key index
     * @return Key length in bytes or negative for error
     */
    int getKeyLength(int keyIndex);
    
    /**
     * Delete key
     * @param keyIndex Key index
     * @return 0 for success, negative for error
     */
    int deleteKey(int keyIndex);
    
    /**
     * Delete all keys
     * @return 0 for success, negative for error
     */
    int deleteAllKeys();
    
    /**
     * Get key check value (KCV)
     * @param keyIndex Key index
     * @return KCV (usually first 3 bytes of encrypted zeros) or null if failed
     */
    byte[] getKcv(int keyIndex);
    
    /**
     * Set key attributes
     * @param keyIndex Key index
     * @param attributes Key attributes flags
     * @return 0 for success, negative for error
     */
    int setKeyAttributes(int keyIndex, int attributes);
    
    /**
     * Get maximum number of keys
     * @return Maximum key count
     */
    int getMaxKeyCount();
    
    /**
     * Release key manager resources
     */
    void release();
}