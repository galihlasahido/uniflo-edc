# Material Icons Font

To use Material Icons in the app:

1. Download Material Icons font from: https://github.com/google/material-design-icons/blob/master/font/MaterialIcons-Regular.ttf
2. Save the file as `material_icons_regular.ttf` in this directory
3. The file should be at: `app/src/main/res/font/material_icons_regular.ttf`

## Material Icon Names Used

The icons used in the payment activity:
- Shopping Cart: `shopping_cart`
- Block (Void): `block`
- Replay (Refund): `replay`
- Check Circle: `check_circle`
- Lock: `lock`
- Wallet: `account_balance_wallet`

You can find more icon names at: https://fonts.google.com/icons

⏺ To build the APK, you can use one of these commands:

Debug APK (for testing):

./gradlew assembleDebug

Release APK (signed for production):

./gradlew assembleRelease

Clean and build (recommended if you have build issues):

./gradlew clean assembleDebug
or
./gradlew clean assembleRelease

APK Output Location:

- Debug APK:
  app/build/outputs/apk/debug/uniedc-V1.0.1.11_00_20250722.00.apk
- Release APK:
  app/build/outputs/apk/release/uniedc-V1.0.1.11_00_20250722.00.apk

