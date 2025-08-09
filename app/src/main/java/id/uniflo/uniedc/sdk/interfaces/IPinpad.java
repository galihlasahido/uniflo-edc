package id.uniflo.uniedc.sdk.interfaces;

/**
 * Pinpad interface for POS SDK abstraction
 */
public interface IPinpad {
    
    /**
     * Initialize pinpad
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Show PIN input screen
     * @param pan Card PAN for PIN encryption
     * @param pinLength Expected PIN length (0 for variable length)
     * @param timeout Timeout in seconds
     * @param listener PIN input listener
     * @return 0 for success, negative for error
     */
    int inputPin(String pan, int pinLength, int timeout, IPinInputListener listener);
    
    /**
     * Load master key
     * @param keyIndex Key index
     * @param keyData Key data
     * @return 0 for success, negative for error
     */
    int loadMasterKey(int keyIndex, byte[] keyData);
    
    /**
     * Load work key
     * @param masterKeyIndex Master key index
     * @param workKeyIndex Work key index
     * @param keyData Encrypted work key data
     * @return 0 for success, negative for error
     */
    int loadWorkKey(int masterKeyIndex, int workKeyIndex, byte[] keyData);
    
    /**
     * Calculate MAC
     * @param data Data to calculate MAC
     * @param keyIndex Key index
     * @return MAC value or null if failed
     */
    byte[] calculateMac(byte[] data, int keyIndex);
    
    /**
     * Encrypt data
     * @param data Data to encrypt
     * @param keyIndex Key index
     * @return Encrypted data or null if failed
     */
    byte[] encryptData(byte[] data, int keyIndex);
    
    /**
     * Decrypt data
     * @param data Data to decrypt
     * @param keyIndex Key index
     * @return Decrypted data or null if failed
     */
    byte[] decryptData(byte[] data, int keyIndex);
    
    /**
     * Cancel PIN input
     * @return 0 for success, negative for error
     */
    int cancelInput();
    
    /**
     * Release pinpad resources
     */
    void release();
    
    /**
     * PIN input listener interface
     */
    interface IPinInputListener {
        /**
         * Called when PIN input is completed
         * @param pinBlock Encrypted PIN block
         */
        void onPinEntered(byte[] pinBlock);
        
        /**
         * Called when PIN input is cancelled
         */
        void onCancelled();
        
        /**
         * Called on timeout
         */
        void onTimeout();
        
        /**
         * Called on error
         * @param errorCode Error code
         * @param message Error message
         */
        void onError(int errorCode, String message);
    }
}