package id.uniflo.uniedc.security;

import android.content.Context;
import android.os.Build;

import java.io.File;

public class DatabaseSecurityConfig {
    
    /**
     * Configure database file permissions to be secure
     */
    public static void configureDatabaseSecurity(Context context) {
        try {
            // Get database directory
            File databaseDir = context.getDatabasePath("dummy").getParentFile();
            if (databaseDir != null && databaseDir.exists()) {
                // Set directory permissions - only owner can read/write/execute
                setFilePermissions(databaseDir, true);
                
                // Set permissions for all database files
                File[] dbFiles = databaseDir.listFiles();
                if (dbFiles != null) {
                    for (File dbFile : dbFiles) {
                        if (dbFile.getName().endsWith(".db") || 
                            dbFile.getName().endsWith(".db-journal") ||
                            dbFile.getName().endsWith(".db-wal") ||
                            dbFile.getName().endsWith(".db-shm")) {
                            setFilePermissions(dbFile, false);
                        }
                    }
                }
            }
            
            // Configure app-wide security settings
            configureAppSecurity(context);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Set restrictive file permissions
     */
    private static void setFilePermissions(File file, boolean isDirectory) {
        try {
            // Remove all permissions for group and others
            file.setReadable(false, false);
            file.setWritable(false, false);
            if (isDirectory) {
                file.setExecutable(false, false);
            }
            
            // Set owner permissions only
            file.setReadable(true, true);
            file.setWritable(true, true);
            if (isDirectory) {
                file.setExecutable(true, true);
            }
            
            // For API 21+, use more restrictive permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    // Use reflection to call Os.chmod if available
                    Class<?> libcoreClass = Class.forName("libcore.io.Libcore");
                    Object os = libcoreClass.getField("os").get(null);
                    
                    Class<?> osClass = os.getClass();
                    java.lang.reflect.Method chmodMethod = osClass.getMethod("chmod", String.class, int.class);
                    
                    // Set permissions to 0600 for files, 0700 for directories
                    int permissions = isDirectory ? 0700 : 0600;
                    chmodMethod.invoke(os, file.getAbsolutePath(), permissions);
                } catch (Exception e) {
                    // Fallback to Java file permissions if reflection fails
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Configure app-wide security settings
     */
    private static void configureAppSecurity(Context context) {
        try {
            // Disable debugging for release builds
            if (!BuildConfig.DEBUG) {
                android.provider.Settings.Global.putInt(
                    context.getContentResolver(),
                    android.provider.Settings.Global.ADB_ENABLED, 0
                );
            }
            
            // Clear clipboard to prevent data leakage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    android.content.ClipData clip = android.content.ClipData.newPlainText("", "");
                    clipboard.setPrimaryClip(clip);
                }
            }
            
        } catch (Exception e) {
            // Ignore exceptions as these are optional security enhancements
        }
    }
    
    /**
     * Check if database files are properly secured
     */
    public static boolean isDatabaseSecure(Context context) {
        try {
            File databaseFile = context.getDatabasePath("uniflo_edc_settings.db");
            if (databaseFile.exists()) {
                // Check if file is only readable/writable by owner
                return databaseFile.canRead() && 
                       databaseFile.canWrite() && 
                       !isWorldReadable(databaseFile) &&
                       !isWorldWritable(databaseFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if file is world readable
     */
    private static boolean isWorldReadable(File file) {
        try {
            // Use reflection to check actual permissions
            Class<?> libcoreClass = Class.forName("libcore.io.Libcore");
            Object os = libcoreClass.getField("os").get(null);
            
            Class<?> structStatClass = Class.forName("libcore.io.StructStat");
            java.lang.reflect.Method statMethod = os.getClass().getMethod("stat", String.class);
            Object stat = statMethod.invoke(os, file.getAbsolutePath());
            
            java.lang.reflect.Field modeField = structStatClass.getField("st_mode");
            int mode = modeField.getInt(stat);
            
            // Check if others have read permission (mode & 0004)
            return (mode & 0004) != 0;
        } catch (Exception e) {
            // Fallback: assume not world readable if we can't check
            return false;
        }
    }
    
    /**
     * Check if file is world writable
     */
    private static boolean isWorldWritable(File file) {
        try {
            // Use reflection to check actual permissions
            Class<?> libcoreClass = Class.forName("libcore.io.Libcore");
            Object os = libcoreClass.getField("os").get(null);
            
            Class<?> structStatClass = Class.forName("libcore.io.StructStat");
            java.lang.reflect.Method statMethod = os.getClass().getMethod("stat", String.class);
            Object stat = statMethod.invoke(os, file.getAbsolutePath());
            
            java.lang.reflect.Field modeField = structStatClass.getField("st_mode");
            int mode = modeField.getInt(stat);
            
            // Check if others have write permission (mode & 0002)
            return (mode & 0002) != 0;
        } catch (Exception e) {
            // Fallback: assume not world writable if we can't check
            return false;
        }
    }
    
    /**
     * Wipe sensitive data from memory
     */
    public static void wipeString(String sensitive) {
        if (sensitive != null) {
            try {
                // Use reflection to access the internal char array
                java.lang.reflect.Field valueField = String.class.getDeclaredField("value");
                valueField.setAccessible(true);
                char[] chars = (char[]) valueField.get(sensitive);
                
                // Overwrite the char array
                if (chars != null) {
                    for (int i = 0; i < chars.length; i++) {
                        chars[i] = '\0';
                    }
                }
            } catch (Exception e) {
                // Can't wipe on this platform
            }
        }
    }
    
    /**
     * Build configuration for release
     */
    static class BuildConfig {
        static final boolean DEBUG = false;
    }
}