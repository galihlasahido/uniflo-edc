package id.uniflo.uniedc.managers;

import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import id.uniflo.uniedc.database.SecureSettingsDatabaseHelper;

public class SettingsManager {
    
    private static SettingsManager instance;
    private final Context context;
    private final SecureSettingsDatabaseHelper dbHelper;
    
    private SettingsManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = SecureSettingsDatabaseHelper.getInstance(context);
    }
    
    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context);
        }
        return instance;
    }
    
    /**
     * Get network settings
     */
    public NetworkSettings getNetworkSettings() {
        NetworkSettings settings = new NetworkSettings();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String[] columns = {
            SecureSettingsDatabaseHelper.COLUMN_PRIMARY_HOST,
            SecureSettingsDatabaseHelper.COLUMN_PRIMARY_PORT,
            SecureSettingsDatabaseHelper.COLUMN_SECONDARY_HOST,
            SecureSettingsDatabaseHelper.COLUMN_SECONDARY_PORT,
            SecureSettingsDatabaseHelper.COLUMN_TIMEOUT,
            SecureSettingsDatabaseHelper.COLUMN_USE_SSL,
            SecureSettingsDatabaseHelper.COLUMN_PROTOCOL
        };
        
        Cursor cursor = db.query(
            SecureSettingsDatabaseHelper.TABLE_NETWORK_SETTINGS,
            columns,
            null,
            null,
            null,
            null,
            null,
            "1"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            settings.primaryHost = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRIMARY_HOST));
            settings.primaryPort = cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRIMARY_PORT));
            settings.secondaryHost = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_SECONDARY_HOST));
            settings.secondaryPort = cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_SECONDARY_PORT));
            settings.timeout = cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TIMEOUT));
            settings.useSSL = cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_USE_SSL)) == 1;
            settings.protocol = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PROTOCOL));
            cursor.close();
        }
        
        return settings;
    }
    
    /**
     * Get terminal configuration
     */
    public TerminalConfig getTerminalConfig() {
        TerminalConfig config = new TerminalConfig();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String[] columns = {
            SecureSettingsDatabaseHelper.COLUMN_TERMINAL_ID,
            SecureSettingsDatabaseHelper.COLUMN_MERCHANT_ID,
            SecureSettingsDatabaseHelper.COLUMN_MERCHANT_NAME,
            SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER,
            SecureSettingsDatabaseHelper.COLUMN_ACQUIRING_INSTITUTION_CODE
        };
        
        Cursor cursor = db.query(
            SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG,
            columns,
            null,
            null,
            null,
            null,
            null,
            "1"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            config.terminalId = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TERMINAL_ID));
            config.merchantId = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_ID));
            config.merchantName = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_NAME));
            config.traceNumber = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER));
            config.acquiringInstitutionCode = cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_ACQUIRING_INSTITUTION_CODE));
            cursor.close();
        }
        
        return config;
    }
    
    /**
     * Increment and get next trace number
     */
    public synchronized String getNextTraceNumber() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String currentTrace = "000001";
        
        // Get current trace number
        Cursor cursor = db.query(
            SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG,
            new String[]{SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER},
            null,
            null,
            null,
            null,
            null,
            "1"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            currentTrace = cursor.getString(0);
            cursor.close();
        }
        
        // Increment trace number
        int traceNum = Integer.parseInt(currentTrace);
        traceNum++;
        if (traceNum > 999999) {
            traceNum = 1;
        }
        
        String newTrace = String.format("%06d", traceNum);
        
        // Update database
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER, newTrace);
        values.put(SecureSettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        db.update(
            SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG,
            values,
            null,
            null
        );
        
        return newTrace;
    }
    
    public static class NetworkSettings {
        public String primaryHost = "192.168.1.18";
        public int primaryPort = 9000;
        public String secondaryHost = "192.168.1.101";
        public int secondaryPort = 9000;
        public int timeout = 30;
        public boolean useSSL = true;
        public String protocol = "ISO8583";
    }
    
    public static class TerminalConfig {
        public String terminalId = "12345678";
        public String merchantId = "123456789012345";
        public String merchantName = "Merchant Name";
        public String traceNumber = "000001";
        public String acquiringInstitutionCode = "123456";
    }
}