package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import id.uniflo.uniedc.sdk.interfaces.ICardReader;

/**
 * Feitian Card Reader Wrapper using Reflection - Combines IC, NFC, and MAG readers
 */
public class FeitianCardReader implements ICardReader {
    
    private static final String TAG = "FeitianCardReader";
    private final Context context;
    private Object icReader;
    private Object nfcReader;
    private Object magReader;
    
    private boolean initialized = false;
    private boolean cardPresent = false;
    private int detectedCardType = 0;
    private volatile boolean isReading = false;
    
    public FeitianCardReader(Context context) {
        this.context = context;
    }
    
    @Override
    public int init() {
        try {
            // Check if Feitian SDK is available
            if (!FeitianReflectionHelper.isFeitianSDKAvailable()) {
                Log.e(TAG, "Feitian SDK not available");
                return -1;
            }
            
            // Load reader classes
            Class<?> icReaderClass = FeitianReflectionHelper.loadClass("com.ftpos.library.smartpos.device.IcReader");
            Class<?> nfcReaderClass = FeitianReflectionHelper.loadClass("com.ftpos.library.smartpos.device.NfcReader");
            Class<?> magReaderClass = FeitianReflectionHelper.loadClass("com.ftpos.library.smartpos.device.MagReader");
            
            if (icReaderClass == null || nfcReaderClass == null || magReaderClass == null) {
                Log.e(TAG, "Failed to load reader classes");
                return -1;
            }
            
            // Get reader instances
            icReader = FeitianReflectionHelper.invokeStaticMethod(
                icReaderClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            nfcReader = FeitianReflectionHelper.invokeStaticMethod(
                nfcReaderClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            magReader = FeitianReflectionHelper.invokeStaticMethod(
                magReaderClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            if (icReader == null || nfcReader == null || magReader == null) {
                Log.e(TAG, "Failed to get reader instances");
                return -1;
            }
            
            initialized = true;
            Log.d(TAG, "Card readers initialized successfully");
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize card readers", e);
            return -1;
        }
    }
    
    @Override
    public int open(int cardTypes, int timeout, ICardDetectListener listener) {
        if (!initialized) {
            Log.e(TAG, "Card readers not initialized");
            return -1;
        }
        
        if (isReading) {
            Log.e(TAG, "Card reader already open");
            return -1;
        }
        
        isReading = true;
        cardPresent = false;
        detectedCardType = 0;
        
        // Start detection for requested card types
        if ((cardTypes & CARD_TYPE_IC) != 0) {
            startICDetection(timeout, listener);
        }
        
        if ((cardTypes & CARD_TYPE_NFC) != 0) {
            startNFCDetection(timeout, listener);
        }
        
        if ((cardTypes & CARD_TYPE_MAG) != 0) {
            startMagDetection(timeout, listener);
        }
        
        return 0;
    }
    
    private void startICDetection(int timeout, ICardDetectListener listener) {
        try {
            // Load callback interface
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.callback.OnIcReaderCallback"
            );
            
            if (callbackClass == null) {
                Log.e(TAG, "OnIcReaderCallback class not found");
                return;
            }
            
            // Create callback proxy
            Object callback = Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[] { callbackClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if ("onIcReaderSuccess".equals(methodName)) {
                            if (!isReading) return null;
                            
                            Log.d(TAG, "IC card detected");
                            cardPresent = true;
                            detectedCardType = CARD_TYPE_IC;
                            isReading = false;
                            
                            // Cancel other readers
                            cancelOtherReaders(CARD_TYPE_IC);
                            
                            if (listener != null) {
                                listener.onCardDetected(CARD_TYPE_IC);
                            }
                        } else if ("onIcReaderError".equals(methodName)) {
                            int errorCode = args != null && args.length > 0 ? (Integer)args[0] : -1;
                            String errorMsg = args != null && args.length > 1 ? (String)args[1] : "Unknown error";
                            Log.e(TAG, "IC reader error: " + errorCode + " - " + errorMsg);
                            
                            if (errorCode == -3) { // Timeout
                                // Continue waiting for other card types
                            } else if (listener != null && isReading) {
                                isReading = false;
                                listener.onError(errorCode, errorMsg);
                            }
                        }
                        return null;
                    }
                }
            );
            
            // Open card
            FeitianReflectionHelper.invokeMethod(
                icReader, "openCard",
                new Class<?>[] { int.class, callbackClass },
                new Object[] { timeout, callback }
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to start IC detection", e);
        }
    }
    
    private void startNFCDetection(int timeout, ICardDetectListener listener) {
        try {
            // Load callback interface
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.callback.OnNfcPollingCallback"
            );
            
            if (callbackClass == null) {
                Log.e(TAG, "OnNfcPollingCallback class not found");
                return;
            }
            
            // Create callback proxy
            Object callback = Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[] { callbackClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if ("onNfcPollingSuccess".equals(methodName)) {
                            if (!isReading) return null;
                            
                            Log.d(TAG, "NFC card detected");
                            cardPresent = true;
                            detectedCardType = CARD_TYPE_NFC;
                            isReading = false;
                            
                            // Cancel other readers
                            cancelOtherReaders(CARD_TYPE_NFC);
                            
                            if (listener != null) {
                                listener.onCardDetected(CARD_TYPE_NFC);
                            }
                        } else if ("onNfcPollingError".equals(methodName)) {
                            int errorCode = args != null && args.length > 0 ? (Integer)args[0] : -1;
                            String errorMsg = args != null && args.length > 1 ? (String)args[1] : "Unknown error";
                            Log.e(TAG, "NFC reader error: " + errorCode + " - " + errorMsg);
                            
                            if (errorCode == -3) { // Timeout
                                // Continue waiting for other card types
                            } else if (listener != null && isReading) {
                                isReading = false;
                                listener.onError(errorCode, errorMsg);
                            }
                        }
                        return null;
                    }
                }
            );
            
            // Open card
            FeitianReflectionHelper.invokeMethod(
                nfcReader, "openCardEx",
                new Class<?>[] { int.class, callbackClass },
                new Object[] { timeout, callback }
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to start NFC detection", e);
        }
    }
    
    private void startMagDetection(int timeout, ICardDetectListener listener) {
        try {
            // Load callback interface
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.callback.OnMagReadCallback"
            );
            
            if (callbackClass == null) {
                Log.e(TAG, "OnMagReadCallback class not found");
                return;
            }
            
            // Create callback proxy
            Object callback = Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[] { callbackClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if ("onMagReadSuccess".equals(methodName)) {
                            if (!isReading) return null;
                            
                            Log.d(TAG, "Magnetic card detected");
                            cardPresent = true;
                            detectedCardType = CARD_TYPE_MAG;
                            isReading = false;
                            
                            // Cancel other readers
                            cancelOtherReaders(CARD_TYPE_MAG);
                            
                            if (listener != null) {
                                listener.onCardDetected(CARD_TYPE_MAG);
                            }
                        } else if ("onMagReadError".equals(methodName)) {
                            int errorCode = args != null && args.length > 0 ? (Integer)args[0] : -1;
                            String errorMsg = args != null && args.length > 1 ? (String)args[1] : "Unknown error";
                            Log.e(TAG, "Mag reader error: " + errorCode + " - " + errorMsg);
                            
                            if (errorCode == -3) { // Timeout
                                // Continue waiting for other card types
                            } else if (listener != null && isReading) {
                                isReading = false;
                                listener.onError(errorCode, errorMsg);
                            }
                        }
                        return null;
                    }
                }
            );
            
            // Read mag card
            FeitianReflectionHelper.invokeMethod(
                magReader, "readMagCard",
                new Class<?>[] { int.class, int.class, callbackClass },
                new Object[] { timeout, 0x07, callback } // Read all 3 tracks
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MAG detection", e);
        }
    }
    
    private void cancelOtherReaders(int detectedType) {
        try {
            if (detectedType != CARD_TYPE_IC && icReader != null) {
                FeitianReflectionHelper.invokeMethod(
                    icReader, "close", new Class<?>[0], new Object[0]
                );
            }
            if (detectedType != CARD_TYPE_NFC && nfcReader != null) {
                FeitianReflectionHelper.invokeMethod(
                    nfcReader, "nfcPollingClose", new Class<?>[0], new Object[0]
                );
            }
            if (detectedType != CARD_TYPE_MAG && magReader != null) {
                FeitianReflectionHelper.invokeMethod(
                    magReader, "close", new Class<?>[0], new Object[0]
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error canceling other readers", e);
        }
    }
    
    @Override
    public int close() {
        if (!initialized) {
            Log.e(TAG, "Card readers not initialized");
            return -1;
        }
        
        isReading = false;
        cardPresent = false;
        
        try {
            if (icReader != null) {
                FeitianReflectionHelper.invokeMethod(
                    icReader, "close", new Class<?>[0], new Object[0]
                );
            }
            if (nfcReader != null) {
                FeitianReflectionHelper.invokeMethod(
                    nfcReader, "nfcPollingClose", new Class<?>[0], new Object[0]
                );
            }
            if (magReader != null) {
                FeitianReflectionHelper.invokeMethod(
                    magReader, "close", new Class<?>[0], new Object[0]
                );
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to close card readers", e);
            return -1;
        }
    }
    
    @Override
    public byte[] powerOn() {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_IC) {
            Log.e(TAG, "Cannot power on - no IC card present");
            return null;
        }
        
        // IC card is already powered on during detection
        // Get ATR
        try {
            byte[] atr = new byte[256];
            int[] atrLen = new int[1];
            
            // Send dummy APDU to get ATR (Feitian doesn't have direct ATR method)
            // Use SELECT command
            byte[] selectCmd = {0x00, (byte)0xA4, 0x04, 0x00, 0x00};
            byte[] response = new byte[256];
            int[] respLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                icReader, "sendApduCustomer",
                new Class<?>[] { byte[].class, int.class, byte[].class, int[].class },
                new Object[] { selectCmd, selectCmd.length, response, respLen }
            );
            
            // For now, return a dummy ATR
            return new byte[]{0x3B, 0x65, 0x00, 0x00, 0x20, 0x56, 0x34, 0x45, 0x4D, 0x56};
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get ATR", e);
            return null;
        }
    }
    
    @Override
    public int powerOff() {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_IC) {
            Log.e(TAG, "Cannot power off - no IC card present");
            return -1;
        }
        
        try {
            FeitianReflectionHelper.invokeMethod(
                icReader, "close", new Class<?>[0], new Object[0]
            );
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to power off IC card", e);
            return -1;
        }
    }
    
    @Override
    public byte[] sendApdu(byte[] apdu) {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_IC) {
            Log.e(TAG, "Cannot send APDU - no IC card present");
            return null;
        }
        
        try {
            byte[] response = new byte[256];
            int[] respLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                icReader, "sendApduCustomer",
                new Class<?>[] { byte[].class, int.class, byte[].class, int[].class },
                new Object[] { apdu, apdu.length, response, respLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to send APDU: " + ret);
                return null;
            }
            
            // Copy response to correct size array
            byte[] result = new byte[respLen[0]];
            System.arraycopy(response, 0, result, 0, respLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to send APDU", e);
            return null;
        }
    }
    
    @Override
    public String getTrackData(int track) {
        if (!initialized || !cardPresent || detectedCardType != CARD_TYPE_MAG) {
            Log.e(TAG, "Cannot get track data - no magnetic card present");
            return null;
        }
        
        try {
            // Read magnetic card data
            // Load callback interface
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.callback.OnMagReadCallback"
            );
            
            if (callbackClass == null) {
                Log.e(TAG, "OnMagReadCallback class not found");
                return null;
            }
            
            final String[] trackData = new String[1];
            final Object lock = new Object();
            
            // Create callback proxy
            Object callback = Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[] { callbackClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if ("onMagReadSuccess".equals(methodName)) {
                            Object magData = args != null && args.length > 0 ? args[0] : null;
                            if (magData != null) {
                                synchronized (lock) {
                                    switch (track) {
                                        case 1:
                                            trackData[0] = (String) FeitianReflectionHelper.invokeMethod(
                                                magData, "getTrack1", new Class<?>[0], new Object[0]
                                            );
                                            break;
                                        case 2:
                                            trackData[0] = (String) FeitianReflectionHelper.invokeMethod(
                                                magData, "getTrack2", new Class<?>[0], new Object[0]
                                            );
                                            break;
                                        case 3:
                                            trackData[0] = (String) FeitianReflectionHelper.invokeMethod(
                                                magData, "getTrack3", new Class<?>[0], new Object[0]
                                            );
                                            break;
                                    }
                                    lock.notify();
                                }
                            }
                        } else if ("onMagReadError".equals(methodName)) {
                            int errorCode = args != null && args.length > 0 ? (Integer)args[0] : -1;
                            String errorMsg = args != null && args.length > 1 ? (String)args[1] : "Unknown error";
                            Log.e(TAG, "Failed to read track data: " + errorCode + " - " + errorMsg);
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                        return null;
                    }
                }
            );
            
            // Read mag card
            FeitianReflectionHelper.invokeMethod(
                magReader, "readMagCard",
                new Class<?>[] { int.class, int.class, callbackClass },
                new Object[] { 5, 1 << (track - 1), callback }
            );
            
            // Wait for read to complete
            synchronized (lock) {
                try {
                    lock.wait(5000); // 5 second timeout
                } catch (InterruptedException e) {
                    Log.e(TAG, "Track read interrupted", e);
                    return null;
                }
            }
            
            return trackData[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get track data", e);
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
        close();
        initialized = false;
        icReader = null;
        nfcReader = null;
        magReader = null;
        Log.d(TAG, "Card readers released");
    }
}