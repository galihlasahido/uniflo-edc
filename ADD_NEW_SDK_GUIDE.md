# How to Add a New SDK to UniEDC

This guide explains how to add support for a new POS SDK (e.g., PAX, Verifone, Ingenico) to the UniEDC SDK wrapper architecture.

## Prerequisites

Before starting, ensure you have:
1. The SDK JAR/AAR files from the vendor
2. SDK documentation and API reference
3. Access to a physical device for testing (optional but recommended)

## Step-by-Step Implementation Guide

### Step 1: Add SDK Dependencies

Add the SDK library files to your project:

```bash
# Copy SDK files to the libs directory
cp vendor-sdk.jar app/libs/
cp vendor-crypto.jar app/libs/  # if applicable
```

In `app/build.gradle`, the dependencies are already configured to include all JARs:
```gradle
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    // ... other dependencies
}
```

### Step 2: Update SDK Type Enum

Edit `app/src/main/java/id/uniflo/uniedc/sdk/SDKType.java`:

```java
public enum SDKType {
    FEITIAN("feitian", "Feitian SDK"),
    PAX("pax", "PAX SDK"),              // Add your new SDK here
    VERIFONE("verifone", "Verifone SDK"),
    NEWVENDOR("newvendor", "NewVendor SDK"), // Example
    EMULATOR("emulator", "Emulator/Mock SDK");
    
    // ... rest of the enum implementation
}
```

### Step 3: Create SDK Package Structure

Create a new package for your SDK implementation:

```
app/src/main/java/id/uniflo/uniedc/sdk/newvendor/
├── NewVendorSDKProvider.java
├── NewVendorPrinter.java
├── NewVendorCardReader.java
├── NewVendorPinpad.java
└── NewVendorDevice.java
```

### Step 4: Implement the SDK Provider

Create `NewVendorSDKProvider.java`:

```java
package id.uniflo.uniedc.sdk.newvendor;

import android.content.Context;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.*;

// Import vendor SDK classes
import com.newvendor.sdk.DeviceService;
import com.newvendor.sdk.ServiceCallback;

public class NewVendorSDKProvider implements ISDKProvider {
    
    private static final String TAG = "NewVendorSDK";
    private boolean initialized = false;
    private Context context;
    
    private NewVendorPrinter printer;
    private NewVendorCardReader cardReader;
    private NewVendorPinpad pinpad;
    private NewVendorDevice device;
    
    // Vendor SDK service reference
    private DeviceService vendorService;
    
    @Override
    public void initialize(Context context, IInitCallback callback) {
        this.context = context;
        
        Log.d(TAG, "Initializing NewVendor SDK...");
        
        // Initialize vendor SDK (example pattern)
        vendorService = DeviceService.getInstance();
        vendorService.bindService(context, new ServiceCallback() {
            @Override
            public void onServiceConnected() {
                try {
                    // Initialize wrapper components
                    printer = new NewVendorPrinter(context, vendorService);
                    cardReader = new NewVendorCardReader(context, vendorService);
                    pinpad = new NewVendorPinpad(context, vendorService);
                    device = new NewVendorDevice(context, vendorService);
                    
                    // Initialize each component
                    printer.init();
                    cardReader.init();
                    pinpad.init();
                    device.init();
                    
                    initialized = true;
                    Log.d(TAG, "NewVendor SDK initialized successfully");
                    callback.onSuccess();
                    
                } catch (Exception e) {
                    Log.e(TAG, "Failed to initialize components", e);
                    callback.onError(-1, e.getMessage());
                }
            }
            
            @Override
            public void onServiceDisconnected() {
                Log.e(TAG, "Service disconnected");
                callback.onError(-2, "Service disconnected");
            }
        });
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
    public String getSDKName() {
        return "NewVendor SDK";
    }
    
    @Override
    public String getSDKVersion() {
        if (vendorService != null) {
            return vendorService.getVersion();
        }
        return "Unknown";
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
        
        if (vendorService != null) {
            vendorService.unbindService();
            vendorService = null;
        }
        
        Log.d(TAG, "NewVendor SDK released");
    }
}
```

### Step 5: Implement Component Wrappers

#### Example: NewVendorPrinter.java

```java
package id.uniflo.uniedc.sdk.newvendor;

import android.content.Context;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.IPrinter;

// Import vendor printer classes
import com.newvendor.sdk.printer.Printer;
import com.newvendor.sdk.printer.PrinterCallback;

public class NewVendorPrinter implements IPrinter {
    
    private static final String TAG = "NewVendorPrinter";
    private Context context;
    private Printer vendorPrinter;
    private boolean initialized = false;
    
    public NewVendorPrinter(Context context, DeviceService service) {
        this.context = context;
        this.vendorPrinter = service.getPrinter();
    }
    
    @Override
    public int init() {
        try {
            // Initialize vendor printer
            int result = vendorPrinter.open();
            if (result == 0) { // Assuming 0 is success
                initialized = true;
                Log.d(TAG, "Printer initialized");
                return 0;
            } else {
                Log.e(TAG, "Printer init failed: " + result);
                return -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize printer", e);
            return -1;
        }
    }
    
    @Override
    public int printText(String data) {
        if (!initialized) {
            return -1;
        }
        
        try {
            // Map to vendor API
            vendorPrinter.addText(data);
            
            // Execute print (synchronous or async depending on vendor)
            final int[] result = {-1};
            final Object lock = new Object();
            
            vendorPrinter.start(new PrinterCallback() {
                @Override
                public void onSuccess() {
                    synchronized (lock) {
                        result[0] = 0;
                        lock.notify();
                    }
                }
                
                @Override
                public void onError(int code, String msg) {
                    Log.e(TAG, "Print error: " + code + " - " + msg);
                    synchronized (lock) {
                        result[0] = code;
                        lock.notify();
                    }
                }
            });
            
            // Wait for completion
            synchronized (lock) {
                try {
                    lock.wait(30000); // 30 second timeout
                } catch (InterruptedException e) {
                    return -1;
                }
            }
            
            return result[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to print", e);
            return -1;
        }
    }
    
    @Override
    public int getStatus() {
        if (!initialized) {
            return -2;
        }
        
        try {
            // Map vendor status codes to standard codes
            int vendorStatus = vendorPrinter.getStatus();
            
            switch (vendorStatus) {
                case 0: // Vendor OK status
                    return 0; // Standard OK
                case 1: // Vendor out of paper
                    return 1; // Standard out of paper
                case 2: // Vendor overheat
                    return 2; // Standard overheat
                default:
                    return -2; // Standard error
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get status", e);
            return -2;
        }
    }
    
    // Implement all other IPrinter methods...
    
    @Override
    public void release() {
        if (vendorPrinter != null) {
            try {
                vendorPrinter.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close printer", e);
            }
        }
        initialized = false;
    }
}
```

### Step 6: Update SDK Manager

Edit `app/src/main/java/id/uniflo/uniedc/sdk/SDKManager.java`:

```java
// In the initializeSDK method, add your new SDK case:

switch (currentSDKType) {
    case FEITIAN:
        currentProvider = new FeitianSDKProvider();
        break;
    case PAX:
        currentProvider = new PaxSDKProvider();
        break;
    case NEWVENDOR:  // Add your new vendor here
        currentProvider = new NewVendorSDKProvider();
        break;
    case EMULATOR:
    default:
        currentProvider = new EmulatorSDKProvider();
        break;
}
```

Also update the device detection in `detectDefaultSDKType()`:

```java
private String detectDefaultSDKType() {
    if (isEmulator()) {
        return SDKType.EMULATOR.getCode();
    }
    
    String manufacturer = Build.MANUFACTURER.toLowerCase();
    if (manufacturer.contains("feitian")) {
        return SDKType.FEITIAN.getCode();
    } else if (manufacturer.contains("pax")) {
        return SDKType.PAX.getCode();
    } else if (manufacturer.contains("newvendor")) {  // Add detection
        return SDKType.NEWVENDOR.getCode();
    }
    
    return SDKType.EMULATOR.getCode();
}
```

### Step 7: Handle Vendor-Specific Features

If your vendor SDK has unique features not covered by the standard interfaces:

1. **Option 1**: Extend the interfaces (not recommended as it breaks compatibility)
2. **Option 2**: Add vendor-specific methods to the provider:

```java
public class NewVendorSDKProvider implements ISDKProvider {
    // ... standard implementation
    
    // Vendor-specific feature
    public NewVendorSpecialFeature getSpecialFeature() {
        return specialFeature;
    }
}

// Usage:
ISDKProvider provider = SDKManager.getInstance().getCurrentProvider();
if (provider instanceof NewVendorSDKProvider) {
    NewVendorSpecialFeature feature = ((NewVendorSDKProvider) provider).getSpecialFeature();
    // Use special feature
}
```

### Step 8: Error Code Mapping

Create a mapping between vendor error codes and standard codes:

```java
public class NewVendorErrorMapper {
    
    public static int mapError(int vendorCode) {
        switch (vendorCode) {
            case 0:     return 0;    // Success
            case -100:  return -1;   // General error
            case -200:  return -2;   // Not initialized
            case -300:  return -3;   // Timeout
            case -400:  return -4;   // Cancelled
            default:    return -999; // Unknown error
        }
    }
}
```

### Step 9: Testing

1. **Unit Tests**: Create test classes for each component
2. **Integration Tests**: Test with the actual SDK on device
3. **Emulator Tests**: Ensure fallback to emulator works

Example test activity modification:

```java
// In SDKTestActivity, add option to test new SDK
private void switchSDKType() {
    SDKType currentType = sdkManager.getCurrentSDKType();
    SDKType newType;
    
    switch (currentType) {
        case FEITIAN:
            newType = SDKType.NEWVENDOR;
            break;
        case NEWVENDOR:
            newType = SDKType.EMULATOR;
            break;
        case EMULATOR:
            newType = SDKType.FEITIAN;
            break;
        default:
            newType = SDKType.EMULATOR;
            break;
    }
    
    sdkManager.setSDKType(newType);
    updateStatus();
}
```

### Step 10: Documentation

Update the SDK documentation:

1. Add vendor-specific notes to CLAUDE.md
2. Update SDK_WRAPPER_GUIDE.md with new SDK examples
3. Document any special initialization requirements
4. Note device-specific limitations

## Common Implementation Patterns

### Async to Sync Conversion

Many vendor SDKs use callbacks. Convert to synchronous for consistency:

```java
public int performOperation() {
    final int[] result = {-1};
    final CountDownLatch latch = new CountDownLatch(1);
    
    vendorApi.doAsync(new Callback() {
        @Override
        public void onComplete(int status) {
            result[0] = mapError(status);
            latch.countDown();
        }
    });
    
    try {
        latch.await(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
        return -1;
    }
    
    return result[0];
}
```

### Resource Management

Always clean up resources:

```java
@Override
public void release() {
    // Release in reverse order of initialization
    if (component != null) {
        try {
            component.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to close component", e);
        }
        component = null;
    }
    
    initialized = false;
}
```

### Thread Safety

Ensure thread-safe access:

```java
private final Object lock = new Object();

public int operation() {
    synchronized (lock) {
        // Perform operation
    }
}
```

## Troubleshooting

### Common Issues

1. **ClassNotFoundException**: Ensure SDK JARs are in libs/ directory
2. **Service binding fails**: Check AndroidManifest.xml permissions
3. **Initialization timeout**: Increase timeout or check device state
4. **Crashes on emulator**: Ensure proper null checks and try-catch blocks

### Debugging Tips

1. Enable verbose logging during development
2. Test each component independently
3. Use the SDKTestActivity for quick testing
4. Check vendor SDK sample code for usage patterns

## Checklist

- [ ] Add SDK dependencies to libs/
- [ ] Update SDKType enum
- [ ] Create package structure
- [ ] Implement SDKProvider
- [ ] Implement all component wrappers
- [ ] Update SDKManager switch statement
- [ ] Update device detection logic
- [ ] Map error codes
- [ ] Test on physical device
- [ ] Test emulator fallback
- [ ] Update documentation
- [ ] Add to version control

## Example: Adding PAX SDK

Here's a concrete example for PAX:

1. Copy PAX SDK files:
   - NeptuneLiteApi.jar → app/libs/

2. Create package:
   - id.uniflo.uniedc.sdk.pax/

3. Key PAX API mappings:
   - IDAL → Device access layer
   - IPrinter → Printer interface
   - IPicc → IC card reader
   - IMag → Magnetic card reader
   - IPed → PIN pad

4. Initialize PAX:
   ```java
   NeptuneLiteUser.getInstance().getDal(context)
   ```

Remember to handle PAX-specific requirements like:
- Font file loading for printer
- Key system initialization
- LED index mapping

## Support

For questions about specific vendor SDKs:
1. Consult vendor documentation
2. Check vendor sample applications
3. Contact vendor support
4. Review existing implementations (Feitian) for patterns