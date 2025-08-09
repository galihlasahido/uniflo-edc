package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.IDevice;

/**
 * Feitian Device Wrapper using Reflection
 */
public class FeitianDevice implements IDevice {
    
    private static final String TAG = "FeitianDevice";
    private final Context context;
    Object device; // Package-private for FeitianSDKProvider access
    private Object led;
    private Object ringLight;
    private Object buzzer;
    private boolean initialized = false;
    private String deviceModel;
    
    // Device model constants
    private static final String DEVICE_MODEL_F100 = "F100";
    private static final String DEVICE_MODEL_F360 = "F360";
    
    public FeitianDevice(Context context) {
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
            
            // Load device classes
            Class<?> deviceClass = FeitianReflectionHelper.loadClass("com.ftpos.library.smartpos.device.Device");
            Class<?> ledClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.LED);
            Class<?> ringLightClass = FeitianReflectionHelper.loadClass("com.ftpos.library.smartpos.device.RingLight");
            Class<?> buzzerClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.BUZZER);
            
            if (deviceClass == null) {
                Log.e(TAG, "Device class not found");
                return -1;
            }
            
            // Get device instance
            device = FeitianReflectionHelper.invokeStaticMethod(
                deviceClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            if (device == null) {
                Log.e(TAG, "Failed to get Device instance");
                return -1;
            }
            
            // Get device model
            Object modelObj = FeitianReflectionHelper.invokeMethod(
                device, "getProductModel", new Class<?>[0], new Object[0]
            );
            deviceModel = modelObj != null ? modelObj.toString() : "Unknown";
            Log.d(TAG, "Device model: " + deviceModel);
            
            // Initialize hardware components based on device model
            if (!DEVICE_MODEL_F100.equals(deviceModel)) {
                // F100 doesn't have LED/buzzer
                
                if (DEVICE_MODEL_F360.equals(deviceModel) && ringLightClass != null) {
                    // F360 uses ring light instead of LED
                    ringLight = FeitianReflectionHelper.invokeStaticMethod(
                        ringLightClass, "getInstance", 
                        new Class<?>[] { Context.class }, 
                        new Object[] { context }
                    );
                    
                    if (ringLight != null) {
                        FeitianReflectionHelper.invokeMethod(
                            ringLight, "open", new Class<?>[0], new Object[0]
                        );
                    }
                } else if (ledClass != null) {
                    // Other models use regular LED
                    led = FeitianReflectionHelper.invokeStaticMethod(
                        ledClass, "getInstance", 
                        new Class<?>[] { Context.class }, 
                        new Object[] { context }
                    );
                    
                    if (led != null) {
                        FeitianReflectionHelper.invokeMethod(
                            led, "open", new Class<?>[0], new Object[0]
                        );
                    }
                }
                
                // Initialize buzzer
                if (buzzerClass != null) {
                    buzzer = FeitianReflectionHelper.invokeStaticMethod(
                        buzzerClass, "getInstance", 
                        new Class<?>[] { Context.class }, 
                        new Object[] { context }
                    );
                    
                    if (buzzer != null) {
                        FeitianReflectionHelper.invokeMethod(
                            buzzer, "open", new Class<?>[0], new Object[0]
                        );
                    }
                }
            }
            
            initialized = true;
            Log.d(TAG, "Device initialized successfully");
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize device", e);
            return -1;
        }
    }
    
    @Override
    public String getSerialNumber() {
        if (!initialized || device == null) {
            return "UNKNOWN";
        }
        
        try {
            Object sn = FeitianReflectionHelper.invokeMethod(
                device, "getSerialNumber", new Class<?>[0], new Object[0]
            );
            return sn != null ? sn.toString() : "UNKNOWN";
        } catch (Exception e) {
            Log.e(TAG, "Failed to get serial number", e);
            return "UNKNOWN";
        }
    }
    
    @Override
    public String getModel() {
        if (!initialized || device == null) {
            return "UNKNOWN";
        }
        
        try {
            Object model = FeitianReflectionHelper.invokeMethod(
                device, "getProductModel", new Class<?>[0], new Object[0]
            );
            return model != null ? model.toString() : "UNKNOWN";
        } catch (Exception e) {
            Log.e(TAG, "Failed to get model", e);
            return "UNKNOWN";
        }
    }
    
    public String getManufacturer() {
        return "Feitian";
    }
    
    @Override
    public String getFirmwareVersion() {
        if (!initialized || device == null) {
            return "UNKNOWN";
        }
        
        try {
            Object version = FeitianReflectionHelper.invokeMethod(
                device, "getFirmwareVersion", new Class<?>[0], new Object[0]
            );
            return version != null ? version.toString() : "UNKNOWN";
        } catch (Exception e) {
            Log.e(TAG, "Failed to get firmware version", e);
            return "UNKNOWN";
        }
    }
    
    @Override
    public int getBatteryLevel() {
        // Feitian doesn't provide direct battery level API
        // Return 100 to indicate full battery
        return 100;
    }
    
    @Override
    public boolean isCharging() {
        // Feitian doesn't provide charging status API
        return false;
    }
    
    @Override
    public int setLed(int ledIndex, boolean on) {
        if (!initialized) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        try {
            if (DEVICE_MODEL_F360.equals(deviceModel) && ringLight != null) {
                // F360 uses ring light
                Object ret;
                if (on) {
                    // Turn on with white color
                    ret = FeitianReflectionHelper.invokeMethod(
                        ringLight, "turnOn",
                        new Class<?>[] { int.class },
                        new Object[] { 0xFFFFFF }
                    );
                } else {
                    ret = FeitianReflectionHelper.invokeMethod(
                        ringLight, "turnOff",
                        new Class<?>[0],
                        new Object[0]
                    );
                }
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to control ring light: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
                return 0;
                
            } else if (led != null) {
                // Other devices use regular LED
                // Load LED constants
                Class<?> ledClass = led.getClass();
                Integer LED_COLOR_OFF = getStaticFieldValue(ledClass, "LED_COLOR_OFF", 0);
                Integer LED_COLOR_RED = getStaticFieldValue(ledClass, "LED_COLOR_RED", 1);
                Integer LED_INDEX_0 = getStaticFieldValue(ledClass, "LED_INDEX_0", 0);
                Integer LED_INDEX_1 = getStaticFieldValue(ledClass, "LED_INDEX_1", 1);
                Integer LED_INDEX_2 = getStaticFieldValue(ledClass, "LED_INDEX_2", 2);
                
                int color = on ? LED_COLOR_RED : LED_COLOR_OFF;
                
                // Map ledIndex to Feitian LED index
                int feitianLedIndex;
                switch (ledIndex) {
                    case 0:
                        feitianLedIndex = LED_INDEX_0;
                        break;
                    case 1:
                        feitianLedIndex = LED_INDEX_1;
                        break;
                    case 2:
                        feitianLedIndex = LED_INDEX_2;
                        break;
                    default:
                        Log.e(TAG, "Invalid LED index: " + ledIndex);
                        return -1;
                }
                
                Object ret = FeitianReflectionHelper.invokeMethod(
                    led, "setLedColor",
                    new Class<?>[] { int.class, int.class },
                    new Object[] { feitianLedIndex, color }
                );
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to set LED: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
                
                return 0;
            } else {
                Log.w(TAG, "LED not available on this device");
                return -1;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set LED", e);
            return -1;
        }
    }
    
    @Override
    public int beep(int duration) {
        if (!initialized) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        if (buzzer == null) {
            Log.w(TAG, "Buzzer not available on this device");
            return -1;
        }
        
        try {
            // Use default frequency of 2000Hz
            int freq = 2000;
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                buzzer, "beep",
                new Class<?>[] { int.class, int.class },
                new Object[] { freq, duration }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to beep: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to beep", e);
            return -1;
        }
    }
    
    @Override
    public int setLedColor(int ledIndex, int color) {
        if (!initialized) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        try {
            if (DEVICE_MODEL_F360.equals(deviceModel) && ringLight != null) {
                // F360 uses ring light
                Object ret = FeitianReflectionHelper.invokeMethod(
                    ringLight, "turnOn",
                    new Class<?>[] { int.class },
                    new Object[] { color }
                );
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to set ring light color: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
                return 0;
                
            } else if (led != null) {
                // Other devices use regular LED with limited colors
                // Load LED constants
                Class<?> ledClass = led.getClass();
                Integer LED_COLOR_OFF = getStaticFieldValue(ledClass, "LED_COLOR_OFF", 0);
                Integer LED_COLOR_RED = getStaticFieldValue(ledClass, "LED_COLOR_RED", 1);
                Integer LED_COLOR_GREEN = getStaticFieldValue(ledClass, "LED_COLOR_GREEN", 2);
                Integer LED_COLOR_BLUE = getStaticFieldValue(ledClass, "LED_COLOR_BLUE", 3);
                Integer LED_COLOR_WHITE = getStaticFieldValue(ledClass, "LED_COLOR_WHITE", 7);
                Integer LED_INDEX_0 = getStaticFieldValue(ledClass, "LED_INDEX_0", 0);
                Integer LED_INDEX_1 = getStaticFieldValue(ledClass, "LED_INDEX_1", 1);
                Integer LED_INDEX_2 = getStaticFieldValue(ledClass, "LED_INDEX_2", 2);
                
                // Convert RGB color to Feitian LED color
                int feitianColor;
                if (color == 0) {
                    feitianColor = LED_COLOR_OFF;
                } else if ((color & 0xFF0000) > 0 && (color & 0x00FF00) == 0 && (color & 0x0000FF) == 0) {
                    feitianColor = LED_COLOR_RED;
                } else if ((color & 0xFF0000) == 0 && (color & 0x00FF00) > 0 && (color & 0x0000FF) == 0) {
                    feitianColor = LED_COLOR_GREEN;
                } else if ((color & 0xFF0000) == 0 && (color & 0x00FF00) == 0 && (color & 0x0000FF) > 0) {
                    feitianColor = LED_COLOR_BLUE;
                } else {
                    feitianColor = LED_COLOR_WHITE;
                }
                
                // Map ledIndex to Feitian LED index
                int feitianLedIndex;
                switch (ledIndex) {
                    case 0:
                        feitianLedIndex = LED_INDEX_0;
                        break;
                    case 1:
                        feitianLedIndex = LED_INDEX_1;
                        break;
                    case 2:
                        feitianLedIndex = LED_INDEX_2;
                        break;
                    default:
                        Log.e(TAG, "Invalid LED index: " + ledIndex);
                        return -1;
                }
                
                Object ret = FeitianReflectionHelper.invokeMethod(
                    led, "setLedColor",
                    new Class<?>[] { int.class, int.class },
                    new Object[] { feitianLedIndex, feitianColor }
                );
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to set LED color: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
                
                return 0;
            } else {
                Log.w(TAG, "LED not available on this device");
                return -1;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set LED color", e);
            return -1;
        }
    }
    
    @Override
    public long getSystemTime() {
        return System.currentTimeMillis();
    }
    
    @Override
    public int setSystemTime(long timeMillis) {
        if (!initialized || device == null) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        try {
            // Convert to seconds
            int seconds = (int)(timeMillis / 1000);
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                device, "setSystemTime",
                new Class<?>[] { int.class },
                new Object[] { seconds }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to set system time: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set system time", e);
            return -1;
        }
    }
    
    @Override
    public int reboot() {
        if (!initialized || device == null) {
            Log.e(TAG, "Device not initialized");
            return -1;
        }
        
        try {
            Object ret = FeitianReflectionHelper.invokeMethod(
                device, "reboot",
                new Class<?>[0],
                new Object[0]
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to reboot: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to reboot", e);
            return -1;
        }
    }
    
    @Override
    public int getCapabilities() {
        if (!initialized) {
            return 0;
        }
        
        int capabilities = 0;
        
        // Basic capabilities
        capabilities |= 0x0001; // Has serial number
        capabilities |= 0x0002; // Has model info
        capabilities |= 0x0004; // Has firmware version
        
        // Hardware capabilities based on device
        if (buzzer != null) {
            capabilities |= 0x0010; // Has buzzer
        }
        
        if (led != null || ringLight != null) {
            capabilities |= 0x0020; // Has LED
        }
        
        if (!DEVICE_MODEL_F100.equals(deviceModel)) {
            capabilities |= 0x0040; // Has card reader
            capabilities |= 0x0080; // Has PIN pad
        }
        
        // Printer capability (not on F100/F300/F600)
        if (!DEVICE_MODEL_F100.equals(deviceModel) && 
            !deviceModel.contains("F300") && 
            !deviceModel.contains("F600")) {
            capabilities |= 0x0100; // Has printer
        }
        
        return capabilities;
    }
    
    @Override
    public void release() {
        if (led != null) {
            try {
                FeitianReflectionHelper.invokeMethod(
                    led, "close", new Class<?>[0], new Object[0]
                );
            } catch (Exception e) {
                Log.e(TAG, "Failed to close LED", e);
            }
            led = null;
        }
        
        if (ringLight != null) {
            try {
                FeitianReflectionHelper.invokeMethod(
                    ringLight, "close", new Class<?>[0], new Object[0]
                );
            } catch (Exception e) {
                Log.e(TAG, "Failed to close ring light", e);
            }
            ringLight = null;
        }
        
        if (buzzer != null) {
            try {
                FeitianReflectionHelper.invokeMethod(
                    buzzer, "close", new Class<?>[0], new Object[0]
                );
            } catch (Exception e) {
                Log.e(TAG, "Failed to close buzzer", e);
            }
            buzzer = null;
        }
        
        device = null;
        initialized = false;
        Log.d(TAG, "Device released");
    }
    
    // Helper method to get static field value
    private Integer getStaticFieldValue(Class<?> clazz, String fieldName, int defaultValue) {
        try {
            java.lang.reflect.Field field = clazz.getField(fieldName);
            return field.getInt(null);
        } catch (Exception e) {
            Log.w(TAG, "Failed to get field " + fieldName + ", using default: " + defaultValue);
            return defaultValue;
        }
    }
}