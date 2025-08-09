package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import id.uniflo.uniedc.sdk.interfaces.IPinpad;

/**
 * Feitian Pinpad Wrapper using Reflection
 */
public class FeitianPinpad implements IPinpad {
    
    private static final String TAG = "FeitianPinpad";
    private final Context context;
    private Object emv;
    private Object keyManager;
    private Object crypto;
    private boolean initialized = false;
    
    public FeitianPinpad(Context context) {
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
            
            // Load required classes
            Class<?> emvClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.EMV);
            Class<?> keyManagerClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.KEY_MANAGER);
            Class<?> cryptoClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.CRYPTO);
            
            if (emvClass == null || keyManagerClass == null || cryptoClass == null) {
                Log.e(TAG, "Failed to load pinpad classes");
                return -1;
            }
            
            // Get instances
            emv = FeitianReflectionHelper.invokeStaticMethod(
                emvClass,
                "getInstance",
                new Class<?>[] { Context.class },
                new Object[] { context }
            );
            
            keyManager = FeitianReflectionHelper.invokeStaticMethod(
                keyManagerClass,
                "getInstance",
                new Class<?>[] { Context.class },
                new Object[] { context }
            );
            
            crypto = FeitianReflectionHelper.invokeStaticMethod(
                cryptoClass,
                "getInstance",
                new Class<?>[] { Context.class },
                new Object[] { context }
            );
            
            if (emv == null || keyManager == null || crypto == null) {
                Log.e(TAG, "Failed to get pinpad component instances");
                return -1;
            }
            
            // Set key group name for non-F100 devices
            String packageName = context.getPackageName();
            FeitianReflectionHelper.invokeMethod(
                keyManager,
                "setKeyGroupName",
                new Class<?>[] { String.class },
                new Object[] { packageName }
            );
            
            initialized = true;
            Log.d(TAG, "Pinpad initialized successfully");
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize pinpad", e);
            return -1;
        }
    }
    
    @Override
    public int inputPin(String pan, int pinLength, int timeout, IPinInputListener listener) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        try {
            // Load PinSeting class
            Class<?> pinSetingClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.pin.PinSeting"
            );
            
            if (pinSetingClass == null) {
                Log.e(TAG, "PinSeting class not found");
                return -1;
            }
            
            // Create PinSeting instance
            Object pinSeting = pinSetingClass.newInstance();
            
            // Configure PIN settings
            FeitianReflectionHelper.invokeMethod(pinSeting, "setMinPinLen", 
                new Class<?>[] { int.class }, new Object[] { 4 });
            FeitianReflectionHelper.invokeMethod(pinSeting, "setMaxPinLen", 
                new Class<?>[] { int.class }, new Object[] { pinLength > 0 ? pinLength : 12 });
            FeitianReflectionHelper.invokeMethod(pinSeting, "setTimeout", 
                new Class<?>[] { int.class }, new Object[] { timeout });
            FeitianReflectionHelper.invokeMethod(pinSeting, "setPan", 
                new Class<?>[] { String.class }, new Object[] { pan });
            FeitianReflectionHelper.invokeMethod(pinSeting, "setOnlinePinKeyIndex", 
                new Class<?>[] { int.class }, new Object[] { 0 });
            FeitianReflectionHelper.invokeMethod(pinSeting, "setOnlinePinKeyType", 
                new Class<?>[] { int.class }, new Object[] { 0 });
            FeitianReflectionHelper.invokeMethod(pinSeting, "setOnlinePinBlockFormat", 
                new Class<?>[] { int.class }, new Object[] { 0 });
            
            // Load callback interface
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.pin.OnPinInputListener"
            );
            
            if (callbackClass == null) {
                Log.e(TAG, "OnPinInputListener class not found");
                return -1;
            }
            
            // Create callback proxy
            Object callback = Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[] { callbackClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if ("onPinInputSuccess".equals(methodName)) {
                            byte[] pinBlock = args != null && args.length > 0 ? (byte[])args[0] : null;
                            Log.d(TAG, "PIN input successful");
                            if (listener != null && pinBlock != null) {
                                listener.onPinEntered(pinBlock);
                            }
                        } else if ("onPinInputError".equals(methodName)) {
                            int errorCode = args != null && args.length > 0 ? (Integer)args[0] : -1;
                            String errorMsg = args != null && args.length > 1 ? (String)args[1] : "Unknown error";
                            Log.e(TAG, "PIN input error: " + errorCode + " - " + errorMsg);
                            if (listener != null) {
                                if (errorCode == -3) { // Timeout
                                    listener.onTimeout();
                                } else if (errorCode == -4) { // Cancelled
                                    listener.onCancelled();
                                } else {
                                    listener.onError(errorCode, errorMsg);
                                }
                            }
                        }
                        return null;
                    }
                }
            );
            
            // Start PIN input - using static method
            Object ret = FeitianReflectionHelper.invokeStaticMethod(
                emv.getClass(),
                "StartPinInput",
                new Class<?>[] { pinSetingClass, callbackClass },
                new Object[] { pinSeting, callback }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to start PIN input: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to input PIN", e);
            return -1;
        }
    }
    
    @Override
    public int loadMasterKey(int keyIndex, byte[] keyData) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        try {
            // Load master key
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager,
                "downloadMainKey",
                new Class<?>[] { int.class, byte[].class, int.class, int.class },
                new Object[] { keyIndex, keyData, keyData.length, 0 }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to load master key: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            Log.d(TAG, "Master key loaded at index: " + keyIndex);
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load master key", e);
            return -1;
        }
    }
    
    @Override
    public int loadWorkKey(int masterKeyIndex, int workKeyIndex, byte[] keyData) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        try {
            // Load work key encrypted by master key
            Object ret = FeitianReflectionHelper.invokeMethod(
                keyManager,
                "downloadWorkKey",
                new Class<?>[] { int.class, int.class, int.class, byte[].class, int.class },
                new Object[] { masterKeyIndex, workKeyIndex, 0, keyData, keyData.length }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to load work key: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            Log.d(TAG, "Work key loaded - master: " + masterKeyIndex + ", work: " + workKeyIndex);
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load work key", e);
            return -1;
        }
    }
    
    @Override
    public byte[] calculateMac(byte[] data, int keyIndex) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return null;
        }
        
        try {
            // Calculate MAC using specified key
            byte[] mac = new byte[8];
            int[] macLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto,
                "calcMac",
                new Class<?>[] { int.class, int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, 0, data, data.length, mac, macLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to calculate MAC: " + ret);
                return null;
            }
            
            // Return MAC with correct length
            byte[] result = new byte[macLen[0]];
            System.arraycopy(mac, 0, result, 0, macLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate MAC", e);
            return null;
        }
    }
    
    @Override
    public byte[] encryptData(byte[] data, int keyIndex) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return null;
        }
        
        try {
            // Encrypt data using specified key
            byte[] encrypted = new byte[data.length + 16]; // Add padding space
            int[] encLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto,
                "encrypt",
                new Class<?>[] { int.class, int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, 0, data, data.length, encrypted, encLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to encrypt data: " + ret);
                return null;
            }
            
            // Return encrypted data with correct length
            byte[] result = new byte[encLen[0]];
            System.arraycopy(encrypted, 0, result, 0, encLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt data", e);
            return null;
        }
    }
    
    @Override
    public byte[] decryptData(byte[] data, int keyIndex) {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return null;
        }
        
        try {
            // Decrypt data using specified key
            byte[] decrypted = new byte[data.length];
            int[] decLen = new int[1];
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                crypto,
                "decrypt",
                new Class<?>[] { int.class, int.class, byte[].class, int.class, byte[].class, int[].class },
                new Object[] { keyIndex, 0, data, data.length, decrypted, decLen }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to decrypt data: " + ret);
                return null;
            }
            
            // Return decrypted data with correct length
            byte[] result = new byte[decLen[0]];
            System.arraycopy(decrypted, 0, result, 0, decLen[0]);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt data", e);
            return null;
        }
    }
    
    @Override
    public int cancelInput() {
        if (!initialized) {
            Log.e(TAG, "Pinpad not initialized");
            return -1;
        }
        
        try {
            // Cancel PIN input - using static method
            Object ret = FeitianReflectionHelper.invokeStaticMethod(
                emv.getClass(),
                "CancelPinInput",
                new Class<?>[0],
                new Object[0]
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to cancel PIN input: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            Log.d(TAG, "PIN input cancelled");
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel PIN input", e);
            return -1;
        }
    }
    
    @Override
    public void release() {
        initialized = false;
        emv = null;
        keyManager = null;
        crypto = null;
        Log.d(TAG, "Pinpad released");
    }
}