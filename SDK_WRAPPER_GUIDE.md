# SDK Wrapper Architecture Guide

## Overview

The SDK wrapper architecture allows the UniEDC application to work with multiple POS SDK providers (Feitian, PAX, Verifone, etc.) through a unified interface. The application can switch between different SDKs via configuration, and automatically falls back to an emulator mode when running on non-POS devices.

## Architecture Components

### 1. Core Interfaces (id.uniflo.uniedc.sdk.interfaces)

- **IPrinter**: Printer operations interface
- **ICardReader**: Card reader operations (MAG, IC, NFC)
- **IPinpad**: PIN entry and encryption operations
- **IDevice**: Device control (LED, buzzer, battery, etc.)
- **ISDKProvider**: Main SDK provider interface

### 2. SDK Manager (id.uniflo.uniedc.sdk.SDKManager)

Singleton class that manages SDK selection and initialization:
- Auto-detects device type based on manufacturer
- Falls back to emulator mode if real SDK fails
- Persists SDK selection in SharedPreferences
- Provides unified access to all SDK components

### 3. SDK Implementations

#### Feitian SDK (id.uniflo.uniedc.sdk.feitian)
- **FeitianSDKProvider**: Main provider implementation
- **FeitianPrinter**: Wraps Feitian printer APIs
- **FeitianCardReader**: Combines IC, NFC, and MAG readers
- **FeitianPinpad**: Wraps EMV and crypto operations
- **FeitianDevice**: Wraps device control APIs

#### Emulator SDK (id.uniflo.uniedc.sdk.emulator)
- **EmulatorSDKProvider**: Mock implementation for development
- **EmulatorPrinter**: Logs print operations to console
- **EmulatorCardReader**: Simulates card detection with delays
- **EmulatorPinpad**: Simulates PIN entry and crypto operations
- **EmulatorDevice**: Returns mock device information

## Usage Examples

### 1. Initialize SDK on App Startup

```java
// In your main activity or application class
SDKManager sdkManager = SDKManager.getInstance();
sdkManager.init(context);

sdkManager.initializeSDK(new ISDKProvider.IInitCallback() {
    @Override
    public void onSuccess() {
        // SDK ready to use
    }
    
    @Override
    public void onError(int errorCode, String message) {
        // Handle initialization error
        // SDK Manager will automatically fallback to emulator
    }
});
```

### 2. Using the Printer

```java
IPrinter printer = SDKManager.getInstance().getPrinter();
if (printer != null) {
    printer.init();
    printer.setAlignment(1); // Center
    printer.setTextSize(1); // Large
    printer.printText("RECEIPT HEADER\n");
    printer.setAlignment(0); // Left
    printer.setTextSize(0); // Normal
    printer.printText("Item 1: $10.00\n");
    printer.feedPaper(3);
}
```

### 3. Reading Cards

```java
ICardReader cardReader = SDKManager.getInstance().getCardReader();
if (cardReader != null) {
    cardReader.init();
    cardReader.open(ICardReader.CARD_TYPE_MAG | ICardReader.CARD_TYPE_IC | ICardReader.CARD_TYPE_NFC,
                   30, // timeout in seconds
                   new ICardReader.ICardDetectListener() {
        @Override
        public void onCardDetected(int cardType) {
            // Handle card detection
            if (cardType == ICardReader.CARD_TYPE_MAG) {
                String track2 = cardReader.getTrackData(2);
            }
        }
        
        @Override
        public void onTimeout() {
            // Handle timeout
        }
        
        @Override
        public void onError(int errorCode, String message) {
            // Handle error
        }
    });
}
```

### 4. PIN Entry

```java
IPinpad pinpad = SDKManager.getInstance().getPinpad();
if (pinpad != null) {
    pinpad.init();
    pinpad.inputPin("1234567890123456", // PAN
                    6, // max PIN length
                    30, // timeout
                    new IPinpad.IPinInputListener() {
        @Override
        public void onPinEntered(byte[] pinBlock) {
            // Handle encrypted PIN block
        }
        
        @Override
        public void onCancel() {
            // User cancelled
        }
        
        @Override
        public void onTimeout() {
            // Timeout occurred
        }
        
        @Override
        public void onError(int errorCode, String message) {
            // Handle error
        }
    });
}
```

### 5. Device Control

```java
IDevice device = SDKManager.getInstance().getDevice();
if (device != null) {
    device.init();
    
    // Get device info
    String serial = device.getSerialNumber();
    int battery = device.getBatteryLevel();
    
    // Control hardware
    device.beep(200); // 200ms beep
    device.setLed(0, true); // Turn on LED 0
    device.setLedColor(0, 0xFF0000); // Set to red
}
```

## Configuration

### SDK Type Selection

The SDK type can be configured programmatically:

```java
SDKManager.getInstance().setSDKType(SDKType.FEITIAN);
// or
SDKManager.getInstance().setSDKType(SDKType.EMULATOR);
```

### Auto-Detection

The SDK Manager automatically detects the device type based on:
1. Device manufacturer (Build.MANUFACTURER)
2. Device fingerprint (for emulator detection)
3. Falls back to emulator mode if detection fails

## Adding New SDK Providers

To add support for a new SDK (e.g., PAX):

1. Create implementation package: `id.uniflo.uniedc.sdk.pax`
2. Implement the provider: `PaxSDKProvider implements ISDKProvider`
3. Implement component wrappers: `PaxPrinter`, `PaxCardReader`, etc.
4. Add SDK type to `SDKType` enum
5. Update `SDKManager.initializeSDK()` to handle the new type

## Testing

Use the `SDKTestActivity` to test SDK functionality:
- Initialize different SDK types
- Test printer operations
- Test card reader detection
- Test device controls
- Switch between SDK implementations

## Error Handling

The wrapper provides consistent error handling:
- All operations return 0 for success, negative values for errors
- Callbacks include error codes and messages
- Automatic fallback to emulator mode on initialization failure

## Thread Safety

- SDK Manager is a thread-safe singleton
- Callbacks may occur on background threads - use `runOnUiThread()` for UI updates
- SDK components should be accessed from a single thread

  # Clean, build and install
  ./gradlew clean installDebug

  # Or for release
  ./gradlew clean assembleRelease && adb install -r
  app/build/outputs/apk/release/uniedc-V*.apk

  Start the App After Installation

  adb shell am start -n id.uniflo.uniedc/.ui.BalanceInquiryActivityBasic

  The APK will be named: uniedc-V1.0.1.11_00_YYYYMMDD-HH:mm.00.apk

The PIN entry dialog is using:

1. Android's built-in Dialog class - android.app.Dialog
2. With Material theme -
   android.R.style.Theme_Material_Light_Dialog_NoActionBar
3. Custom layout - R.layout.dialog_pin_input

Here's the implementation pattern used across all activities:

// Create modern PIN dialog
Dialog pinDialog = new Dialog(this,
android.R.style.Theme_Material_Light_Dialog_NoActionBar);
pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
pinDialog.setContentView(R.layout.dialog_pin_input);
pinDialog.setCancelable(false);

The dialog uses:
- Layout file: /app/src/main/res/layout/dialog_pin_input.xml
- Standard Android Views:
  - TextView for messages
  - EditText (hidden) for PIN input
  - View elements for PIN dots
  - Button widgets for the number pad (0-9, delete, OK)
- Drawable resources:
  - R.drawable.pin_dot_empty
  - R.drawable.pin_dot_filled

This PIN dialog is now implemented in:
1. BalanceInquiryActivityBasic.java - showPinInputDialog() method
2. PurchaseActivity.java - showModernPinDialog() method
3. TransferActivity.java - showModernPinDialog() method
4. PurchaseActivityEnhanced.java - showPinInputDialog() method

All use the same dialog approach with the standard Android Dialog class
and the custom dialog_pin_input.xml layout, providing a consistent PIN
entry experience across the application.
