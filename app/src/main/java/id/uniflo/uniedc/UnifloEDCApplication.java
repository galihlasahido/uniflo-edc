package id.uniflo.uniedc;

import android.app.Application;

import net.sqlcipher.database.SQLiteDatabase;

import id.uniflo.uniedc.security.DatabaseSecurityConfig;

public class UnifloEDCApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize SQLCipher
        SQLiteDatabase.loadLibs(this);
        
        // Configure database security
        DatabaseSecurityConfig.configureDatabaseSecurity(this);
        
        // Perform database integrity check on startup
        performDatabaseIntegrityCheck();
    }
    
    private void performDatabaseIntegrityCheck() {
        try {
            // Check if database is properly secured
            if (!DatabaseSecurityConfig.isDatabaseSecure(this)) {
                // Re-configure security if needed
                DatabaseSecurityConfig.configureDatabaseSecurity(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // Clear any sensitive data from memory
        System.gc();
    }
}