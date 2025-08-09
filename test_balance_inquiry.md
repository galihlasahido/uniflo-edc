# Testing Balance Inquiry Directly

To test Balance Inquiry directly without going through login, you can temporarily modify the AndroidManifest.xml:

## Option 1: Change Launcher Activity
Change the launcher activity in AndroidManifest.xml from LoginActivity to BalanceInquiryActivityBasic:

```xml
<!-- Comment out LoginActivity launcher -->
<!--
<activity
    android:name="id.uniflo.uniedc.ui.LoginActivity"
    android:theme="@style/AppTheme.NoActionBar"
    android:windowSoftInputMode="adjustResize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
-->

<!-- Add launcher to BalanceInquiryActivityBasic -->
<activity
    android:name="id.uniflo.uniedc.ui.BalanceInquiryActivityBasic"
    android:theme="@style/AppTheme.NoActionBar"
    android:windowSoftInputMode="adjustResize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

## Option 2: Use ADB Command
You can also launch the activity directly using ADB:

```bash
adb shell am start -n com.ftpos.ftappdemo/id.uniflo.uniedc.ui.BalanceInquiryActivityBasic
```

## Common Errors and Solutions:

1. **Service Connection Failed**
   - Make sure the Feitian service app is installed and running on the device
   - Check that the device is a Feitian terminal

2. **Card Reader Not Initialized**
   - The ServiceManager.bindPosServer() might be failing
   - Check logcat for service binding errors

3. **Power Error (32778)**
   - This usually means the card is not properly powered
   - Try removing and reinserting the card
   - The app has retry logic built in

4. **Card Processing Timeout**
   - The card might not be responding to APDU commands
   - Try a different card
   - Check if the card is EMV compatible

## Debug Tips:
- The dialog shows debug logs in real-time
- Check Android Studio logcat with filter: TAG:BalanceInquiry
- The app logs all APDU communication