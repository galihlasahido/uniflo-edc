package id.uniflo.uniedc.database;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import id.uniflo.uniedc.security.DatabaseKeyManager;

public class TransactionDatabaseHelper extends SQLiteOpenHelper {
    
    // Database Information
    private static final String DATABASE_NAME = "uniflo_edc_transactions.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table Name
    public static final String TABLE_TRANSACTIONS = "transactions";
    
    // Column names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CARD_NUMBER = "card_number";
    public static final String COLUMN_CARD_HOLDER_NAME = "card_holder_name";
    public static final String COLUMN_CARD_TYPE = "card_type";
    public static final String COLUMN_ENTRY_MODE = "entry_mode";
    public static final String COLUMN_TERMINAL_ID = "terminal_id";
    public static final String COLUMN_MERCHANT_ID = "merchant_id";
    public static final String COLUMN_BATCH_NUMBER = "batch_number";
    public static final String COLUMN_TRACE_NUMBER = "trace_number";
    public static final String COLUMN_REFERENCE_NUMBER = "reference_number";
    public static final String COLUMN_APPROVAL_CODE = "approval_code";
    public static final String COLUMN_RESPONSE_CODE = "response_code";
    public static final String COLUMN_RESPONSE_MESSAGE = "response_message";
    public static final String COLUMN_EMV_DATA = "emv_data";
    public static final String COLUMN_PIN_BLOCK = "pin_block";
    public static final String COLUMN_ARQC = "arqc";
    public static final String COLUMN_ATC = "atc";
    public static final String COLUMN_TVR = "tvr";
    public static final String COLUMN_TSI = "tsi";
    public static final String COLUMN_AID = "aid";
    public static final String COLUMN_APPLICATION_LABEL = "application_label";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_IS_VOIDED = "is_voided";
    public static final String COLUMN_VOID_REFERENCE_NUMBER = "void_reference_number";
    public static final String COLUMN_VOID_DATE = "void_date";
    public static final String COLUMN_RAW_REQUEST = "raw_request";
    public static final String COLUMN_RAW_RESPONSE = "raw_response";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    
    private static TransactionDatabaseHelper instance;
    private final Context context;
    private final String databaseKey;
    
    // Create table SQL statement
    private static final String CREATE_TRANSACTIONS_TABLE = 
        "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL, " +
        COLUMN_STATUS + " TEXT NOT NULL, " +
        COLUMN_AMOUNT + " INTEGER NOT NULL, " +
        COLUMN_CARD_NUMBER + " TEXT, " +
        COLUMN_CARD_HOLDER_NAME + " TEXT, " +
        COLUMN_CARD_TYPE + " TEXT, " +
        COLUMN_ENTRY_MODE + " TEXT, " +
        COLUMN_TERMINAL_ID + " TEXT, " +
        COLUMN_MERCHANT_ID + " TEXT, " +
        COLUMN_BATCH_NUMBER + " TEXT, " +
        COLUMN_TRACE_NUMBER + " TEXT, " +
        COLUMN_REFERENCE_NUMBER + " TEXT UNIQUE, " +
        COLUMN_APPROVAL_CODE + " TEXT, " +
        COLUMN_RESPONSE_CODE + " TEXT, " +
        COLUMN_RESPONSE_MESSAGE + " TEXT, " +
        COLUMN_EMV_DATA + " TEXT, " +
        COLUMN_PIN_BLOCK + " TEXT, " +
        COLUMN_ARQC + " TEXT, " +
        COLUMN_ATC + " TEXT, " +
        COLUMN_TVR + " TEXT, " +
        COLUMN_TSI + " TEXT, " +
        COLUMN_AID + " TEXT, " +
        COLUMN_APPLICATION_LABEL + " TEXT, " +
        COLUMN_TRANSACTION_DATE + " INTEGER NOT NULL, " +
        COLUMN_IS_VOIDED + " INTEGER DEFAULT 0, " +
        COLUMN_VOID_REFERENCE_NUMBER + " TEXT, " +
        COLUMN_VOID_DATE + " INTEGER, " +
        COLUMN_RAW_REQUEST + " TEXT, " +
        COLUMN_RAW_RESPONSE + " TEXT, " +
        COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
        COLUMN_UPDATED_AT + " INTEGER NOT NULL)";
    
    // Create indexes for performance
    private static final String CREATE_INDEX_TRANSACTION_DATE = 
        "CREATE INDEX idx_transaction_date ON " + TABLE_TRANSACTIONS + " (" + COLUMN_TRANSACTION_DATE + ")";
    
    private static final String CREATE_INDEX_REFERENCE_NUMBER = 
        "CREATE INDEX idx_reference_number ON " + TABLE_TRANSACTIONS + " (" + COLUMN_REFERENCE_NUMBER + ")";
    
    private static final String CREATE_INDEX_BATCH_NUMBER = 
        "CREATE INDEX idx_batch_number ON " + TABLE_TRANSACTIONS + " (" + COLUMN_BATCH_NUMBER + ")";
    
    private static final String CREATE_INDEX_CARD_NUMBER = 
        "CREATE INDEX idx_card_number ON " + TABLE_TRANSACTIONS + " (" + COLUMN_CARD_NUMBER + ")";
    
    private static final String CREATE_INDEX_STATUS = 
        "CREATE INDEX idx_status ON " + TABLE_TRANSACTIONS + " (" + COLUMN_STATUS + ")";
    
    private static final String CREATE_INDEX_TYPE = 
        "CREATE INDEX idx_type ON " + TABLE_TRANSACTIONS + " (" + COLUMN_TRANSACTION_TYPE + ")";
    
    private TransactionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
        
        // Load SQLCipher native libraries
        SQLiteDatabase.loadLibs(context);
        
        // Get database encryption key
        this.databaseKey = DatabaseKeyManager.getInstance(context).getDatabaseKey();
    }
    
    public static synchronized TransactionDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionDatabaseHelper(context);
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create transactions table
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        
        // Create indexes for better performance
        db.execSQL(CREATE_INDEX_TRANSACTION_DATE);
        db.execSQL(CREATE_INDEX_REFERENCE_NUMBER);
        db.execSQL(CREATE_INDEX_BATCH_NUMBER);
        db.execSQL(CREATE_INDEX_CARD_NUMBER);
        db.execSQL(CREATE_INDEX_STATUS);
        db.execSQL(CREATE_INDEX_TYPE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades here
        // For now, we'll recreate the table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }
    
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase(databaseKey);
    }
    
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase(databaseKey);
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
     * Get database statistics
     */
    public DatabaseStats getDatabaseStats() {
        SQLiteDatabase db = getReadableDatabase();
        DatabaseStats stats = new DatabaseStats();
        
        // Get total transaction count
        android.database.Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS, null);
        if (countCursor != null && countCursor.moveToFirst()) {
            stats.setTotalTransactions(countCursor.getInt(0));
            countCursor.close();
        }
        
        // Get database size
        android.database.Cursor sizeCursor = db.rawQuery("SELECT page_count * page_size as size FROM pragma_page_count(), pragma_page_size()", null);
        if (sizeCursor != null && sizeCursor.moveToFirst()) {
            stats.setDatabaseSize(sizeCursor.getLong(0));
            sizeCursor.close();
        }
        
        return stats;
    }
    
    /**
     * Vacuum database to optimize storage
     */
    public void vacuumDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("VACUUM");
    }
    
    /**
     * Inner class for database statistics
     */
    public static class DatabaseStats {
        private int totalTransactions;
        private long databaseSize;
        
        public int getTotalTransactions() {
            return totalTransactions;
        }
        
        public void setTotalTransactions(int totalTransactions) {
            this.totalTransactions = totalTransactions;
        }
        
        public long getDatabaseSize() {
            return databaseSize;
        }
        
        public void setDatabaseSize(long databaseSize) {
            this.databaseSize = databaseSize;
        }
        
        public String getFormattedSize() {
            if (databaseSize < 1024) {
                return databaseSize + " B";
            } else if (databaseSize < 1024 * 1024) {
                return String.format("%.2f KB", databaseSize / 1024.0);
            } else {
                return String.format("%.2f MB", databaseSize / (1024.0 * 1024.0));
            }
        }
    }
}