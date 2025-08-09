package id.uniflo.uniedc.sdk.emulator;

import android.os.Handler;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.ICardReader;

/**
 * Emulator Card Reader - Mock implementation for development
 */
public class EmulatorCardReader implements ICardReader {
    
    private static final String TAG = "EmulatorCardReader";
    private boolean initialized = false;
    private boolean cardPresent = false;
    private int detectedCardType = 0;
    private Handler handler = new Handler();
    
    @Override
    public int init() {
        Log.d(TAG, "Initializing emulator card reader...");
        initialized = true;
        return 0;
    }
    
    @Override
    public int open(int cardTypes, int timeout, ICardDetectListener listener) {
        if (!initialized) {
            Log.e(TAG, "Card reader not initialized");
            return -1;
        }
        
        Log.d(TAG, "Opening card reader for types: " + cardTypes + ", timeout: " + timeout + "s");
        
        // Simulate card detection after 2 seconds
        handler.postDelayed(() -> {
            if (listener != null) {
                // Randomly select a card type
                int[] types = {CARD_TYPE_MAG, CARD_TYPE_IC, CARD_TYPE_NFC};
                detectedCardType = types[(int)(Math.random() * types.length)];
                cardPresent = true;
                
                Log.d(TAG, "Simulating card detected: " + getCardTypeName(detectedCardType));
                listener.onCardDetected(detectedCardType);
            }
        }, 2000);
        
        return 0;
    }
    
    @Override
    public int close() {
        if (!initialized) {
            Log.e(TAG, "Card reader not initialized");
            return -1;
        }
        
        Log.d(TAG, "Closing card reader");
        cardPresent = false;
        detectedCardType = 0;
        handler.removeCallbacksAndMessages(null);
        return 0;
    }
    
    @Override
    public byte[] powerOn() {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_IC) {
            Log.e(TAG, "Cannot power on - no IC card present");
            return null;
        }
        
        Log.d(TAG, "Powering on IC card");
        // Return dummy ATR
        return new byte[]{0x3B, 0x65, 0x00, 0x00, 0x20, 0x56, 0x34, 0x45, 0x4D, 0x56};
    }
    
    @Override
    public int powerOff() {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_IC) {
            Log.e(TAG, "Cannot power off - no IC card present");
            return -1;
        }
        
        Log.d(TAG, "Powering off IC card");
        return 0;
    }
    
    @Override
    public byte[] sendApdu(byte[] apdu) {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_IC) {
            Log.e(TAG, "Cannot send APDU - no IC card present");
            return null;
        }
        
        Log.d(TAG, "Sending APDU: " + bytesToHex(apdu));
        
        // Return dummy response (SW=9000 success)
        return new byte[]{(byte)0x90, 0x00};
    }
    
    @Override
    public String getTrackData(int track) {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_MAG) {
            Log.e(TAG, "Cannot get track data - no magnetic card present");
            return null;
        }
        
        Log.d(TAG, "Getting track " + track + " data");
        
        // Return dummy track data
        switch (track) {
            case 1:
                return "%B4111111111111111^TEST/CARD^2512101123456789?";
            case 2:
                return ";4111111111111111=25121011234567890?";
            case 3:
                return ";014111111111111111=724724000000000000701234567890123?";
            default:
                return null;
        }
    }
    
    @Override
    public boolean isCardPresent() {
        return cardPresent;
    }
    
    @Override
    public int getCardType() {
        return detectedCardType;
    }
    
    @Override
    public void release() {
        Log.d(TAG, "Releasing emulator card reader");
        handler.removeCallbacksAndMessages(null);
        initialized = false;
        cardPresent = false;
        detectedCardType = 0;
    }
    
    private String getCardTypeName(int type) {
        switch (type) {
            case CARD_TYPE_MAG:
                return "Magnetic";
            case CARD_TYPE_IC:
                return "IC/Chip";
            case CARD_TYPE_NFC:
                return "NFC/Contactless";
            default:
                return "Unknown";
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}