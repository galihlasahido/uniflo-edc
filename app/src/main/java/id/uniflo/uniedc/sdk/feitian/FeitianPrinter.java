package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import id.uniflo.uniedc.sdk.interfaces.IPrinter;

/**
 * Feitian Printer Wrapper using Reflection
 */
public class FeitianPrinter implements IPrinter {
    
    private static final String TAG = "FeitianPrinter";
    private Object printer;
    private Context context;
    private boolean initialized = false;
    
    public FeitianPrinter(Context context) {
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
            
            // Load printer class
            Class<?> printerClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.PRINTER);
            if (printerClass == null) {
                Log.e(TAG, "Printer class not found");
                return -1;
            }
            
            // Get instance
            printer = FeitianReflectionHelper.invokeStaticMethod(
                printerClass, 
                "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            if (printer == null) {
                Log.e(TAG, "Failed to get Printer instance");
                return -1;
            }
            
            // Open printer
            Object ret = FeitianReflectionHelper.invokeMethod(
                printer, 
                "open", 
                new Class<?>[0], 
                new Object[0]
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to open printer: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            initialized = true;
            Log.d(TAG, "Printer initialized successfully");
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize printer", e);
            return -1;
        }
    }
    
    @Override
    public int printText(String data) {
        if (!initialized || printer == null) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        try {
            // Start caching print data
            FeitianReflectionHelper.invokeMethod(
                printer,
                "startCaching",
                new Class<?>[0],
                new Object[0]
            );
            
            // Print the text
            Object ret = FeitianReflectionHelper.invokeMethod(
                printer,
                "printStr",
                new Class<?>[] { String.class },
                new Object[] { data }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to print text: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            // Create callback using dynamic proxy
            final int[] result = {-1};
            final Object lock = new Object();
            
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.printer.OnPrinterCallback"
            );
            
            if (callbackClass != null) {
                Object callback = Proxy.newProxyInstance(
                    callbackClass.getClassLoader(),
                    new Class<?>[] { callbackClass },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String methodName = method.getName();
                            
                            if ("onSuccess".equals(methodName)) {
                                synchronized (lock) {
                                    result[0] = 0;
                                    lock.notify();
                                }
                            } else if ("onError".equals(methodName)) {
                                int errorCode = args != null && args.length > 0 ? (Integer)args[0] : -1;
                                String errorMsg = args != null && args.length > 1 ? (String)args[1] : "Unknown error";
                                Log.e(TAG, "Print error: " + errorCode + " - " + errorMsg);
                                synchronized (lock) {
                                    result[0] = errorCode;
                                    lock.notify();
                                }
                            }
                            return null;
                        }
                    }
                );
                
                // Execute print
                FeitianReflectionHelper.invokeMethod(
                    printer,
                    "print",
                    new Class<?>[] { callbackClass },
                    new Object[] { callback }
                );
                
                // Wait for print to complete (with timeout)
                synchronized (lock) {
                    try {
                        lock.wait(30000); // 30 second timeout
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Print interrupted", e);
                        return -1;
                    }
                }
            } else {
                Log.e(TAG, "Callback class not found, trying synchronous print");
                return 0; // Assume success if callback not available
            }
            
            return result[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to print text", e);
            return -1;
        }
    }
    
    @Override
    public int printData(byte[] data) {
        if (data == null) {
            return -1;
        }
        return printText(new String(data));
    }
    
    @Override
    public int feedPaper(int lines) {
        if (!initialized || printer == null) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        try {
            // Start caching
            FeitianReflectionHelper.invokeMethod(
                printer,
                "startCaching",
                new Class<?>[0],
                new Object[0]
            );
            
            // Feed lines
            Object ret = FeitianReflectionHelper.invokeMethod(
                printer,
                "feed",
                new Class<?>[] { int.class },
                new Object[] { lines }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to feed paper: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            // For simplicity, assume synchronous operation
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to feed paper", e);
            return -1;
        }
    }
    
    @Override
    public int getStatus() {
        if (!initialized || printer == null) {
            return -2; // Error
        }
        
        try {
            // For now, return OK status
            // Proper implementation would need PrintStatus class via reflection
            return 0; // OK
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get printer status", e);
            return -2;
        }
    }
    
    @Override
    public int setAlignment(int align) {
        if (!initialized || printer == null) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        try {
            // Load AlignStyle enum
            Class<?> alignStyleClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.printer.AlignStyle"
            );
            
            if (alignStyleClass != null && alignStyleClass.isEnum()) {
                Object[] enumConstants = alignStyleClass.getEnumConstants();
                Object style = null;
                
                switch (align) {
                    case 0: // LEFT
                        style = enumConstants[0];
                        break;
                    case 1: // CENTER
                        style = enumConstants[1];
                        break;
                    case 2: // RIGHT
                        style = enumConstants[2];
                        break;
                    default:
                        Log.e(TAG, "Invalid alignment: " + align);
                        return -1;
                }
                
                Object ret = FeitianReflectionHelper.invokeMethod(
                    printer,
                    "setAlignStyle",
                    new Class<?>[] { alignStyleClass },
                    new Object[] { style }
                );
                
                if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                    Log.e(TAG, "Failed to set alignment: " + ret);
                    return ret != null ? (Integer)ret : -1;
                }
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set alignment", e);
            return -1;
        }
    }
    
    @Override
    public int setTextSize(int size) {
        if (!initialized || printer == null) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        try {
            Bundle params = new Bundle();
            
            // Map size to Feitian font sizes
            int fontSize;
            switch (size) {
                case 0:
                    fontSize = 16; // Small/Normal
                    break;
                case 1:
                    fontSize = 24; // Large
                    break;
                case 2:
                    fontSize = 32; // Extra Large
                    break;
                default:
                    Log.e(TAG, "Invalid text size: " + size);
                    return -1;
            }
            
            params.putInt("font_size", fontSize);
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                printer,
                "setFont",
                new Class<?>[] { Bundle.class },
                new Object[] { params }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to set text size: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set text size", e);
            return -1;
        }
    }
    
    @Override
    public int setBold(boolean bold) {
        if (!initialized || printer == null) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        try {
            Bundle params = new Bundle();
            params.putBoolean("font_bold", bold);
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                printer,
                "setFont",
                new Class<?>[] { Bundle.class },
                new Object[] { params }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to set bold: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to set bold", e);
            return -1;
        }
    }
    
    @Override
    public int printBarcode(String data, int type) {
        if (!initialized || printer == null) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        try {
            // Start caching
            FeitianReflectionHelper.invokeMethod(
                printer,
                "startCaching",
                new Class<?>[0],
                new Object[0]
            );
            
            // For now, print as text with barcode-like formatting
            Object ret = FeitianReflectionHelper.invokeMethod(
                printer,
                "printStr",
                new Class<?>[] { String.class },
                new Object[] { "||||||" + data + "||||||" }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to print barcode: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to print barcode", e);
            return -1;
        }
    }
    
    @Override
    public int printQRCode(String data, int size) {
        if (!initialized || printer == null) {
            Log.e(TAG, "Printer not initialized");
            return -1;
        }
        
        try {
            // Start caching
            FeitianReflectionHelper.invokeMethod(
                printer,
                "startCaching",
                new Class<?>[0],
                new Object[0]
            );
            
            // Set up QR code parameters
            Bundle params = new Bundle();
            params.putInt("qr_size", size);
            params.putInt("qr_error_level", 2); // Error correction level M
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                printer,
                "printQRCodeEx",
                new Class<?>[] { byte[].class, Bundle.class },
                new Object[] { data.getBytes(), params }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to print QR code: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to print QR code", e);
            return -1;
        }
    }
    
    @Override
    public int cutPaper() {
        // Feitian printers don't have paper cutter
        // Just feed some lines
        return feedPaper(5);
    }
    
    @Override
    public void release() {
        if (printer != null) {
            try {
                FeitianReflectionHelper.invokeMethod(
                    printer,
                    "close",
                    new Class<?>[0],
                    new Object[0]
                );
            } catch (Exception e) {
                Log.e(TAG, "Failed to close printer", e);
            }
            printer = null;
        }
        initialized = false;
        Log.d(TAG, "Printer released");
    }
}