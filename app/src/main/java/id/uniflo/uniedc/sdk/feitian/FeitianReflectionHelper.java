package id.uniflo.uniedc.sdk.feitian;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for accessing Feitian SDK via reflection
 * This allows the app to compile and run even when Feitian SDK is not available
 */
public class FeitianReflectionHelper {
    
    private static final String TAG = "FeitianReflection";
    private static final Map<String, Class<?>> classCache = new HashMap<>();
    
    /**
     * Load a class from Feitian SDK
     */
    public static Class<?> loadClass(String className) {
        if (classCache.containsKey(className)) {
            return classCache.get(className);
        }
        
        try {
            Class<?> clazz = Class.forName(className);
            classCache.put(className, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "Class not found: " + className);
            return null;
        }
    }
    
    /**
     * Get method from class
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        if (clazz == null) return null;
        
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Method not found: " + methodName);
            return null;
        }
    }
    
    /**
     * Get constructor from class
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        if (clazz == null) return null;
        
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Constructor not found");
            return null;
        }
    }
    
    /**
     * Invoke static method
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, 
                                           Class<?>[] paramTypes, Object[] args) {
        try {
            Method method = getMethod(clazz, methodName, paramTypes);
            if (method == null) return null;
            
            return method.invoke(null, args);
        } catch (Exception e) {
            Log.e(TAG, "Failed to invoke static method: " + methodName, e);
            return null;
        }
    }
    
    /**
     * Invoke instance method
     */
    public static Object invokeMethod(Object instance, String methodName, 
                                     Class<?>[] paramTypes, Object[] args) {
        if (instance == null) return null;
        
        try {
            Method method = getMethod(instance.getClass(), methodName, paramTypes);
            if (method == null) return null;
            
            return method.invoke(instance, args);
        } catch (Exception e) {
            Log.e(TAG, "Failed to invoke method: " + methodName, e);
            return null;
        }
    }
    
    /**
     * Create instance
     */
    public static Object createInstance(Class<?> clazz, Class<?>[] paramTypes, Object[] args) {
        if (clazz == null) return null;
        
        try {
            Constructor<?> constructor = getConstructor(clazz, paramTypes);
            if (constructor == null) return null;
            
            return constructor.newInstance(args);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create instance", e);
            return null;
        }
    }
    
    /**
     * Set field value
     */
    public static void setField(Object instance, String fieldName, Object value) {
        if (instance == null) return;
        
        try {
            java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set field: " + fieldName, e);
        }
    }
    
    /**
     * Check if Feitian SDK is available
     */
    public static boolean isFeitianSDKAvailable() {
        Class<?> serviceManagerClass = loadClass("com.ftpos.library.smartpos.service.ServiceManager");
        return serviceManagerClass != null;
    }
    
    // Common Feitian SDK class names
    public static final String SERVICE_MANAGER = "com.ftpos.library.smartpos.service.ServiceManager";
    public static final String PRINTER = "com.ftpos.library.smartpos.printer.Printer";
    public static final String LED = "com.ftpos.library.smartpos.led.Led";
    public static final String BUZZER = "com.ftpos.library.smartpos.buzzer.Buzzer";
    public static final String IC_READER = "com.ftpos.library.smartpos.icreader.IcReader";
    public static final String NFC_READER = "com.ftpos.library.smartpos.nfcreader.NfcReader";
    public static final String MAG_READER = "com.ftpos.library.smartpos.magreader.MagReader";
    public static final String KEY_MANAGER = "com.ftpos.library.smartpos.keymanager.KeyManager";
    public static final String CRYPTO = "com.ftpos.library.smartpos.crypto.Crypto";
    public static final String EMV = "com.ftpos.library.smartpos.emv.Emv";
    public static final String DUKPT = "com.ftpos.library.smartpos.dukpt.Dukpt";
    public static final String ERR_CODE = "com.ftpos.library.smartpos.errcode.ErrCode";
    
    // Error code constants (mirrored from ErrCode class)
    public static final int ERR_SUCCESS = 0;
    public static final int ERR_FAIL = -1;
    public static final int ERR_TIMEOUT = -3;
    public static final int ERR_CANCEL = -4;
}