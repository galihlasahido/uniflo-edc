package id.uniflo.uniedc.sdk.emulator;

import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.IPrinter;

/**
 * Emulator Printer - Mock implementation for development
 */
public class EmulatorPrinter implements IPrinter {
    
    private static final String TAG = "EmulatorPrinter";
    private int alignment = 0;
    private int textSize = 0;
    private boolean bold = false;
    private boolean initialized = false;
    
    @Override
    public int init() {
        Log.d(TAG, "Initializing emulator printer...");
        initialized = true;
        return 0;
    }
    
    @Override
    public int printText(String data) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        Log.d(TAG, "=== PRINTING TEXT ===");
        Log.d(TAG, "Alignment: " + (alignment == 0 ? "LEFT" : alignment == 1 ? "CENTER" : "RIGHT"));
        Log.d(TAG, "Size: " + (textSize == 0 ? "NORMAL" : textSize == 1 ? "LARGE" : "EXTRA LARGE"));
        Log.d(TAG, "Bold: " + bold);
        Log.d(TAG, "Content:\n" + data);
        Log.d(TAG, "===================");
        
        return 0;
    }
    
    @Override
    public int printData(byte[] data) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        String text = new String(data);
        return printText(text);
    }
    
    @Override
    public int feedPaper(int lines) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        Log.d(TAG, "Feeding " + lines + " lines");
        return 0;
    }
    
    @Override
    public int getStatus() {
        if (!initialized) {
            return -2; // Error
        }
        
        // Always return OK for emulator
        return 0;
    }
    
    @Override
    public int setAlignment(int align) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        if (align < 0 || align > 2) {
            Log.e(TAG, "Invalid alignment: " + align);
            return -1;
        }
        
        this.alignment = align;
        Log.d(TAG, "Set alignment to: " + align);
        return 0;
    }
    
    @Override
    public int setTextSize(int size) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        if (size < 0 || size > 2) {
            Log.e(TAG, "Invalid text size: " + size);
            return -1;
        }
        
        this.textSize = size;
        Log.d(TAG, "Set text size to: " + size);
        return 0;
    }
    
    @Override
    public int setBold(boolean bold) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        this.bold = bold;
        Log.d(TAG, "Set bold to: " + bold);
        return 0;
    }
    
    @Override
    public int printBarcode(String data, int type) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        Log.d(TAG, "=== PRINTING BARCODE ===");
        Log.d(TAG, "Type: " + type);
        Log.d(TAG, "Data: " + data);
        Log.d(TAG, "======================");
        return 0;
    }
    
    @Override
    public int printQRCode(String data, int size) {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        Log.d(TAG, "=== PRINTING QR CODE ===");
        Log.d(TAG, "Size: " + size);
        Log.d(TAG, "Data: " + data);
        Log.d(TAG, "======================");
        return 0;
    }
    
    @Override
    public int cutPaper() {
        if (!initialized) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        Log.d(TAG, "Cutting paper...");
        return 0;
    }
    
    @Override
    public void release() {
        Log.d(TAG, "Releasing emulator printer");
        initialized = false;
    }
}