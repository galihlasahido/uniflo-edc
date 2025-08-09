# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android SDK demo application for Feitian's FTSDK (Financial Terminal SDK) that demonstrates hardware features of POS/payment terminal devices. The SDK provides access to card readers, security features, printers, and other hardware components.

### Project Structure

```
id.uniflo.uniedc/
├── app/
│   ├── build.gradle              # App build configuration
│   ├── libs/                     # SDK libraries
│   │   ├── FTSDK_api_V1.0.1.11_20241029.jar
│   │   ├── Crypto_V1.00.03_20220812.jar
│   │   ├── arm64-v8a/           # Native libraries for 64-bit ARM
│   │   └── armeabi-v7a/         # Native libraries for 32-bit ARM
│   └── src/main/
│       ├── java/com/ftpos/ftappdemo/  # Main application code
│       ├── res/                       # Resources (layouts, drawables, etc.)
│       └── AndroidManifest.xml        # App manifest
├── keystore/                     # Release signing keys
├── build.gradle                  # Project build configuration
└── CLAUDE.md                     # This file
```

### Build Configuration

- **Target SDK**: 29 (Android 10)
- **Minimum SDK**: 22 (Android 5.1)
- **Build Tools**: 29.0.2
- **Package Name**: com.ftpos.ftappdemo
- **APK Naming**: `uniedc-V{version}_{date}.00.apk`

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (generates signed APK with custom name)
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install debug build on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented Android tests
./gradlew connectedAndroidTest
```

## Architecture and Key Patterns

### SDK Initialization Flow

1. **Service Binding Required**: All SDK components must be initialized AFTER ServiceManager.bindPosServer() succeeds in MainActivity:
```java
ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
    @Override
    public void onSuccess() {
        // Initialize SDK components here
        led = Led.getInstance(mContext);
        buzzer = Buzzer.getInstance(mContext);
        // etc...
    }
});
```

2. **Component Access**: SDK components are stored as static variables in MainActivity and accessed throughout the app:
```java
MainActivity.led.open();
MainActivity.printer.print(data);
```

3. **KeyManager Special Initialization**: For non-F100 devices, KeyManager requires setKeyGroupName() with package name after instantiation.

### Error Handling Pattern

All SDK methods return integer error codes. Check against ErrCode.ERR_SUCCESS:
```java
int ret = component.operation();
if (ret != ErrCode.ERR_SUCCESS) {
    logMsg("Operation failed" + String.format(" errCode = 0x%x", ret));
    return;
}
```

### Operation Patterns

1. **Synchronous Operations**: Most hardware operations (LED, buzzer, printer) are synchronous
2. **Asynchronous Operations**: Card readers use callbacks for card detection/removal
3. **Resource Management**: Always close() components after use

### Device-Specific Features

- **F100**: Limited features (no key management, buzzer, LED, ring light, printer)
- **F600/F300**: No printer support
- **F360**: Uses ring light instead of LED

### Device Model Detection

The app automatically detects the device model at startup using system properties. Supported models include F100, F300, F360, F600, and others. Device model detection is crucial for enabling/disabling features based on hardware capabilities.

### Key SDK Components

- **Card Readers**: IcReader (EMV), NfcReader, MagReader
- **Security**: KeyManager, Dukpt, Crypto
- **Hardware**: Led/RingLight, Buzzer, Printer
- **Communication**: SerialPort, BtScreen (Bluetooth screen)
- **Storage**: PsamReader, MemoryCard

### Important Development Notes

1. **UI Updates**: Use runOnUiThread() when updating UI from SDK callbacks
2. **Binary Data**: Use BytesUtils for hex string conversions
3. **Progress Dialogs**: Use non-cancelable dialogs for card operations
4. **Permissions**: Requires external storage and internet permissions
5. **Build Configuration**: Uses Android SDK 29, minimum SDK 22
6. **Base Classes**: Activities should extend Activity, not BaseActivity (except MainActivity)
7. **Logging**: Use logMsg() method for consistent result display in activities
8. **Resource Cleanup**: Always call close() on SDK components when done

### Testing Approach

The demo app is primarily for manual testing of hardware features. Each activity demonstrates a specific hardware component with interactive tests. Automated testing is limited due to hardware dependencies.

### Common Tasks

To add a new hardware feature demo:
1. Create new Activity extending Activity (not BaseActivity)
2. Initialize component in onCreate() after getting from MainActivity
3. Follow error handling pattern with ErrCode checks
4. Use logMsg() for consistent result display
5. Add activity to AndroidManifest.xml
6. Add button in activity_main.xml and click handler in MainActivity

### Development Environment Setup

1. **Android Studio**: Use latest stable version
2. **Android SDK**: Install SDK 29 (Android 10)
3. **Device/Emulator**: Physical Feitian device required for hardware testing
4. **Permissions**: Grant storage permissions when prompted

### Release Process

1. **Version Update**: Update versionCode and versionName in app/build.gradle
2. **Build Release**: Run `./gradlew assembleRelease`
3. **APK Location**: Find signed APK in `app/build/outputs/apk/release/`
4. **APK Name Format**: `uniedc-V{version}_{date}.00.apk`
5. **Signing**: Uses keystore at `keystore/feitian/ftdemo.jks` (password: 123456)

### Troubleshooting

1. **Service Connection Failed**: Ensure Feitian service app is installed and running
2. **Component Not Working**: Check device model compatibility
3. **Permission Denied**: Grant required permissions in app settings
4. **Build Errors**: Clean project with `./gradlew clean` then rebuild
5. **Card Reader Issues**: Ensure cards are properly inserted/tapped

### Code Style Guidelines

1. **Naming Conventions**:
   - Activities: `{Feature}Activity` (e.g., PrinterActivity)
   - Layouts: `activity_{feature}.xml`
   - IDs: `{feature}_{element}_{type}` (e.g., printer_print_btn)

2. **Package Structure**:
   - Main package: `com.ftpos.ftappdemo`
   - Utilities: `com.ftpos.ftappdemo.util`

3. **Error Handling**:
   - Always check return codes against ErrCode.ERR_SUCCESS
   - Log errors with formatted hex codes
   - Show user-friendly error messages

### SDK Dependencies

- **FTSDK API**: FTSDK_api_V1.0.1.11_20241029.jar
- **Crypto SDK**: Crypto_V1.00.03_20220812.jar
- **Native Libraries**: libCryptoSDK.so (arm64-v8a, armeabi-v7a)