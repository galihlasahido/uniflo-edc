package id.uniflo.uniedc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SettingsDAO {
    
    private SettingsDatabaseHelper dbHelper;
    private Context context;
    
    public SettingsDAO(Context context) {
        this.context = context;
        this.dbHelper = SettingsDatabaseHelper.getInstance(context);
    }
    
    // Network Settings Methods
    public NetworkSettings getNetworkSettings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_NETWORK_SETTINGS,
            null, null, null, null, null, null, "1");
        
        NetworkSettings settings = null;
        if (cursor != null && cursor.moveToFirst()) {
            settings = new NetworkSettings();
            settings.setConnectionType(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_CONNECTION_TYPE)));
            settings.setPrimaryHost(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PRIMARY_HOST)));
            settings.setPrimaryPort(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PRIMARY_PORT)));
            settings.setSecondaryHost(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_SECONDARY_HOST)));
            settings.setSecondaryPort(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_SECONDARY_PORT)));
            settings.setTimeout(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_TIMEOUT)));
            settings.setRetryCount(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_RETRY_COUNT)));
            settings.setUseSsl(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_USE_SSL)) == 1);
            settings.setKeepAlive(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_KEEP_ALIVE)) == 1);
            settings.setProtocol(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PROTOCOL)));
            cursor.close();
        }
        
        return settings;
    }
    
    public boolean saveNetworkSettings(NetworkSettings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SettingsDatabaseHelper.COLUMN_CONNECTION_TYPE, settings.getConnectionType());
        values.put(SettingsDatabaseHelper.COLUMN_PRIMARY_HOST, settings.getPrimaryHost());
        values.put(SettingsDatabaseHelper.COLUMN_PRIMARY_PORT, settings.getPrimaryPort());
        values.put(SettingsDatabaseHelper.COLUMN_SECONDARY_HOST, settings.getSecondaryHost());
        values.put(SettingsDatabaseHelper.COLUMN_SECONDARY_PORT, settings.getSecondaryPort());
        values.put(SettingsDatabaseHelper.COLUMN_TIMEOUT, settings.getTimeout());
        values.put(SettingsDatabaseHelper.COLUMN_RETRY_COUNT, settings.getRetryCount());
        values.put(SettingsDatabaseHelper.COLUMN_USE_SSL, settings.isUseSsl() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_KEEP_ALIVE, settings.isKeepAlive() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_PROTOCOL, settings.getProtocol());
        values.put(SettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SettingsDatabaseHelper.TABLE_NETWORK_SETTINGS, values, null, null);
        return rows > 0;
    }
    
    // Printer Settings Methods
    public PrinterSettings getPrinterSettings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_PRINTER_SETTINGS,
            null, null, null, null, null, null, "1");
        
        PrinterSettings settings = null;
        if (cursor != null && cursor.moveToFirst()) {
            settings = new PrinterSettings();
            settings.setPrintDensity(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PRINT_DENSITY)));
            settings.setPrintLogo(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PRINT_LOGO)) == 1);
            settings.setPrintMerchantCopy(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PRINT_MERCHANT_COPY)) == 1);
            settings.setPrintCustomerCopy(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PRINT_CUSTOMER_COPY)) == 1);
            settings.setHeaderLine1(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_HEADER_LINE1)));
            settings.setHeaderLine2(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_HEADER_LINE2)));
            settings.setFooterLine1(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_FOOTER_LINE1)));
            settings.setFooterLine2(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_FOOTER_LINE2)));
            cursor.close();
        }
        
        return settings;
    }
    
    public boolean savePrinterSettings(PrinterSettings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SettingsDatabaseHelper.COLUMN_PRINT_DENSITY, settings.getPrintDensity());
        values.put(SettingsDatabaseHelper.COLUMN_PRINT_LOGO, settings.isPrintLogo() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_PRINT_MERCHANT_COPY, settings.isPrintMerchantCopy() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_PRINT_CUSTOMER_COPY, settings.isPrintCustomerCopy() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_HEADER_LINE1, settings.getHeaderLine1());
        values.put(SettingsDatabaseHelper.COLUMN_HEADER_LINE2, settings.getHeaderLine2());
        values.put(SettingsDatabaseHelper.COLUMN_FOOTER_LINE1, settings.getFooterLine1());
        values.put(SettingsDatabaseHelper.COLUMN_FOOTER_LINE2, settings.getFooterLine2());
        values.put(SettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SettingsDatabaseHelper.TABLE_PRINTER_SETTINGS, values, null, null);
        return rows > 0;
    }
    
    // Security Settings Methods
    public SecuritySettings getSecuritySettings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_SECURITY_SETTINGS,
            null, null, null, null, null, null, "1");
        
        SecuritySettings settings = null;
        if (cursor != null && cursor.moveToFirst()) {
            settings = new SecuritySettings();
            settings.setPinVerification(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PIN_VERIFICATION)) == 1);
            settings.setAdminPin(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_ADMIN_PIN)));
            settings.setMaxPinAttempts(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_MAX_PIN_ATTEMPTS)));
            settings.setVoidPassword(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_VOID_PASSWORD)) == 1);
            settings.setSettlementPassword(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_SETTLEMENT_PASSWORD)) == 1);
            settings.setRefundPassword(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_REFUND_PASSWORD)) == 1);
            settings.setKeyStatus(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_KEY_STATUS)));
            settings.setLastKeyDownload(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_LAST_KEY_DOWNLOAD)));
            cursor.close();
        }
        
        return settings;
    }
    
    public boolean saveSecuritySettings(SecuritySettings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SettingsDatabaseHelper.COLUMN_PIN_VERIFICATION, settings.isPinVerification() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_ADMIN_PIN, settings.getAdminPin());
        values.put(SettingsDatabaseHelper.COLUMN_MAX_PIN_ATTEMPTS, settings.getMaxPinAttempts());
        values.put(SettingsDatabaseHelper.COLUMN_VOID_PASSWORD, settings.isVoidPassword() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_SETTLEMENT_PASSWORD, settings.isSettlementPassword() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_REFUND_PASSWORD, settings.isRefundPassword() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_KEY_STATUS, settings.getKeyStatus());
        values.put(SettingsDatabaseHelper.COLUMN_LAST_KEY_DOWNLOAD, settings.getLastKeyDownload());
        values.put(SettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SettingsDatabaseHelper.TABLE_SECURITY_SETTINGS, values, null, null);
        return rows > 0;
    }
    
    // Transaction Limits Methods
    public TransactionLimits getTransactionLimits() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_TRANSACTION_LIMITS,
            null, null, null, null, null, null, "1");
        
        TransactionLimits limits = null;
        if (cursor != null && cursor.moveToFirst()) {
            limits = new TransactionLimits();
            limits.setPurchaseLimitEnabled(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PURCHASE_LIMIT_ENABLED)) == 1);
            limits.setPurchaseMin(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PURCHASE_MIN)));
            limits.setPurchaseMax(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_PURCHASE_MAX)));
            limits.setWithdrawalLimitEnabled(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_WITHDRAWAL_LIMIT_ENABLED)) == 1);
            limits.setWithdrawalMin(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_WITHDRAWAL_MIN)));
            limits.setWithdrawalMax(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_WITHDRAWAL_MAX)));
            limits.setTransferLimitEnabled(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_TRANSFER_LIMIT_ENABLED)) == 1);
            limits.setTransferMin(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_TRANSFER_MIN)));
            limits.setTransferMax(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_TRANSFER_MAX)));
            limits.setRefundLimitEnabled(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_REFUND_LIMIT_ENABLED)) == 1);
            limits.setRefundMax(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_REFUND_MAX)));
            limits.setCashBackLimitEnabled(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_CASH_BACK_LIMIT_ENABLED)) == 1);
            limits.setCashBackMax(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_CASH_BACK_MAX)));
            limits.setDailyLimitEnabled(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_DAILY_LIMIT_ENABLED)) == 1);
            limits.setDailyTransactionLimit(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_DAILY_TRANSACTION_LIMIT)));
            limits.setDailyAmountLimit(cursor.getLong(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_DAILY_AMOUNT_LIMIT)));
            cursor.close();
        }
        
        return limits;
    }
    
    public boolean saveTransactionLimits(TransactionLimits limits) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SettingsDatabaseHelper.COLUMN_PURCHASE_LIMIT_ENABLED, limits.isPurchaseLimitEnabled() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_PURCHASE_MIN, limits.getPurchaseMin());
        values.put(SettingsDatabaseHelper.COLUMN_PURCHASE_MAX, limits.getPurchaseMax());
        values.put(SettingsDatabaseHelper.COLUMN_WITHDRAWAL_LIMIT_ENABLED, limits.isWithdrawalLimitEnabled() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_WITHDRAWAL_MIN, limits.getWithdrawalMin());
        values.put(SettingsDatabaseHelper.COLUMN_WITHDRAWAL_MAX, limits.getWithdrawalMax());
        values.put(SettingsDatabaseHelper.COLUMN_TRANSFER_LIMIT_ENABLED, limits.isTransferLimitEnabled() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_TRANSFER_MIN, limits.getTransferMin());
        values.put(SettingsDatabaseHelper.COLUMN_TRANSFER_MAX, limits.getTransferMax());
        values.put(SettingsDatabaseHelper.COLUMN_REFUND_LIMIT_ENABLED, limits.isRefundLimitEnabled() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_REFUND_MAX, limits.getRefundMax());
        values.put(SettingsDatabaseHelper.COLUMN_CASH_BACK_LIMIT_ENABLED, limits.isCashBackLimitEnabled() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_CASH_BACK_MAX, limits.getCashBackMax());
        values.put(SettingsDatabaseHelper.COLUMN_DAILY_LIMIT_ENABLED, limits.isDailyLimitEnabled() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_DAILY_TRANSACTION_LIMIT, limits.getDailyTransactionLimit());
        values.put(SettingsDatabaseHelper.COLUMN_DAILY_AMOUNT_LIMIT, limits.getDailyAmountLimit());
        values.put(SettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SettingsDatabaseHelper.TABLE_TRANSACTION_LIMITS, values, null, null);
        return rows > 0;
    }
    
    // Terminal Config Methods
    public TerminalConfig getTerminalConfig() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_TERMINAL_CONFIG,
            null, null, null, null, null, null, "1");
        
        TerminalConfig config = null;
        if (cursor != null && cursor.moveToFirst()) {
            config = new TerminalConfig();
            config.setTerminalId(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_TERMINAL_ID)));
            config.setMerchantId(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_MERCHANT_ID)));
            config.setMerchantName(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_MERCHANT_NAME)));
            config.setMerchantAddress(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_MERCHANT_ADDRESS)));
            config.setMerchantCity(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_MERCHANT_CITY)));
            config.setMerchantPhone(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_MERCHANT_PHONE)));
            config.setCurrency(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_CURRENCY)));
            config.setLanguage(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_LANGUAGE)));
            config.setDateFormat(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_DATE_FORMAT)));
            config.setTipEnabled(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_TIP_ENABLED)) == 1);
            config.setSignatureRequired(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_SIGNATURE_REQUIRED)) == 1);
            config.setOfflineMode(cursor.getInt(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_OFFLINE_MODE)) == 1);
            config.setBatchNumber(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_BATCH_NUMBER)));
            config.setTraceNumber(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_TRACE_NUMBER)));
            config.setInvoiceNumber(cursor.getString(cursor.getColumnIndex(SettingsDatabaseHelper.COLUMN_INVOICE_NUMBER)));
            cursor.close();
        }
        
        return config;
    }
    
    public boolean saveTerminalConfig(TerminalConfig config) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SettingsDatabaseHelper.COLUMN_TERMINAL_ID, config.getTerminalId());
        values.put(SettingsDatabaseHelper.COLUMN_MERCHANT_ID, config.getMerchantId());
        values.put(SettingsDatabaseHelper.COLUMN_MERCHANT_NAME, config.getMerchantName());
        values.put(SettingsDatabaseHelper.COLUMN_MERCHANT_ADDRESS, config.getMerchantAddress());
        values.put(SettingsDatabaseHelper.COLUMN_MERCHANT_CITY, config.getMerchantCity());
        values.put(SettingsDatabaseHelper.COLUMN_MERCHANT_PHONE, config.getMerchantPhone());
        values.put(SettingsDatabaseHelper.COLUMN_CURRENCY, config.getCurrency());
        values.put(SettingsDatabaseHelper.COLUMN_LANGUAGE, config.getLanguage());
        values.put(SettingsDatabaseHelper.COLUMN_DATE_FORMAT, config.getDateFormat());
        values.put(SettingsDatabaseHelper.COLUMN_TIP_ENABLED, config.isTipEnabled() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_SIGNATURE_REQUIRED, config.isSignatureRequired() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_OFFLINE_MODE, config.isOfflineMode() ? 1 : 0);
        values.put(SettingsDatabaseHelper.COLUMN_BATCH_NUMBER, config.getBatchNumber());
        values.put(SettingsDatabaseHelper.COLUMN_TRACE_NUMBER, config.getTraceNumber());
        values.put(SettingsDatabaseHelper.COLUMN_INVOICE_NUMBER, config.getInvoiceNumber());
        values.put(SettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SettingsDatabaseHelper.TABLE_TERMINAL_CONFIG, values, null, null);
        return rows > 0;
    }
    
    // Counter methods
    public void incrementTraceNumber() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + SettingsDatabaseHelper.TABLE_TERMINAL_CONFIG + 
            " SET " + SettingsDatabaseHelper.COLUMN_TRACE_NUMBER + 
            " = printf('%06d', CAST(" + SettingsDatabaseHelper.COLUMN_TRACE_NUMBER + " AS INTEGER) + 1)");
    }
    
    public void incrementInvoiceNumber() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + SettingsDatabaseHelper.TABLE_TERMINAL_CONFIG + 
            " SET " + SettingsDatabaseHelper.COLUMN_INVOICE_NUMBER + 
            " = printf('%06d', CAST(" + SettingsDatabaseHelper.COLUMN_INVOICE_NUMBER + " AS INTEGER) + 1)");
    }
    
    public void resetCounters() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SettingsDatabaseHelper.COLUMN_BATCH_NUMBER, "000001");
        values.put(SettingsDatabaseHelper.COLUMN_TRACE_NUMBER, "000001");
        values.put(SettingsDatabaseHelper.COLUMN_INVOICE_NUMBER, "000001");
        db.update(SettingsDatabaseHelper.TABLE_TERMINAL_CONFIG, values, null, null);
    }
    
    // Generic setting methods for key-value storage
    public void setSetting(String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        db.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("settings", new String[]{"value"}, 
            "key = ?", new String[]{key}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(0);
            cursor.close();
            return value;
        }
        
        if (cursor != null) cursor.close();
        return defaultValue;
    }
}