package id.uniflo.uniedc.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import id.uniflo.uniedc.sdk.emulator.EmulatorSDKProvider;
import id.uniflo.uniedc.sdk.feitian.FeitianSDKProvider;
import id.uniflo.uniedc.sdk.interfaces.*;

/**
 * SDK Manager - Main entry point for SDK operations
 * This class manages SDK selection and provides access to device features
 */
public class SDKManager {
    
    private static final String TAG = "SDKManager";
    private static final String PREF_NAME = "sdk_config";
    private static final String KEY_SDK_TYPE = "sdk_type";
    
    private static SDKManager instance;
    private Context context;
    private SDKType currentSDKType;
    private ISDKProvider currentProvider;
    
    private SDKManager() {
    }
    
    public static synchronized SDKManager getInstance() {
        if (instance == null) {
            instance = new SDKManager();
        }
        return instance;
    }
    
    /**
     * Initialize SDK Manager
     * @param context Application context
     */
    public void init(Context context) {
        this.context = context.getApplicationContext();
        
        // Load SDK type from configuration
        SharedPreferences prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String sdkTypeCode = prefs.getString(KEY_SDK_TYPE, detectDefaultSDKType());
        currentSDKType = SDKType.fromCode(sdkTypeCode);
        
        Log.d(TAG, "Initializing SDK Manager with type: " + currentSDKType.getDisplayName());
    }
    
    /**
     * Set SDK type
     * @param type SDK type to use
     */
    public void setSDKType(SDKType type) {
        if (currentSDKType != type) {
            // Release current provider
            if (currentProvider != null) {
                currentProvider.release();
                currentProvider = null;
            }
            
            currentSDKType = type;
            
            // Save to preferences
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_SDK_TYPE, type.getCode()).apply();
            
            Log.d(TAG, "SDK type changed to: " + type.getDisplayName());
        }
    }
    
    /**
     * Get current SDK type
     * @return Current SDK type
     */
    public SDKType getCurrentSDKType() {
        return currentSDKType;
    }
    
    /**
     * Initialize SDK provider
     * @param callback Initialization callback
     */
    public void initializeSDK(ISDKProvider.IInitCallback callback) {
        if (currentProvider != null && currentProvider.isInitialized()) {
            callback.onSuccess();
            return;
        }
        
        // Create provider based on SDK type
        switch (currentSDKType) {
            case FEITIAN:
                currentProvider = new FeitianSDKProvider();
                break;
            case PAX:
                // TODO: Implement PAX SDK provider
                currentProvider = new EmulatorSDKProvider();
                break;
            case VERIFONE:
                // TODO: Implement Verifone SDK provider
                currentProvider = new EmulatorSDKProvider();
                break;
            case EMULATOR:
            default:
                currentProvider = new EmulatorSDKProvider();
                break;
        }
        
        // Initialize the provider
        currentProvider.initialize(context, new ISDKProvider.IInitCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "SDK initialized successfully: " + currentProvider.getSDKName());
                callback.onSuccess();
            }
            
            @Override
            public void onError(int errorCode, String message) {
                Log.e(TAG, "SDK initialization failed: " + message);
                
                // Fallback to emulator if real SDK fails
                if (currentSDKType != SDKType.EMULATOR) {
                    Log.w(TAG, "Falling back to emulator SDK");
                    currentSDKType = SDKType.EMULATOR;
                    currentProvider = new EmulatorSDKProvider();
                    currentProvider.initialize(context, callback);
                } else {
                    callback.onError(errorCode, message);
                }
            }
        });
    }
    
    /**
     * Get printer instance
     * @return Printer interface or null
     */
    public IPrinter getPrinter() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getPrinter();
    }
    
    /**
     * Get card reader instance
     * @return Card reader interface or null
     */
    public ICardReader getCardReader() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getCardReader();
    }
    
    /**
     * Get pinpad instance
     * @return Pinpad interface or null
     */
    public IPinpad getPinpad() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getPinpad();
    }
    
    /**
     * Get device instance
     * @return Device interface or null
     */
    public IDevice getDevice() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getDevice();
    }
    
    /**
     * Get LED instance
     * @return LED interface or null
     */
    public ILed getLed() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getLed();
    }
    
    /**
     * Get buzzer instance
     * @return Buzzer interface or null
     */
    public IBuzzer getBuzzer() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getBuzzer();
    }
    
    /**
     * Get PSAM reader instance
     * @return PSAM interface or null
     */
    public IPsam getPsam() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getPsam();
    }
    
    /**
     * Get crypto instance
     * @return Crypto interface or null
     */
    public ICrypto getCrypto() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getCrypto();
    }
    
    /**
     * Get DUKPT instance
     * @return DUKPT interface or null
     */
    public IDukpt getDukpt() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getDukpt();
    }
    
    /**
     * Get key manager instance
     * @return Key manager interface or null
     */
    public IKeyManager getKeyManager() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getKeyManager();
    }
    
    /**
     * Get serial port instance
     * @return Serial port interface or null
     */
    public ISerialPort getSerialPort() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getSerialPort();
    }
    
    /**
     * Get Bluetooth screen instance
     * @return Bluetooth screen interface or null
     */
    public IBtScreen getBtScreen() {
        if (currentProvider == null || !currentProvider.isInitialized()) {
            Log.w(TAG, "SDK not initialized");
            return null;
        }
        return currentProvider.getBtScreen();
    }
    
    /**
     * Check if SDK is initialized
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return currentProvider != null && currentProvider.isInitialized();
    }
    
    /**
     * Get current SDK provider
     * @return Current SDK provider or null
     */
    public ISDKProvider getCurrentProvider() {
        return currentProvider;
    }
    
    /**
     * Release SDK resources
     */
    public void release() {
        if (currentProvider != null) {
            currentProvider.release();
            currentProvider = null;
        }
    }
    
    /**
     * Detect default SDK type based on device
     * @return SDK type code
     */
    private String detectDefaultSDKType() {
        // Check if running on emulator
        if (isEmulator()) {
            return SDKType.EMULATOR.getCode();
        }
        
        // Check device manufacturer
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (manufacturer.contains("feitian")) {
            return SDKType.FEITIAN.getCode();
        } else if (manufacturer.contains("pax")) {
            return SDKType.PAX.getCode();
        } else if (manufacturer.contains("verifone")) {
            return SDKType.VERIFONE.getCode();
        }
        
        // Default to emulator for safety
        return SDKType.EMULATOR.getCode();
    }
    
    /**
     * Check if running on emulator
     * @return true if emulator, false otherwise
     */
    private boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}