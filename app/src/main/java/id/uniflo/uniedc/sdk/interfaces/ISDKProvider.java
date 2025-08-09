package id.uniflo.uniedc.sdk.interfaces;

import android.content.Context;

/**
 * SDK Provider interface - each SDK implementation must implement this
 */
public interface ISDKProvider {
    
    /**
     * Initialize the SDK
     * @param context Application context
     * @param callback Initialization callback
     */
    void initialize(Context context, IInitCallback callback);
    
    /**
     * Check if SDK is initialized
     * @return true if initialized, false otherwise
     */
    boolean isInitialized();
    
    /**
     * Get printer instance
     * @return Printer interface or null if not supported
     */
    IPrinter getPrinter();
    
    /**
     * Get card reader instance
     * @return Card reader interface or null if not supported
     */
    ICardReader getCardReader();
    
    /**
     * Get pinpad instance
     * @return Pinpad interface or null if not supported
     */
    IPinpad getPinpad();
    
    /**
     * Get device instance
     * @return Device interface or null if not supported
     */
    IDevice getDevice();
    
    /**
     * Get LED instance
     * @return LED interface or null if not supported
     */
    ILed getLed();
    
    /**
     * Get buzzer instance
     * @return Buzzer interface or null if not supported
     */
    IBuzzer getBuzzer();
    
    /**
     * Get PSAM reader instance
     * @return PSAM interface or null if not supported
     */
    IPsam getPsam();
    
    /**
     * Get crypto instance
     * @return Crypto interface or null if not supported
     */
    ICrypto getCrypto();
    
    /**
     * Get DUKPT instance
     * @return DUKPT interface or null if not supported
     */
    IDukpt getDukpt();
    
    /**
     * Get key manager instance
     * @return Key manager interface or null if not supported
     */
    IKeyManager getKeyManager();
    
    /**
     * Get serial port instance
     * @return Serial port interface or null if not supported
     */
    ISerialPort getSerialPort();
    
    /**
     * Get Bluetooth screen instance
     * @return Bluetooth screen interface or null if not supported
     */
    IBtScreen getBtScreen();
    
    /**
     * Get SDK name
     * @return SDK name
     */
    String getSDKName();
    
    /**
     * Get SDK version
     * @return SDK version
     */
    String getSDKVersion();
    
    /**
     * Release all SDK resources
     */
    void release();
    
    /**
     * Initialization callback interface
     */
    interface IInitCallback {
        /**
         * Called when initialization succeeds
         */
        void onSuccess();
        
        /**
         * Called when initialization fails
         * @param errorCode Error code
         * @param message Error message
         */
        void onError(int errorCode, String message);
    }
}