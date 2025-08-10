package id.uniflo.uniedc.database;

import android.content.ContentValues;
import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import id.uniflo.uniedc.security.DatabaseKeyManager;

public class SecureSettingsDatabaseHelper extends SQLiteOpenHelper {
    
    // Database Information
    private static final String DATABASE_NAME = "uniflo_edc_settings.db";
    private static final int DATABASE_VERSION = 2; // Incremented for new column
    
    // Table Names
    public static final String TABLE_NETWORK_SETTINGS = "network_settings";
    public static final String TABLE_PRINTER_SETTINGS = "printer_settings";
    public static final String TABLE_SECURITY_SETTINGS = "security_settings";
    public static final String TABLE_TRANSACTION_LIMITS = "transaction_limits";
    public static final String TABLE_TERMINAL_CONFIG = "terminal_config";
    
    // Common Columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    
    // Network Settings Columns
    public static final String COLUMN_CONNECTION_TYPE = "connection_type";
    public static final String COLUMN_PRIMARY_HOST = "primary_host";
    public static final String COLUMN_PRIMARY_PORT = "primary_port";
    public static final String COLUMN_SECONDARY_HOST = "secondary_host";
    public static final String COLUMN_SECONDARY_PORT = "secondary_port";
    public static final String COLUMN_TIMEOUT = "timeout";
    public static final String COLUMN_RETRY_COUNT = "retry_count";
    public static final String COLUMN_USE_SSL = "use_ssl";
    public static final String COLUMN_KEEP_ALIVE = "keep_alive";
    public static final String COLUMN_PROTOCOL = "protocol";
    
    // Printer Settings Columns
    public static final String COLUMN_PRINT_DENSITY = "print_density";
    public static final String COLUMN_PRINT_LOGO = "print_logo";
    public static final String COLUMN_PRINT_MERCHANT_COPY = "print_merchant_copy";
    public static final String COLUMN_PRINT_CUSTOMER_COPY = "print_customer_copy";
    public static final String COLUMN_HEADER_LINE1 = "header_line1";
    public static final String COLUMN_HEADER_LINE2 = "header_line2";
    public static final String COLUMN_FOOTER_LINE1 = "footer_line1";
    public static final String COLUMN_FOOTER_LINE2 = "footer_line2";
    
    // Security Settings Columns
    public static final String COLUMN_PIN_VERIFICATION = "pin_verification";
    public static final String COLUMN_ADMIN_PIN = "admin_pin";
    public static final String COLUMN_MAX_PIN_ATTEMPTS = "max_pin_attempts";
    public static final String COLUMN_VOID_PASSWORD = "void_password";
    public static final String COLUMN_SETTLEMENT_PASSWORD = "settlement_password";
    public static final String COLUMN_REFUND_PASSWORD = "refund_password";
    public static final String COLUMN_KEY_STATUS = "key_status";
    public static final String COLUMN_LAST_KEY_DOWNLOAD = "last_key_download";
    
    // Transaction Limits Columns
    public static final String COLUMN_PURCHASE_LIMIT_ENABLED = "purchase_limit_enabled";
    public static final String COLUMN_PURCHASE_MIN = "purchase_min";
    public static final String COLUMN_PURCHASE_MAX = "purchase_max";
    public static final String COLUMN_WITHDRAWAL_LIMIT_ENABLED = "withdrawal_limit_enabled";
    public static final String COLUMN_WITHDRAWAL_MIN = "withdrawal_min";
    public static final String COLUMN_WITHDRAWAL_MAX = "withdrawal_max";
    public static final String COLUMN_TRANSFER_LIMIT_ENABLED = "transfer_limit_enabled";
    public static final String COLUMN_TRANSFER_MIN = "transfer_min";
    public static final String COLUMN_TRANSFER_MAX = "transfer_max";
    public static final String COLUMN_REFUND_LIMIT_ENABLED = "refund_limit_enabled";
    public static final String COLUMN_REFUND_MAX = "refund_max";
    public static final String COLUMN_CASH_BACK_LIMIT_ENABLED = "cash_back_limit_enabled";
    public static final String COLUMN_CASH_BACK_MAX = "cash_back_max";
    public static final String COLUMN_DAILY_LIMIT_ENABLED = "daily_limit_enabled";
    public static final String COLUMN_DAILY_TRANSACTION_LIMIT = "daily_transaction_limit";
    public static final String COLUMN_DAILY_AMOUNT_LIMIT = "daily_amount_limit";
    
    // Terminal Config Columns
    public static final String COLUMN_TERMINAL_ID = "terminal_id";
    public static final String COLUMN_MERCHANT_ID = "merchant_id";
    public static final String COLUMN_MERCHANT_NAME = "merchant_name";
    public static final String COLUMN_MERCHANT_ADDRESS = "merchant_address";
    public static final String COLUMN_MERCHANT_CITY = "merchant_city";
    public static final String COLUMN_MERCHANT_PHONE = "merchant_phone";
    public static final String COLUMN_CURRENCY = "currency";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_DATE_FORMAT = "date_format";
    public static final String COLUMN_TIP_ENABLED = "tip_enabled";
    public static final String COLUMN_SIGNATURE_REQUIRED = "signature_required";
    public static final String COLUMN_OFFLINE_MODE = "offline_mode";
    public static final String COLUMN_BATCH_NUMBER = "batch_number";
    public static final String COLUMN_TRACE_NUMBER = "trace_number";
    public static final String COLUMN_INVOICE_NUMBER = "invoice_number";
    public static final String COLUMN_ACQUIRING_INSTITUTION_CODE = "acquiring_institution_code";
    
    private static SecureSettingsDatabaseHelper instance;
    private final Context context;
    private final String databaseKey;
    
    // Create table SQL statements
    private static final String CREATE_NETWORK_SETTINGS_TABLE = 
        "CREATE TABLE " + TABLE_NETWORK_SETTINGS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_CONNECTION_TYPE + " TEXT, " +
        COLUMN_PRIMARY_HOST + " TEXT, " +
        COLUMN_PRIMARY_PORT + " INTEGER, " +
        COLUMN_SECONDARY_HOST + " TEXT, " +
        COLUMN_SECONDARY_PORT + " INTEGER, " +
        COLUMN_TIMEOUT + " INTEGER, " +
        COLUMN_RETRY_COUNT + " INTEGER, " +
        COLUMN_USE_SSL + " INTEGER, " +
        COLUMN_KEEP_ALIVE + " INTEGER, " +
        COLUMN_PROTOCOL + " TEXT, " +
        COLUMN_UPDATED_AT + " INTEGER)";
    
    private static final String CREATE_PRINTER_SETTINGS_TABLE = 
        "CREATE TABLE " + TABLE_PRINTER_SETTINGS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_PRINT_DENSITY + " INTEGER, " +
        COLUMN_PRINT_LOGO + " INTEGER, " +
        COLUMN_PRINT_MERCHANT_COPY + " INTEGER, " +
        COLUMN_PRINT_CUSTOMER_COPY + " INTEGER, " +
        COLUMN_HEADER_LINE1 + " TEXT, " +
        COLUMN_HEADER_LINE2 + " TEXT, " +
        COLUMN_FOOTER_LINE1 + " TEXT, " +
        COLUMN_FOOTER_LINE2 + " TEXT, " +
        COLUMN_UPDATED_AT + " INTEGER)";
    
    private static final String CREATE_SECURITY_SETTINGS_TABLE = 
        "CREATE TABLE " + TABLE_SECURITY_SETTINGS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_PIN_VERIFICATION + " INTEGER, " +
        COLUMN_ADMIN_PIN + " TEXT, " +
        COLUMN_MAX_PIN_ATTEMPTS + " INTEGER, " +
        COLUMN_VOID_PASSWORD + " INTEGER, " +
        COLUMN_SETTLEMENT_PASSWORD + " INTEGER, " +
        COLUMN_REFUND_PASSWORD + " INTEGER, " +
        COLUMN_KEY_STATUS + " TEXT, " +
        COLUMN_LAST_KEY_DOWNLOAD + " TEXT, " +
        COLUMN_UPDATED_AT + " INTEGER)";
    
    private static final String CREATE_TRANSACTION_LIMITS_TABLE = 
        "CREATE TABLE " + TABLE_TRANSACTION_LIMITS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_PURCHASE_LIMIT_ENABLED + " INTEGER, " +
        COLUMN_PURCHASE_MIN + " INTEGER, " +
        COLUMN_PURCHASE_MAX + " INTEGER, " +
        COLUMN_WITHDRAWAL_LIMIT_ENABLED + " INTEGER, " +
        COLUMN_WITHDRAWAL_MIN + " INTEGER, " +
        COLUMN_WITHDRAWAL_MAX + " INTEGER, " +
        COLUMN_TRANSFER_LIMIT_ENABLED + " INTEGER, " +
        COLUMN_TRANSFER_MIN + " INTEGER, " +
        COLUMN_TRANSFER_MAX + " INTEGER, " +
        COLUMN_REFUND_LIMIT_ENABLED + " INTEGER, " +
        COLUMN_REFUND_MAX + " INTEGER, " +
        COLUMN_CASH_BACK_LIMIT_ENABLED + " INTEGER, " +
        COLUMN_CASH_BACK_MAX + " INTEGER, " +
        COLUMN_DAILY_LIMIT_ENABLED + " INTEGER, " +
        COLUMN_DAILY_TRANSACTION_LIMIT + " INTEGER, " +
        COLUMN_DAILY_AMOUNT_LIMIT + " INTEGER, " +
        COLUMN_UPDATED_AT + " INTEGER)";
    
    private static final String CREATE_TERMINAL_CONFIG_TABLE = 
        "CREATE TABLE " + TABLE_TERMINAL_CONFIG + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_TERMINAL_ID + " TEXT, " +
        COLUMN_MERCHANT_ID + " TEXT, " +
        COLUMN_MERCHANT_NAME + " TEXT, " +
        COLUMN_MERCHANT_ADDRESS + " TEXT, " +
        COLUMN_MERCHANT_CITY + " TEXT, " +
        COLUMN_MERCHANT_PHONE + " TEXT, " +
        COLUMN_CURRENCY + " TEXT, " +
        COLUMN_LANGUAGE + " TEXT, " +
        COLUMN_DATE_FORMAT + " TEXT, " +
        COLUMN_TIP_ENABLED + " INTEGER, " +
        COLUMN_SIGNATURE_REQUIRED + " INTEGER, " +
        COLUMN_OFFLINE_MODE + " INTEGER, " +
        COLUMN_BATCH_NUMBER + " TEXT, " +
        COLUMN_TRACE_NUMBER + " TEXT, " +
        COLUMN_INVOICE_NUMBER + " TEXT, " +
        COLUMN_ACQUIRING_INSTITUTION_CODE + " TEXT, " +
        COLUMN_UPDATED_AT + " INTEGER)";
    
    private SecureSettingsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
        
        // Load SQLCipher native libraries
        SQLiteDatabase.loadLibs(context);
        
        // Get database encryption key
        this.databaseKey = DatabaseKeyManager.getInstance(context).getDatabaseKey();
    }
    
    public static synchronized SecureSettingsDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SecureSettingsDatabaseHelper(context);
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(CREATE_NETWORK_SETTINGS_TABLE);
        db.execSQL(CREATE_PRINTER_SETTINGS_TABLE);
        db.execSQL(CREATE_SECURITY_SETTINGS_TABLE);
        db.execSQL(CREATE_TRANSACTION_LIMITS_TABLE);
        db.execSQL(CREATE_TERMINAL_CONFIG_TABLE);
        
        // Insert default values
        insertDefaultValues(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades
        if (oldVersion < 2) {
            // Add acquiring institution code column if it doesn't exist
            try {
                db.execSQL("ALTER TABLE " + TABLE_TERMINAL_CONFIG + 
                          " ADD COLUMN " + COLUMN_ACQUIRING_INSTITUTION_CODE + " TEXT DEFAULT '123456'");
            } catch (Exception e) {
                // Column might already exist or table needs recreation
                // For now, we'll recreate tables
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NETWORK_SETTINGS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRINTER_SETTINGS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURITY_SETTINGS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_LIMITS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMINAL_CONFIG);
                onCreate(db);
            }
        }
    }
    
    public SQLiteDatabase getWritableDatabase() {
        try {
            return super.getWritableDatabase(databaseKey);
        } catch (net.sqlcipher.database.SQLiteException e) {
            // Handle corrupted database
            if (e.getMessage() != null && e.getMessage().contains("file is not a database")) {
                android.util.Log.w("SecureDB", "Database corrupted, attempting recovery: " + e.getMessage());
                return recoverDatabase();
            }
            throw e;
        }
    }
    
    public SQLiteDatabase getReadableDatabase() {
        try {
            return super.getReadableDatabase(databaseKey);
        } catch (net.sqlcipher.database.SQLiteException e) {
            // Handle corrupted database
            if (e.getMessage() != null && e.getMessage().contains("file is not a database")) {
                android.util.Log.w("SecureDB", "Database corrupted, attempting recovery: " + e.getMessage());
                return recoverDatabase();
            }
            throw e;
        }
    }
    
    private SQLiteDatabase recoverDatabase() {
        try {
            // Close any existing connections
            close();
            
            // Delete corrupted database file
            context.deleteDatabase(DATABASE_NAME);
            android.util.Log.i("SecureDB", "Corrupted database deleted");
            
            // Create new database
            SQLiteDatabase db = super.getWritableDatabase(databaseKey);
            android.util.Log.i("SecureDB", "New database created successfully");
            
            return db;
        } catch (Exception e) {
            android.util.Log.e("SecureDB", "Failed to recover database: " + e.getMessage());
            throw new RuntimeException("Unable to recover database", e);
        }
    }
    
    private void insertDefaultValues(SQLiteDatabase db) {
        // Network Settings defaults
        ContentValues networkValues = new ContentValues();
        networkValues.put(COLUMN_CONNECTION_TYPE, "ETHERNET");
        networkValues.put(COLUMN_PRIMARY_HOST, "192.168.1.18");
        networkValues.put(COLUMN_PRIMARY_PORT, 9000);
        networkValues.put(COLUMN_SECONDARY_HOST, "192.168.1.101");
        networkValues.put(COLUMN_SECONDARY_PORT, 9000);
        networkValues.put(COLUMN_TIMEOUT, 30);
        networkValues.put(COLUMN_RETRY_COUNT, 3);
        networkValues.put(COLUMN_USE_SSL, 1);
        networkValues.put(COLUMN_KEEP_ALIVE, 1);
        networkValues.put(COLUMN_PROTOCOL, "ISO8583");
        networkValues.put(COLUMN_UPDATED_AT, System.currentTimeMillis());
        db.insert(TABLE_NETWORK_SETTINGS, null, networkValues);
        
        // Printer Settings defaults
        ContentValues printerValues = new ContentValues();
        printerValues.put(COLUMN_PRINT_DENSITY, 50);
        printerValues.put(COLUMN_PRINT_LOGO, 1);
        printerValues.put(COLUMN_PRINT_MERCHANT_COPY, 1);
        printerValues.put(COLUMN_PRINT_CUSTOMER_COPY, 1);
        printerValues.put(COLUMN_HEADER_LINE1, "UNIFLO EDC");
        printerValues.put(COLUMN_HEADER_LINE2, "Payment Terminal");
        printerValues.put(COLUMN_FOOTER_LINE1, "Thank you for your business");
        printerValues.put(COLUMN_FOOTER_LINE2, "Powered by Uniflo");
        printerValues.put(COLUMN_UPDATED_AT, System.currentTimeMillis());
        db.insert(TABLE_PRINTER_SETTINGS, null, printerValues);
        
        // Security Settings defaults
        ContentValues securityValues = new ContentValues();
        securityValues.put(COLUMN_PIN_VERIFICATION, 1);
        securityValues.put(COLUMN_ADMIN_PIN, hashPin("123456")); // Hash the default PIN
        securityValues.put(COLUMN_MAX_PIN_ATTEMPTS, 3);
        securityValues.put(COLUMN_VOID_PASSWORD, 1);
        securityValues.put(COLUMN_SETTLEMENT_PASSWORD, 1);
        securityValues.put(COLUMN_REFUND_PASSWORD, 1);
        securityValues.put(COLUMN_KEY_STATUS, "Not Active");
        securityValues.put(COLUMN_LAST_KEY_DOWNLOAD, "");
        securityValues.put(COLUMN_UPDATED_AT, System.currentTimeMillis());
        db.insert(TABLE_SECURITY_SETTINGS, null, securityValues);
        
        // Transaction Limits defaults
        ContentValues limitsValues = new ContentValues();
        limitsValues.put(COLUMN_PURCHASE_LIMIT_ENABLED, 1);
        limitsValues.put(COLUMN_PURCHASE_MIN, 10000);
        limitsValues.put(COLUMN_PURCHASE_MAX, 10000000);
        limitsValues.put(COLUMN_WITHDRAWAL_LIMIT_ENABLED, 1);
        limitsValues.put(COLUMN_WITHDRAWAL_MIN, 50000);
        limitsValues.put(COLUMN_WITHDRAWAL_MAX, 5000000);
        limitsValues.put(COLUMN_TRANSFER_LIMIT_ENABLED, 1);
        limitsValues.put(COLUMN_TRANSFER_MIN, 10000);
        limitsValues.put(COLUMN_TRANSFER_MAX, 25000000);
        limitsValues.put(COLUMN_REFUND_LIMIT_ENABLED, 1);
        limitsValues.put(COLUMN_REFUND_MAX, 10000000);
        limitsValues.put(COLUMN_CASH_BACK_LIMIT_ENABLED, 1);
        limitsValues.put(COLUMN_CASH_BACK_MAX, 1000000);
        limitsValues.put(COLUMN_DAILY_LIMIT_ENABLED, 1);
        limitsValues.put(COLUMN_DAILY_TRANSACTION_LIMIT, 500);
        limitsValues.put(COLUMN_DAILY_AMOUNT_LIMIT, 100000000);
        limitsValues.put(COLUMN_UPDATED_AT, System.currentTimeMillis());
        db.insert(TABLE_TRANSACTION_LIMITS, null, limitsValues);
        
        // Terminal Config defaults
        ContentValues configValues = new ContentValues();
        configValues.put(COLUMN_TERMINAL_ID, "12345678");
        configValues.put(COLUMN_MERCHANT_ID, "123456789012345");
        configValues.put(COLUMN_MERCHANT_NAME, "Merchant Name");
        configValues.put(COLUMN_MERCHANT_ADDRESS, "Merchant Address");
        configValues.put(COLUMN_MERCHANT_CITY, "Jakarta");
        configValues.put(COLUMN_MERCHANT_PHONE, "021-1234567");
        configValues.put(COLUMN_CURRENCY, "IDR");
        configValues.put(COLUMN_LANGUAGE, "English");
        configValues.put(COLUMN_DATE_FORMAT, "DD/MM/YYYY");
        configValues.put(COLUMN_TIP_ENABLED, 0);
        configValues.put(COLUMN_SIGNATURE_REQUIRED, 1);
        configValues.put(COLUMN_OFFLINE_MODE, 0);
        configValues.put(COLUMN_BATCH_NUMBER, "000001");
        configValues.put(COLUMN_TRACE_NUMBER, "000001");
        configValues.put(COLUMN_INVOICE_NUMBER, "000001");
        configValues.put(COLUMN_ACQUIRING_INSTITUTION_CODE, "123456");
        configValues.put(COLUMN_UPDATED_AT, System.currentTimeMillis());
        db.insert(TABLE_TERMINAL_CONFIG, null, configValues);
    }
    
    /**
     * Hash PIN using SHA-256
     */
    private String hashPin(String pin) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return pin; // Fallback to plain text if hashing fails
        }
    }
    
    /**
     * Perform database integrity check
     */
    public boolean performIntegrityCheck() {
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("PRAGMA integrity_check", null);
        boolean isValid = false;
        
        if (cursor != null && cursor.moveToFirst()) {
            String result = cursor.getString(0);
            isValid = "ok".equalsIgnoreCase(result);
            cursor.close();
        }
        
        return isValid;
    }
    
    /**
     * Re-encrypt database with new key
     */
    public void reEncryptDatabase(String newKey) {
        SQLiteDatabase db = getWritableDatabase();
        db.rawExecSQL("PRAGMA rekey = '" + newKey + "'");
    }
}