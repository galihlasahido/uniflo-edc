package id.uniflo.uniedc.sdk.emulator;

import android.os.Handler;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.IPinpad;

/**
 * Emulator Pinpad - Mock implementation for development
 */
public class EmulatorPinpad implements IPinpad {
    
    private static final String TAG = "EmulatorPinpad";
    private boolean initialized = false;
    private Handler handler = new Handler();
    
    @Override
    public int init() {
        Log.d(TAG, "Initializing emulator pinpad...");
        initialized = true;
        return 0;
    }
    
    @Override
    public int inputPin(String pan, int pinLength, int timeout, IPinInputListener listener) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        Log.d(TAG, "Requesting PIN input for PAN: " + maskPan(pan) + ", length: " + pinLength);
        
        // Simulate PIN entry after 3 seconds
        handler.postDelayed(() -> {
            if (listener != null) {
                // Generate dummy PIN block
                byte[] pinBlock = new byte[8];
                for (int i = 0; i < 8; i++) {
                    pinBlock[i] = (byte)(Math.random() * 256);
                }
                
                Log.d(TAG, "Simulating PIN entered");
                listener.onPinEntered(pinBlock);
            }
        }, 3000);
        
        return 0;
    }
    
    @Override
    public int loadMasterKey(int keyIndex, byte[] keyData) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        Log.d(TAG, "Loading master key at index: " + keyIndex);
        return 0;
    }
    
    @Override
    public int loadWorkKey(int masterKeyIndex, int workKeyIndex, byte[] keyData) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        Log.d(TAG, "Loading work key - master: " + masterKeyIndex + ", work: " + workKeyIndex);
        return 0;
    }
    
    @Override
    public byte[] calculateMac(byte[] data, int keyIndex) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return null;
        }
        
        Log.d(TAG, "Calculating MAC with key: " + keyIndex);
        
        // Return dummy MAC (8 bytes)
        byte[] mac = new byte[8];
        for (int i = 0; i < 8; i++) {
            mac[i] = (byte)(Math.random() * 256);
        }
        return mac;
    }
    
    @Override
    public byte[] encryptData(byte[] data, int keyIndex) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return null;
        }
        
        Log.d(TAG, "Encrypting data with key: " + keyIndex);
        
        // Return dummy encrypted data (same length as input)
        byte[] encrypted = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            encrypted[i] = (byte)(data[i] ^ 0xFF); // Simple XOR for demo
        }
        return encrypted;
    }
    
    @Override
    public byte[] decryptData(byte[] data, int keyIndex) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return null;
        }
        
        Log.d(TAG, "Decrypting data with key: " + keyIndex);
        
        // Return dummy decrypted data (same length as input)
        byte[] decrypted = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            decrypted[i] = (byte)(data[i] ^ 0xFF); // Simple XOR for demo
        }
        return decrypted;
    }
    
    @Override
    public int cancelInput() {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        Log.d(TAG, "Cancelling PIN input");
        handler.removeCallbacksAndMessages(null);
        return 0;
    }
    
    @Override
    public void release() {
        Log.d(TAG, "Releasing emulator pinpad");
        handler.removeCallbacksAndMessages(null);
        initialized = false;
    }
    
    private String maskPan(String pan) {
        if (pan == null || pan.length() < 10) {
            return pan;
        }
        
        // Show first 6 and last 4 digits
        return pan.substring(0, 6) + "****" + pan.substring(pan.length() - 4);
    }
}