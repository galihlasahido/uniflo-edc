package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.ILed;

/**
 * Feitian LED Wrapper using Reflection
 */
public class FeitianLed implements ILed {
    
    private static final String TAG = "FeitianLed";
    private final Context context;
    private Object led;
    private Object ringLight;
    private boolean initialized = false;
    private boolean isRingLight = false;
    
    public FeitianLed(Context context) {
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
            
            // Try to get device model first
            Class<?> deviceClass = FeitianReflectionHelper.loadClass("com.ftpos.library.smartpos.device.Device");
            if (deviceClass != null) {
                Object device = FeitianReflectionHelper.invokeStaticMethod(
                    deviceClass, "getInstance", 
                    new Class<?>[] { Context.class }, 
                    new Object[] { context }
                );
                
                if (device != null) {
                    Object modelObj = FeitianReflectionHelper.invokeMethod(
                        device, "getProductModel", new Class<?>[0], new Object[0]
                    );
                    String model = modelObj != null ? modelObj.toString() : "";
                    
                    // F360 uses ring light
                    if ("F360".equals(model)) {
                        isRingLight = true;
                    }
                }
            }
            
            if (isRingLight) {
                // Load ring light class
                Class<?> ringLightClass = FeitianReflectionHelper.loadClass(
                    "com.ftpos.library.smartpos.device.RingLight"
                );
                
                if (ringLightClass != null) {
                    ringLight = FeitianReflectionHelper.invokeStaticMethod(
                        ringLightClass, "getInstance", 
                        new Class<?>[] { Context.class }, 
                        new Object[] { context }
                    );
                }
            } else {
                // Load regular LED class
                Class<?> ledClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.LED);
                
                if (ledClass != null) {
                    led = FeitianReflectionHelper.invokeStaticMethod(
                        ledClass, "getInstance", 
                        new Class<?>[] { Context.class }, 
                        new Object[] { context }
                    );
                }
            }
            
            if (led == null && ringLight == null) {
                Log.e(TAG, "Failed to get LED/RingLight instance");
                return -1;
            }
            
            initialized = true;
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize LED", e);
            return -1;
        }
    }
    
    @Override
    public int open() {
        if (!initialized) {
            return -1;
        }
        
        try {
            Object ret;
            if (isRingLight && ringLight != null) {
                ret = FeitianReflectionHelper.invokeMethod(
                    ringLight, "open", new Class<?>[0], new Object[0]
                );
            } else if (led != null) {
                ret = FeitianReflectionHelper.invokeMethod(
                    led, "open", new Class<?>[0], new Object[0]
                );
            } else {
                return -1;
            }
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to open LED: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open LED", e);
            return -1;
        }
    }
    
    @Override
    public int on(int ledIndex, int color) {
        if (!initialized) {
            return -1;
        }
        
        try {
            if (isRingLight && ringLight != null) {
                // Ring light uses RGB color directly
                int rgbColor = convertToRGB(color);
                Object ret = FeitianReflectionHelper.invokeMethod(
                    ringLight, "turnOn",
                    new Class<?>[] { int.class },
                    new Object[] { rgbColor }
                );
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to turn on ring light: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
            } else if (led != null) {
                // Regular LED uses setLedColor
                int feitianColor = convertToFeitianColor(color);
                int feitianIndex = convertLedIndex(ledIndex);
                
                Object ret = FeitianReflectionHelper.invokeMethod(
                    led, "setLedColor",
                    new Class<?>[] { int.class, int.class },
                    new Object[] { feitianIndex, feitianColor }
                );
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to turn on LED: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
            } else {
                return -1;
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to turn on LED", e);
            return -1;
        }
    }
    
    @Override
    public int off(int ledIndex) {
        if (!initialized) {
            return -1;
        }
        
        try {
            if (isRingLight && ringLight != null) {
                Object ret = FeitianReflectionHelper.invokeMethod(
                    ringLight, "turnOff",
                    new Class<?>[0],
                    new Object[0]
                );
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to turn off ring light: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
            } else if (led != null) {
                // Turn off by setting color to OFF
                return on(ledIndex, LED_COLOR_OFF);
            } else {
                return -1;
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to turn off LED", e);
            return -1;
        }
    }
    
    @Override
    public int setMode(int ledIndex, int mode, int onTime, int offTime) {
        // Feitian SDK doesn't support LED modes directly
        // Could be implemented with a timer
        Log.w(TAG, "LED modes not supported by Feitian SDK");
        return -1;
    }
    
    @Override
    public int getStatus(int ledIndex) {
        // Feitian SDK doesn't provide LED status query
        Log.w(TAG, "LED status query not supported by Feitian SDK");
        return -1;
    }
    
    @Override
    public int close() {
        if (!initialized) {
            return -1;
        }
        
        try {
            Object ret;
            if (isRingLight && ringLight != null) {
                ret = FeitianReflectionHelper.invokeMethod(
                    ringLight, "close", new Class<?>[0], new Object[0]
                );
            } else if (led != null) {
                ret = FeitianReflectionHelper.invokeMethod(
                    led, "close", new Class<?>[0], new Object[0]
                );
            } else {
                return -1;
            }
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to close LED: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to close LED", e);
            return -1;
        }
    }
    
    @Override
    public void release() {
        close();
        led = null;
        ringLight = null;
        initialized = false;
    }
    
    // Helper method to convert ILed color to RGB
    private int convertToRGB(int color) {
        switch (color) {
            case LED_COLOR_OFF:
                return 0x000000;
            case LED_COLOR_RED:
                return 0xFF0000;
            case LED_COLOR_GREEN:
                return 0x00FF00;
            case LED_COLOR_BLUE:
                return 0x0000FF;
            case LED_COLOR_YELLOW:
                return 0xFFFF00;
            case LED_COLOR_MAGENTA:
                return 0xFF00FF;
            case LED_COLOR_CYAN:
                return 0x00FFFF;
            case LED_COLOR_WHITE:
                return 0xFFFFFF;
            default:
                return 0x000000;
        }
    }
    
    // Helper method to convert ILed color to Feitian LED color
    private int convertToFeitianColor(int color) {
        try {
            Class<?> ledClass = led.getClass();
            
            switch (color) {
                case LED_COLOR_OFF:
                    return getStaticFieldValue(ledClass, "LED_COLOR_OFF", 0);
                case LED_COLOR_RED:
                    return getStaticFieldValue(ledClass, "LED_COLOR_RED", 1);
                case LED_COLOR_GREEN:
                    return getStaticFieldValue(ledClass, "LED_COLOR_GREEN", 2);
                case LED_COLOR_BLUE:
                    return getStaticFieldValue(ledClass, "LED_COLOR_BLUE", 3);
                case LED_COLOR_YELLOW:
                    return getStaticFieldValue(ledClass, "LED_COLOR_YELLOW", 4);
                case LED_COLOR_WHITE:
                    return getStaticFieldValue(ledClass, "LED_COLOR_WHITE", 7);
                default:
                    return getStaticFieldValue(ledClass, "LED_COLOR_OFF", 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to convert color", e);
            return 0;
        }
    }
    
    // Helper method to convert LED index
    private int convertLedIndex(int index) {
        try {
            Class<?> ledClass = led.getClass();
            
            switch (index) {
                case 0:
                    return getStaticFieldValue(ledClass, "LED_INDEX_0", 0);
                case 1:
                    return getStaticFieldValue(ledClass, "LED_INDEX_1", 1);
                case 2:
                    return getStaticFieldValue(ledClass, "LED_INDEX_2", 2);
                default:
                    return getStaticFieldValue(ledClass, "LED_INDEX_0", 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to convert LED index", e);
            return 0;
        }
    }
    
    // Helper method to get static field value
    private int getStaticFieldValue(Class<?> clazz, String fieldName, int defaultValue) {
        try {
            java.lang.reflect.Field field = clazz.getField(fieldName);
            return field.getInt(null);
        } catch (Exception e) {
            Log.w(TAG, "Failed to get field " + fieldName + ", using default: " + defaultValue);
            return defaultValue;
        }
    }
}