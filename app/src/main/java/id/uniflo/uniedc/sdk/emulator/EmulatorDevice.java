package id.uniflo.uniedc.sdk.emulator;

import android.os.Build;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.IDevice;

/**
 * Emulator Device - Mock implementation for development
 */
public class EmulatorDevice implements IDevice {
    
    private static final String TAG = "EmulatorDevice";
    private boolean initialized = false;
    
    @Override
    public int init() {
        Log.d(TAG, "Initializing emulator device...");
        initialized = true;
        return 0;
    }
    
    @Override
    public String getSerialNumber() {
        return "EMU" + System.currentTimeMillis();
    }
    
    @Override
    public String getModel() {
        return Build.MODEL + " (Emulator)";
    }
    
    @Override
    public String getFirmwareVersion() {
        return "EMU-1.0.0";
    }
    
    @Override
    public int getBatteryLevel() {
        // Return random battery level between 50-100
        return 50 + (int)(Math.random() * 50);
    }
    
    @Override
    public boolean isCharging() {
        // Randomly return charging status
        return Math.random() > 0.5;
    }
    
    @Override
    public int beep(int duration) {
        if (!initialized) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        Log.d(TAG, "BEEP! Duration: " + duration + "ms");
        return 0;
    }
    
    @Override
    public int setLed(int ledIndex, boolean on) {
        if (!initialized) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        Log.d(TAG, "LED " + ledIndex + " set to: " + (on ? "ON" : "OFF"));
        return 0;
    }
    
    @Override
    public int setLedColor(int ledIndex, int color) {
        if (!initialized) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        Log.d(TAG, "LED " + ledIndex + " color set to: #" + Integer.toHexString(color));
        return 0;
    }
    
    @Override
    public long getSystemTime() {
        return System.currentTimeMillis();
    }
    
    @Override
    public int setSystemTime(long timeMillis) {
        Log.d(TAG, "Setting system time to: " + new java.util.Date(timeMillis));
        // Cannot actually set system time in emulator
        return 0;
    }
    
    @Override
    public int reboot() {
        Log.d(TAG, "REBOOT requested (ignored in emulator)");
        return 0;
    }
    
    @Override
    public int getCapabilities() {
        // Return capabilities flags
        // For emulator, support all basic features
        return 0xFFFF;
    }
    
    @Override
    public void release() {
        Log.d(TAG, "Releasing emulator device");
        initialized = false;
    }
}