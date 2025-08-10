package id.uniflo.uniedc.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SettingsDatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "uniflo_edc_settings.db";
    private static final int DATABASE_VERSION = 2; // Incremented to trigger database upgrade
    
    // Table names
    public static final String TABLE_NETWORK_SETTINGS = "network_settings";
    public static final String TABLE_PRINTER_SETTINGS = "printer_settings";
    public static final String TABLE_SECURITY_SETTINGS = "security_settings";
    public static final String TABLE_TRANSACTION_LIMITS = "transaction_limits";
    public static final String TABLE_TERMINAL_CONFIG = "terminal_config";
    public static final String TABLE_SETTINGS = "settings"; // Generic key-value settings
    
    // Common columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    
    // Network settings columns
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
    
    // Printer settings columns
    public static final String COLUMN_PRINT_DENSITY = "print_density";
    public static final String COLUMN_PRINT_LOGO = "print_logo";
    public static final String COLUMN_PRINT_MERCHANT_COPY = "print_merchant_copy";
    public static final String COLUMN_PRINT_CUSTOMER_COPY = "print_customer_copy";
    public static final String COLUMN_HEADER_LINE1 = "header_line1";
    public static final String COLUMN_HEADER_LINE2 = "header_line2";
    public static final String COLUMN_FOOTER_LINE1 = "footer_line1";
    public static final String COLUMN_FOOTER_LINE2 = "footer_line2";
    
    // Security settings columns
    public static final String COLUMN_PIN_VERIFICATION = "pin_verification";
    public static final String COLUMN_ADMIN_PIN = "admin_pin";
    public static final String COLUMN_MAX_PIN_ATTEMPTS = "max_pin_attempts";
    public static final String COLUMN_VOID_PASSWORD = "void_password";
    public static final String COLUMN_SETTLEMENT_PASSWORD = "settlement_password";
    public static final String COLUMN_REFUND_PASSWORD = "refund_password";
    public static final String COLUMN_KEY_STATUS = "key_status";
    public static final String COLUMN_LAST_KEY_DOWNLOAD = "last_key_download";
    
    // Transaction limits columns
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
    
    // Terminal config columns
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
    
    // Create table statements
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
        COLUMN_USE_SSL + " INTEGER DEFAULT 0, " +
        COLUMN_KEEP_ALIVE + " INTEGER DEFAULT 0, " +
        COLUMN_PROTOCOL + " TEXT, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    private static final String CREATE_PRINTER_SETTINGS_TABLE = 
        "CREATE TABLE " + TABLE_PRINTER_SETTINGS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_PRINT_DENSITY + " INTEGER, " +
        COLUMN_PRINT_LOGO + " INTEGER DEFAULT 0, " +
        COLUMN_PRINT_MERCHANT_COPY + " INTEGER DEFAULT 0, " +
        COLUMN_PRINT_CUSTOMER_COPY + " INTEGER DEFAULT 0, " +
        COLUMN_HEADER_LINE1 + " TEXT, " +
        COLUMN_HEADER_LINE2 + " TEXT, " +
        COLUMN_FOOTER_LINE1 + " TEXT, " +
        COLUMN_FOOTER_LINE2 + " TEXT, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    private static final String CREATE_SECURITY_SETTINGS_TABLE = 
        "CREATE TABLE " + TABLE_SECURITY_SETTINGS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_PIN_VERIFICATION + " INTEGER DEFAULT 0, " +
        COLUMN_ADMIN_PIN + " TEXT, " +
        COLUMN_MAX_PIN_ATTEMPTS + " INTEGER, " +
        COLUMN_VOID_PASSWORD + " INTEGER DEFAULT 0, " +
        COLUMN_SETTLEMENT_PASSWORD + " INTEGER DEFAULT 0, " +
        COLUMN_REFUND_PASSWORD + " INTEGER DEFAULT 0, " +
        COLUMN_KEY_STATUS + " TEXT, " +
        COLUMN_LAST_KEY_DOWNLOAD + " TEXT, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    private static final String CREATE_TRANSACTION_LIMITS_TABLE = 
        "CREATE TABLE " + TABLE_TRANSACTION_LIMITS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_PURCHASE_LIMIT_ENABLED + " INTEGER DEFAULT 0, " +
        COLUMN_PURCHASE_MIN + " INTEGER, " +
        COLUMN_PURCHASE_MAX + " INTEGER, " +
        COLUMN_WITHDRAWAL_LIMIT_ENABLED + " INTEGER DEFAULT 0, " +
        COLUMN_WITHDRAWAL_MIN + " INTEGER, " +
        COLUMN_WITHDRAWAL_MAX + " INTEGER, " +
        COLUMN_TRANSFER_LIMIT_ENABLED + " INTEGER DEFAULT 0, " +
        COLUMN_TRANSFER_MIN + " INTEGER, " +
        COLUMN_TRANSFER_MAX + " INTEGER, " +
        COLUMN_REFUND_LIMIT_ENABLED + " INTEGER DEFAULT 0, " +
        COLUMN_REFUND_MAX + " INTEGER, " +
        COLUMN_CASH_BACK_LIMIT_ENABLED + " INTEGER DEFAULT 0, " +
        COLUMN_CASH_BACK_MAX + " INTEGER, " +
        COLUMN_DAILY_LIMIT_ENABLED + " INTEGER DEFAULT 0, " +
        COLUMN_DAILY_TRANSACTION_LIMIT + " INTEGER, " +
        COLUMN_DAILY_AMOUNT_LIMIT + " INTEGER, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
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
        COLUMN_TIP_ENABLED + " INTEGER DEFAULT 0, " +
        COLUMN_SIGNATURE_REQUIRED + " INTEGER DEFAULT 0, " +
        COLUMN_OFFLINE_MODE + " INTEGER DEFAULT 0, " +
        COLUMN_BATCH_NUMBER + " TEXT, " +
        COLUMN_TRACE_NUMBER + " TEXT, " +
        COLUMN_INVOICE_NUMBER + " TEXT, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    // Generic key-value settings table
    private static final String CREATE_SETTINGS_TABLE = 
        "CREATE TABLE " + TABLE_SETTINGS + " (" +
        "key TEXT PRIMARY KEY, " +
        "value TEXT, " +
        COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    private static SettingsDatabaseHelper instance;
    
    public static synchronized SettingsDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private SettingsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NETWORK_SETTINGS_TABLE);
        db.execSQL(CREATE_PRINTER_SETTINGS_TABLE);
        db.execSQL(CREATE_SECURITY_SETTINGS_TABLE);
        db.execSQL(CREATE_TRANSACTION_LIMITS_TABLE);
        db.execSQL(CREATE_TERMINAL_CONFIG_TABLE);
        db.execSQL(CREATE_SETTINGS_TABLE); // Create the generic settings table
        
        // Insert default values
        insertDefaultValues(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NETWORK_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRINTER_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURITY_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_LIMITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMINAL_CONFIG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        
        // Create tables again
        onCreate(db);
    }
    
    private void insertDefaultValues(SQLiteDatabase db) {
        // Insert default network settings
        db.execSQL("INSERT INTO " + TABLE_NETWORK_SETTINGS + " (" +
            COLUMN_CONNECTION_TYPE + ", " + COLUMN_PRIMARY_HOST + ", " +
            COLUMN_PRIMARY_PORT + ", " + COLUMN_TIMEOUT + ", " +
            COLUMN_RETRY_COUNT + ", " + COLUMN_USE_SSL + ", " +
            COLUMN_KEEP_ALIVE + ", " + COLUMN_PROTOCOL + ") VALUES (" +
            "'ETHERNET', '192.168.1.18', 8080, 30, 3, 1, 1, 'ISO8583')");
        
        // Insert default printer settings
        db.execSQL("INSERT INTO " + TABLE_PRINTER_SETTINGS + " (" +
            COLUMN_PRINT_DENSITY + ", " + COLUMN_PRINT_LOGO + ", " +
            COLUMN_PRINT_MERCHANT_COPY + ", " + COLUMN_PRINT_CUSTOMER_COPY + ", " +
            COLUMN_HEADER_LINE1 + ", " + COLUMN_HEADER_LINE2 + ", " +
            COLUMN_FOOTER_LINE1 + ", " + COLUMN_FOOTER_LINE2 + ") VALUES (" +
            "50, 1, 1, 1, 'UNIFLO MERCHANT', 'Your trusted payment partner', " +
            "'Thank you for your business', 'www.uniflo.id')");
        
        // Insert default security settings
        db.execSQL("INSERT INTO " + TABLE_SECURITY_SETTINGS + " (" +
            COLUMN_PIN_VERIFICATION + ", " + COLUMN_ADMIN_PIN + ", " +
            COLUMN_MAX_PIN_ATTEMPTS + ", " + COLUMN_VOID_PASSWORD + ", " +
            COLUMN_SETTLEMENT_PASSWORD + ", " + COLUMN_REFUND_PASSWORD + ", " +
            COLUMN_KEY_STATUS + ") VALUES (" +
            "1, '1234', 3, 1, 1, 0, 'Not Active')");
        
        // Insert default transaction limits
        db.execSQL("INSERT INTO " + TABLE_TRANSACTION_LIMITS + " (" +
            COLUMN_PURCHASE_LIMIT_ENABLED + ", " + COLUMN_PURCHASE_MIN + ", " +
            COLUMN_PURCHASE_MAX + ", " + COLUMN_WITHDRAWAL_LIMIT_ENABLED + ", " +
            COLUMN_WITHDRAWAL_MIN + ", " + COLUMN_WITHDRAWAL_MAX + ", " +
            COLUMN_TRANSFER_LIMIT_ENABLED + ", " + COLUMN_TRANSFER_MIN + ", " +
            COLUMN_TRANSFER_MAX + ", " + COLUMN_REFUND_LIMIT_ENABLED + ", " +
            COLUMN_REFUND_MAX + ", " + COLUMN_DAILY_LIMIT_ENABLED + ", " +
            COLUMN_DAILY_TRANSACTION_LIMIT + ", " + COLUMN_DAILY_AMOUNT_LIMIT + ") VALUES (" +
            "1, 10000, 50000000, 1, 50000, 10000000, 1, 10000, 25000000, " +
            "1, 50000000, 1, 100, 100000000)");
        
        // Insert default terminal config
        db.execSQL("INSERT INTO " + TABLE_TERMINAL_CONFIG + " (" +
            COLUMN_TERMINAL_ID + ", " + COLUMN_MERCHANT_ID + ", " +
            COLUMN_MERCHANT_NAME + ", " + COLUMN_MERCHANT_ADDRESS + ", " +
            COLUMN_MERCHANT_CITY + ", " + COLUMN_MERCHANT_PHONE + ", " +
            COLUMN_CURRENCY + ", " + COLUMN_LANGUAGE + ", " +
            COLUMN_DATE_FORMAT + ", " + COLUMN_TIP_ENABLED + ", " +
            COLUMN_SIGNATURE_REQUIRED + ", " + COLUMN_OFFLINE_MODE + ", " +
            COLUMN_BATCH_NUMBER + ", " + COLUMN_TRACE_NUMBER + ", " +
            COLUMN_INVOICE_NUMBER + ") VALUES (" +
            "'12345678', '000000123456789', 'UNIFLO MERCHANT', " +
            "'Jl. Sudirman No. 123', 'Jakarta', '+62 21 1234567', " +
            "'IDR', 'English', 'DD/MM/YYYY', 0, 1, 0, " +
            "'000001', '000001', '000001')");
    }
}