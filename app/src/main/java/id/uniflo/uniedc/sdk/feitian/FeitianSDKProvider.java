package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import id.uniflo.uniedc.sdk.interfaces.*;

/**
 * Feitian SDK Provider using Reflection - Wrapper for Feitian FTSDK
 */
public class FeitianSDKProvider implements ISDKProvider {
    
    private static final String TAG = "FeitianSDK";
    private boolean initialized = false;
    private Context context;
    
    private FeitianPrinter printer;
    private FeitianCardReader cardReader;
    private FeitianPinpad pinpad;
    private FeitianDevice device;
    private FeitianLed led;
    private FeitianBuzzer buzzer;
    private FeitianCrypto crypto;
    private FeitianKeyManager keyManager;
    // Note: PSAM, DUKPT, SerialPort, and BtScreen will be null as they need specific implementation
    
    @Override
    public void initialize(Context context, IInitCallback callback) {
        this.context = context;
        
        Log.d(TAG, "Initializing Feitian SDK...");
        
        // Check if Feitian SDK is available
        if (!FeitianReflectionHelper.isFeitianSDKAvailable()) {
            Log.e(TAG, "Feitian SDK not available, falling back to emulator");
            callback.onError(-1, "Feitian SDK not found");
            return;
        }
        
        // Try to bind to Feitian POS service using reflection
        try {
            Class<?> serviceManagerClass = FeitianReflectionHelper.loadClass(
                FeitianReflectionHelper.SERVICE_MANAGER
            );
            
            if (serviceManagerClass == null) {
                Log.e(TAG, "ServiceManager class not found");
                callback.onError(-1, "ServiceManager not found");
                return;
            }
            
            // Load callback interface
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.callback.OnServiceConnectCallback"
            );
            
            if (callbackClass == null) {
                Log.e(TAG, "OnServiceConnectCallback class not found");
                callback.onError(-1, "Callback interface not found");
                return;
            }
            
            // Create callback proxy
            Object serviceCallback = Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class<?>[] { callbackClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if ("onSuccess".equals(methodName)) {
                            try {
                                Log.d(TAG, "ServiceManager bound successfully");
                                
                                // Initialize wrapper components
                                printer = new FeitianPrinter(context);
                                cardReader = new FeitianCardReader(context);
                                pinpad = new FeitianPinpad(context);
                                device = new FeitianDevice(context);
                                led = new FeitianLed(context);
                                buzzer = new FeitianBuzzer(context);
                                crypto = new FeitianCrypto(context);
                                keyManager = new FeitianKeyManager(context);
                                
                                // Initialize each component
                                int ret = printer.init();
                                if (ret != 0) {
                                    Log.w(TAG, "Printer init returned: " + ret);
                                }
                                
                                ret = cardReader.init();
                                if (ret != 0) {
                                    Log.w(TAG, "CardReader init returned: " + ret);
                                }
                                
                                ret = pinpad.init();
                                if (ret != 0) {
                                    Log.w(TAG, "Pinpad init returned: " + ret);
                                }
                                
                                ret = device.init();
                                if (ret != 0) {
                                    Log.w(TAG, "Device init returned: " + ret);
                                }
                                
                                ret = led.init();
                                if (ret != 0) {
                                    Log.w(TAG, "LED init returned: " + ret);
                                }
                                
                                ret = buzzer.init();
                                if (ret != 0) {
                                    Log.w(TAG, "Buzzer init returned: " + ret);
                                }
                                
                                ret = crypto.init();
                                if (ret != 0) {
                                    Log.w(TAG, "Crypto init returned: " + ret);
                                }
                                
                                ret = keyManager.init();
                                if (ret != 0) {
                                    Log.w(TAG, "KeyManager init returned: " + ret);
                                }
                                
                                // Set key group name for key manager
                                if (keyManager != null) {
                                    keyManager.setKeyGroupName(context.getPackageName());
                                }
                                
                                initialized = true;
                                Log.d(TAG, "Feitian SDK initialized successfully");
                                callback.onSuccess();
                                
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to initialize Feitian SDK components", e);
                                callback.onError(-1, e.getMessage());
                            }
                        } else if ("onError".equals(methodName)) {
                            int errorCode = args != null && args.length > 0 ? (Integer)args[0] : -1;
                            String errorMsg = args != null && args.length > 1 ? (String)args[1] : "Unknown error";
                            Log.e(TAG, "ServiceManager bind failed: " + errorCode + " - " + errorMsg);
                            callback.onError(errorCode, errorMsg);
                        }
                        return null;
                    }
                }
            );
            
            // Bind to service
            FeitianReflectionHelper.invokeStaticMethod(
                serviceManagerClass,
                "bindPosServer",
                new Class<?>[] { Context.class, callbackClass },
                new Object[] { context, serviceCallback }
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind Feitian service", e);
            callback.onError(-1, "Failed to bind service: " + e.getMessage());
        }
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
        return led;
    }
    
    @Override
    public IBuzzer getBuzzer() {
        return buzzer;
    }
    
    @Override
    public IPsam getPsam() {
        // PSAM not implemented yet
        return null;
    }
    
    @Override
    public ICrypto getCrypto() {
        return crypto;
    }
    
    @Override
    public IDukpt getDukpt() {
        // DUKPT not implemented yet
        return null;
    }
    
    @Override
    public IKeyManager getKeyManager() {
        return keyManager;
    }
    
    @Override
    public ISerialPort getSerialPort() {
        // Serial port not implemented yet
        return null;
    }
    
    @Override
    public IBtScreen getBtScreen() {
        // Bluetooth screen not implemented yet
        return null;
    }
    
    @Override
    public String getSDKName() {
        return "Feitian FTSDK";
    }
    
    @Override
    public String getSDKVersion() {
        try {
            // Try to get SDK version from device
            if (device != null && device instanceof FeitianDevice) {
                Object deviceObj = ((FeitianDevice)device).device;
                if (deviceObj != null) {
                    Object version = FeitianReflectionHelper.invokeMethod(
                        deviceObj, "getSdkVersion", new Class<?>[0], new Object[0]
                    );
                    if (version != null) {
                        return version.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get SDK version", e);
        }
        return "1.0.1.11"; // Default version from JAR name
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
        
        if (led != null) {
            led.release();
            led = null;
        }
        
        if (buzzer != null) {
            buzzer.release();
            buzzer = null;
        }
        
        if (crypto != null) {
            crypto.release();
            crypto = null;
        }
        
        if (keyManager != null) {
            keyManager.release();
            keyManager = null;
        }
        
        // Unbind from service using reflection
        try {
            Class<?> serviceManagerClass = FeitianReflectionHelper.loadClass(
                FeitianReflectionHelper.SERVICE_MANAGER
            );
            
            if (serviceManagerClass != null) {
                FeitianReflectionHelper.invokeStaticMethod(
                    serviceManagerClass,
                    "unbindPosServer",
                    new Class<?>[] { Context.class },
                    new Object[] { context }
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to unbind service", e);
        }
        
        Log.d(TAG, "Feitian SDK released");
    }
}