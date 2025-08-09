package id.uniflo.uniedc.sdk.interfaces;

/**
 * DUKPT (Derived Unique Key Per Transaction) interface for POS SDK abstraction
 */
public interface IDukpt {
    
    // DUKPT key types
    int KEY_TYPE_PIN = 0;
    int KEY_TYPE_MAC = 1;
    int KEY_TYPE_DATA = 2;
    
    // DUKPT variants
    int VARIANT_2009 = 0;  // ANSI X9.24-1:2009
    int VARIANT_2017 = 1;  // ANSI X9.24-3:2017
    
    /**
     * Initialize DUKPT module
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Load IPEK (Initial PIN Encryption Key)
     * @param keyIndex Key index to store IPEK
     * @param ipek IPEK data (16 bytes for 2DES, 24 bytes for 3DES)
     * @param ksn Initial KSN (Key Serial Number)
     * @return 0 for success, negative for error
     */
    int loadIpek(int keyIndex, byte[] ipek, byte[] ksn);
    
    /**
     * Get current KSN
     * @param keyIndex Key index
     * @return Current KSN or null if failed
     */
    byte[] getCurrentKsn(int keyIndex);
    
    /**
     * Increase KSN
     * @param keyIndex Key index
     * @return New KSN or null if failed
     */
    byte[] increaseKsn(int keyIndex);
    
    /**
     * Encrypt PIN block using DUKPT
     * @param keyIndex Key index
     * @param pan Card PAN
     * @param pin PIN data
     * @return Encrypted PIN block or null if failed
     */
    byte[] encryptPin(int keyIndex, String pan, byte[] pin);
    
    /**
     * Calculate MAC using DUKPT
     * @param keyIndex Key index
     * @param data Data to calculate MAC
     * @return MAC value or null if failed
     */
    byte[] calculateMac(int keyIndex, byte[] data);
    
    /**
     * Encrypt data using DUKPT
     * @param keyIndex Key index
     * @param keyType Key type (PIN, MAC, or DATA)
     * @param data Data to encrypt
     * @return Encrypted data or null if failed
     */
    byte[] encryptData(int keyIndex, int keyType, byte[] data);
    
    /**
     * Decrypt data using DUKPT
     * @param keyIndex Key index
     * @param keyType Key type (PIN, MAC, or DATA)
     * @param data Data to decrypt
     * @return Decrypted data or null if failed
     */
    byte[] decryptData(int keyIndex, int keyType, byte[] data);
    
    /**
     * Set DUKPT variant
     * @param variant DUKPT variant (2009 or 2017)
     * @return 0 for success, negative for error
     */
    int setVariant(int variant);
    
    /**
     * Get DUKPT variant
     * @return Current variant or negative for error
     */
    int getVariant();
    
    /**
     * Check if KSN needs to be synchronized with host
     * @param keyIndex Key index
     * @return true if synchronization needed, false otherwise
     */
    boolean needsSync(int keyIndex);
    
    /**
     * Get transaction counter from KSN
     * @param ksn KSN data
     * @return Transaction counter or negative for error
     */
    int getTransactionCounter(byte[] ksn);
    
    /**
     * Clear DUKPT key
     * @param keyIndex Key index to clear
     * @return 0 for success, negative for error
     */
    int clearKey(int keyIndex);
    
    /**
     * Release DUKPT resources
     */
    void release();
}