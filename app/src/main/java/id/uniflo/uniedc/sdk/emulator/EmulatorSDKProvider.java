package id.uniflo.uniedc.sdk.emulator;

import android.content.Context;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.*;

/**
 * Emulator SDK Provider - Mock implementation for development
 */
public class EmulatorSDKProvider implements ISDKProvider {
    
    private static final String TAG = "EmulatorSDK";
    private boolean initialized = false;
    private Context context;
    
    private EmulatorPrinter printer;
    private EmulatorCardReader cardReader;
    private EmulatorPinpad pinpad;
    private EmulatorDevice device;
    
    @Override
    public void initialize(Context context, IInitCallback callback) {
        this.context = context;
        
        Log.d(TAG, "Initializing Emulator SDK...");
        
        // Simulate initialization delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Initialize components
                printer = new EmulatorPrinter();
                cardReader = new EmulatorCardReader();
                pinpad = new EmulatorPinpad();
                device = new EmulatorDevice();
                
                initialized = true;
                Log.d(TAG, "Emulator SDK initialized successfully");
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Emulator SDK", e);
                callback.onError(-1, e.getMessage());
            }
        }, 500); // 500ms delay
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public IPrinter getPrinter() {
        return printer;
    }
    
    @Override
    public ICardReader getCardReader() {
        return cardReader;
    }
    
    @Override
    public IPinpad getPinpad() {
        return pinpad;
    }
    
    @Override
    public IDevice getDevice() {
        return device;
    }
    
    @Override
    public ILed getLed() {
        // LED is part of device in emulator
        return null;
    }
    
    @Override
    public IBuzzer getBuzzer() {
        // Buzzer is part of device in emulator
        return null;
    }
    
    @Override
    public IPsam getPsam() {
        // PSAM not supported in emulator
        return null;
    }
    
    @Override
    public ICrypto getCrypto() {
        // Crypto not supported in emulator
        return null;
    }
    
    @Override
    public IDukpt getDukpt() {
        // DUKPT not supported in emulator
        return null;
    }
    
    @Override
    public IKeyManager getKeyManager() {
        // Key manager not supported in emulator
        return null;
    }
    
    @Override
    public ISerialPort getSerialPort() {
        // Serial port not supported in emulator
        return null;
    }
    
    @Override
    public IBtScreen getBtScreen() {
        // Bluetooth screen not supported in emulator
        return null;
    }
    
    @Override
    public String getSDKName() {
        return "Emulator SDK";
    }
    
    @Override
    public String getSDKVersion() {
        return "1.0.0";
    }
    
    @Override
    public void release() {
        initialized = false;
        
        if (printer != null) {
            printer.release();
            printer = null;
        }
        
        if (cardReader != null) {
            cardReader.release();
            cardReader = null;
        }
        
        if (pinpad != null) {
            pinpad.release();
            pinpad = null;
        }
        
        if (device != null) {
            device.release();
            device = null;
        }
        
        Log.d(TAG, "Emulator SDK released");
    }
}