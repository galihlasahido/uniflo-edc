package id.uniflo.uniedc.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.io.File;
import java.security.MessageDigest;

public class AntiTamperCheck {
    
    // Expected app signature hash (you should update this with your actual release signature)
    private static final String EXPECTED_SIGNATURE = "YOUR_APP_SIGNATURE_HASH";
    
    /**
     * Check if the app is running on a rooted device
     */
    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }
    
    private static boolean checkRootMethod1() {
        String[] paths = {
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        };
        
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean checkRootMethod2() {
        try {
            Process process = Runtime.getRuntime().exec("which su");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean checkRootMethod3() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }
    
    /**
     * Check if the app is running in debug mode
     */
    public static boolean isDebuggable(Context context) {
        return (context.getApplicationInfo().flags & 
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
    
    /**
     * Check if debugger is attached
     */
    public static boolean isDebuggerAttached() {
        return android.os.Debug.isDebuggerConnected() || 
               android.os.Debug.waitingForDebugger();
    }
    
    /**
     * Verify app signature
     */
    public static boolean verifyAppSignature(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            for (Signature signature : packageInfo.signatures) {
                String currentSignature = getSHA256(signature.toByteArray());
                if (EXPECTED_SIGNATURE.equals(currentSignature)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if app is installed from Play Store
     */
    public static boolean isInstalledFromPlayStore(Context context) {
        String installer = context.getPackageManager()
            .getInstallerPackageName(context.getPackageName());
        
        return "com.android.vending".equals(installer) || 
               "com.google.android.feedback".equals(installer);
    }
    
    /**
     * Check for common emulator signs
     */
    public static boolean isRunningOnEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk".equals(Build.PRODUCT)
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator");
    }
    
    /**
     * Check for Xposed Framework
     */
    public static boolean isXposedActive() {
        try {
            throw new Exception();
        } catch (Exception e) {
            for (StackTraceElement element : e.getStackTrace()) {
                String className = element.getClassName();
                if (className.contains("de.robv.android.xposed") ||
                    className.contains("com.saurik.substrate")) {
                    return true;
                }
            }
        }
        
        try {
            ClassLoader.getSystemClassLoader()
                .loadClass("de.robv.android.xposed.XposedHelpers");
            return true;
        } catch (ClassNotFoundException e) {
            // Xposed not found
        }
        
        return false;
    }
    
    /**
     * Get SHA256 hash of byte array
     */
    private static String getSHA256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Perform comprehensive security check
     */
    public static SecurityCheckResult performSecurityCheck(Context context) {
        SecurityCheckResult result = new SecurityCheckResult();
        
        result.isRooted = isDeviceRooted();
        result.isDebuggable = isDebuggable(context);
        result.isDebuggerAttached = isDebuggerAttached();
        result.isEmulator = isRunningOnEmulator();
        result.isXposedActive = isXposedActive();
        result.isSignatureValid = verifyAppSignature(context);
        result.isFromPlayStore = isInstalledFromPlayStore(context);
        
        result.isSecure = !result.isRooted && 
                         !result.isDebuggable && 
                         !result.isDebuggerAttached && 
                         !result.isEmulator && 
                         !result.isXposedActive;
        
        return result;
    }
    
    /**
     * Security check result
     */
    public static class SecurityCheckResult {
        public boolean isRooted;
        public boolean isDebuggable;
        public boolean isDebuggerAttached;
        public boolean isEmulator;
        public boolean isXposedActive;
        public boolean isSignatureValid;
        public boolean isFromPlayStore;
        public boolean isSecure;
        
        @Override
        public String toString() {
            return "SecurityCheckResult{" +
                "isRooted=" + isRooted +
                ", isDebuggable=" + isDebuggable +
                ", isDebuggerAttached=" + isDebuggerAttached +
                ", isEmulator=" + isEmulator +
                ", isXposedActive=" + isXposedActive +
                ", isSignatureValid=" + isSignatureValid +
                ", isFromPlayStore=" + isFromPlayStore +
                ", isSecure=" + isSecure +
                '}';
        }
    }
}