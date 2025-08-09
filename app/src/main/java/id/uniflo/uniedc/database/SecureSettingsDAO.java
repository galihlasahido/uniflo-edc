package id.uniflo.uniedc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import java.security.MessageDigest;

public class SecureSettingsDAO {
    
    private SecureSettingsDatabaseHelper dbHelper;
    private Context context;
    
    public SecureSettingsDAO(Context context) {
        this.context = context;
        this.dbHelper = SecureSettingsDatabaseHelper.getInstance(context);
    }
    
    // Network Settings Methods
    public NetworkSettings getNetworkSettings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SecureSettingsDatabaseHelper.TABLE_NETWORK_SETTINGS,
            null, null, null, null, null, null, "1");
        
        NetworkSettings settings = null;
        if (cursor != null && cursor.moveToFirst()) {
            settings = new NetworkSettings();
            settings.setConnectionType(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_CONNECTION_TYPE)));
            settings.setPrimaryHost(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRIMARY_HOST)));
            settings.setPrimaryPort(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRIMARY_PORT)));
            settings.setSecondaryHost(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_SECONDARY_HOST)));
            settings.setSecondaryPort(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_SECONDARY_PORT)));
            settings.setTimeout(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TIMEOUT)));
            settings.setRetryCount(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_RETRY_COUNT)));
            settings.setUseSsl(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_USE_SSL)) == 1);
            settings.setKeepAlive(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_KEEP_ALIVE)) == 1);
            settings.setProtocol(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PROTOCOL)));
            cursor.close();
        }
        
        return settings;
    }
    
    public boolean saveNetworkSettings(NetworkSettings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SecureSettingsDatabaseHelper.COLUMN_CONNECTION_TYPE, settings.getConnectionType());
        values.put(SecureSettingsDatabaseHelper.COLUMN_PRIMARY_HOST, settings.getPrimaryHost());
        values.put(SecureSettingsDatabaseHelper.COLUMN_PRIMARY_PORT, settings.getPrimaryPort());
        values.put(SecureSettingsDatabaseHelper.COLUMN_SECONDARY_HOST, settings.getSecondaryHost());
        values.put(SecureSettingsDatabaseHelper.COLUMN_SECONDARY_PORT, settings.getSecondaryPort());
        values.put(SecureSettingsDatabaseHelper.COLUMN_TIMEOUT, settings.getTimeout());
        values.put(SecureSettingsDatabaseHelper.COLUMN_RETRY_COUNT, settings.getRetryCount());
        values.put(SecureSettingsDatabaseHelper.COLUMN_USE_SSL, settings.isUseSsl() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_KEEP_ALIVE, settings.isKeepAlive() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_PROTOCOL, settings.getProtocol());
        values.put(SecureSettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SecureSettingsDatabaseHelper.TABLE_NETWORK_SETTINGS, values, null, null);
        return rows > 0;
    }
    
    // Printer Settings Methods
    public PrinterSettings getPrinterSettings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SecureSettingsDatabaseHelper.TABLE_PRINTER_SETTINGS,
            null, null, null, null, null, null, "1");
        
        PrinterSettings settings = null;
        if (cursor != null && cursor.moveToFirst()) {
            settings = new PrinterSettings();
            settings.setPrintDensity(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRINT_DENSITY)));
            settings.setPrintLogo(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRINT_LOGO)) == 1);
            settings.setPrintMerchantCopy(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRINT_MERCHANT_COPY)) == 1);
            settings.setPrintCustomerCopy(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PRINT_CUSTOMER_COPY)) == 1);
            settings.setHeaderLine1(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_HEADER_LINE1)));
            settings.setHeaderLine2(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_HEADER_LINE2)));
            settings.setFooterLine1(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_FOOTER_LINE1)));
            settings.setFooterLine2(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_FOOTER_LINE2)));
            cursor.close();
        }
        
        return settings;
    }
    
    public boolean savePrinterSettings(PrinterSettings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SecureSettingsDatabaseHelper.COLUMN_PRINT_DENSITY, settings.getPrintDensity());
        values.put(SecureSettingsDatabaseHelper.COLUMN_PRINT_LOGO, settings.isPrintLogo() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_PRINT_MERCHANT_COPY, settings.isPrintMerchantCopy() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_PRINT_CUSTOMER_COPY, settings.isPrintCustomerCopy() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_HEADER_LINE1, settings.getHeaderLine1());
        values.put(SecureSettingsDatabaseHelper.COLUMN_HEADER_LINE2, settings.getHeaderLine2());
        values.put(SecureSettingsDatabaseHelper.COLUMN_FOOTER_LINE1, settings.getFooterLine1());
        values.put(SecureSettingsDatabaseHelper.COLUMN_FOOTER_LINE2, settings.getFooterLine2());
        values.put(SecureSettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SecureSettingsDatabaseHelper.TABLE_PRINTER_SETTINGS, values, null, null);
        return rows > 0;
    }
    
    // Security Settings Methods
    public SecuritySettings getSecuritySettings() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SecureSettingsDatabaseHelper.TABLE_SECURITY_SETTINGS,
            null, null, null, null, null, null, "1");
        
        SecuritySettings settings = null;
        if (cursor != null && cursor.moveToFirst()) {
            settings = new SecuritySettings();
            settings.setPinVerification(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PIN_VERIFICATION)) == 1);
            settings.setAdminPin(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_ADMIN_PIN)));
            settings.setMaxPinAttempts(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MAX_PIN_ATTEMPTS)));
            settings.setVoidPassword(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_VOID_PASSWORD)) == 1);
            settings.setSettlementPassword(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_SETTLEMENT_PASSWORD)) == 1);
            settings.setRefundPassword(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_REFUND_PASSWORD)) == 1);
            settings.setKeyStatus(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_KEY_STATUS)));
            settings.setLastKeyDownload(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_LAST_KEY_DOWNLOAD)));
            cursor.close();
        }
        
        return settings;
    }
    
    public boolean saveSecuritySettings(SecuritySettings settings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SecureSettingsDatabaseHelper.COLUMN_PIN_VERIFICATION, settings.isPinVerification() ? 1 : 0);
        // Hash the PIN before storing
        values.put(SecureSettingsDatabaseHelper.COLUMN_ADMIN_PIN, hashPin(settings.getAdminPin()));
        values.put(SecureSettingsDatabaseHelper.COLUMN_MAX_PIN_ATTEMPTS, settings.getMaxPinAttempts());
        values.put(SecureSettingsDatabaseHelper.COLUMN_VOID_PASSWORD, settings.isVoidPassword() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_SETTLEMENT_PASSWORD, settings.isSettlementPassword() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_REFUND_PASSWORD, settings.isRefundPassword() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_KEY_STATUS, settings.getKeyStatus());
        values.put(SecureSettingsDatabaseHelper.COLUMN_LAST_KEY_DOWNLOAD, settings.getLastKeyDownload());
        values.put(SecureSettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SecureSettingsDatabaseHelper.TABLE_SECURITY_SETTINGS, values, null, null);
        return rows > 0;
    }
    
    /**
     * Verify PIN by comparing hashed values
     */
    public boolean verifyPin(String inputPin) {
        SecuritySettings settings = getSecuritySettings();
        if (settings != null) {
            String hashedInput = hashPin(inputPin);
            return hashedInput.equals(settings.getAdminPin());
        }
        return false;
    }
    
    // Transaction Limits Methods
    public TransactionLimits getTransactionLimits() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SecureSettingsDatabaseHelper.TABLE_TRANSACTION_LIMITS,
            null, null, null, null, null, null, "1");
        
        TransactionLimits limits = null;
        if (cursor != null && cursor.moveToFirst()) {
            limits = new TransactionLimits();
            limits.setPurchaseLimitEnabled(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PURCHASE_LIMIT_ENABLED)) == 1);
            limits.setPurchaseMin(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PURCHASE_MIN)));
            limits.setPurchaseMax(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_PURCHASE_MAX)));
            limits.setWithdrawalLimitEnabled(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_WITHDRAWAL_LIMIT_ENABLED)) == 1);
            limits.setWithdrawalMin(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_WITHDRAWAL_MIN)));
            limits.setWithdrawalMax(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_WITHDRAWAL_MAX)));
            limits.setTransferLimitEnabled(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TRANSFER_LIMIT_ENABLED)) == 1);
            limits.setTransferMin(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TRANSFER_MIN)));
            limits.setTransferMax(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TRANSFER_MAX)));
            limits.setRefundLimitEnabled(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_REFUND_LIMIT_ENABLED)) == 1);
            limits.setRefundMax(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_REFUND_MAX)));
            limits.setCashBackLimitEnabled(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_CASH_BACK_LIMIT_ENABLED)) == 1);
            limits.setCashBackMax(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_CASH_BACK_MAX)));
            limits.setDailyLimitEnabled(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_DAILY_LIMIT_ENABLED)) == 1);
            limits.setDailyTransactionLimit(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_DAILY_TRANSACTION_LIMIT)));
            limits.setDailyAmountLimit(cursor.getLong(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_DAILY_AMOUNT_LIMIT)));
            cursor.close();
        }
        
        return limits;
    }
    
    public boolean saveTransactionLimits(TransactionLimits limits) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SecureSettingsDatabaseHelper.COLUMN_PURCHASE_LIMIT_ENABLED, limits.isPurchaseLimitEnabled() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_PURCHASE_MIN, limits.getPurchaseMin());
        values.put(SecureSettingsDatabaseHelper.COLUMN_PURCHASE_MAX, limits.getPurchaseMax());
        values.put(SecureSettingsDatabaseHelper.COLUMN_WITHDRAWAL_LIMIT_ENABLED, limits.isWithdrawalLimitEnabled() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_WITHDRAWAL_MIN, limits.getWithdrawalMin());
        values.put(SecureSettingsDatabaseHelper.COLUMN_WITHDRAWAL_MAX, limits.getWithdrawalMax());
        values.put(SecureSettingsDatabaseHelper.COLUMN_TRANSFER_LIMIT_ENABLED, limits.isTransferLimitEnabled() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_TRANSFER_MIN, limits.getTransferMin());
        values.put(SecureSettingsDatabaseHelper.COLUMN_TRANSFER_MAX, limits.getTransferMax());
        values.put(SecureSettingsDatabaseHelper.COLUMN_REFUND_LIMIT_ENABLED, limits.isRefundLimitEnabled() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_REFUND_MAX, limits.getRefundMax());
        values.put(SecureSettingsDatabaseHelper.COLUMN_CASH_BACK_LIMIT_ENABLED, limits.isCashBackLimitEnabled() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_CASH_BACK_MAX, limits.getCashBackMax());
        values.put(SecureSettingsDatabaseHelper.COLUMN_DAILY_LIMIT_ENABLED, limits.isDailyLimitEnabled() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_DAILY_TRANSACTION_LIMIT, limits.getDailyTransactionLimit());
        values.put(SecureSettingsDatabaseHelper.COLUMN_DAILY_AMOUNT_LIMIT, limits.getDailyAmountLimit());
        values.put(SecureSettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SecureSettingsDatabaseHelper.TABLE_TRANSACTION_LIMITS, values, null, null);
        return rows > 0;
    }
    
    // Terminal Config Methods
    public TerminalConfig getTerminalConfig() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG,
            null, null, null, null, null, null, "1");
        
        TerminalConfig config = null;
        if (cursor != null && cursor.moveToFirst()) {
            config = new TerminalConfig();
            config.setTerminalId(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TERMINAL_ID)));
            config.setMerchantId(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_ID)));
            config.setMerchantName(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_NAME)));
            config.setMerchantAddress(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_ADDRESS)));
            config.setMerchantCity(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_CITY)));
            config.setMerchantPhone(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_PHONE)));
            config.setCurrency(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_CURRENCY)));
            config.setLanguage(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_LANGUAGE)));
            config.setDateFormat(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_DATE_FORMAT)));
            config.setTipEnabled(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TIP_ENABLED)) == 1);
            config.setSignatureRequired(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_SIGNATURE_REQUIRED)) == 1);
            config.setOfflineMode(cursor.getInt(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_OFFLINE_MODE)) == 1);
            config.setBatchNumber(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_BATCH_NUMBER)));
            config.setTraceNumber(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER)));
            config.setInvoiceNumber(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_INVOICE_NUMBER)));
            config.setAcquiringInstitutionCode(cursor.getString(cursor.getColumnIndex(SecureSettingsDatabaseHelper.COLUMN_ACQUIRING_INSTITUTION_CODE)));
            cursor.close();
        }
        
        return config;
    }
    
    public boolean saveTerminalConfig(TerminalConfig config) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(SecureSettingsDatabaseHelper.COLUMN_TERMINAL_ID, config.getTerminalId());
        values.put(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_ID, config.getMerchantId());
        values.put(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_NAME, config.getMerchantName());
        values.put(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_ADDRESS, config.getMerchantAddress());
        values.put(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_CITY, config.getMerchantCity());
        values.put(SecureSettingsDatabaseHelper.COLUMN_MERCHANT_PHONE, config.getMerchantPhone());
        values.put(SecureSettingsDatabaseHelper.COLUMN_CURRENCY, config.getCurrency());
        values.put(SecureSettingsDatabaseHelper.COLUMN_LANGUAGE, config.getLanguage());
        values.put(SecureSettingsDatabaseHelper.COLUMN_DATE_FORMAT, config.getDateFormat());
        values.put(SecureSettingsDatabaseHelper.COLUMN_TIP_ENABLED, config.isTipEnabled() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_SIGNATURE_REQUIRED, config.isSignatureRequired() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_OFFLINE_MODE, config.isOfflineMode() ? 1 : 0);
        values.put(SecureSettingsDatabaseHelper.COLUMN_BATCH_NUMBER, config.getBatchNumber());
        values.put(SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER, config.getTraceNumber());
        values.put(SecureSettingsDatabaseHelper.COLUMN_INVOICE_NUMBER, config.getInvoiceNumber());
        values.put(SecureSettingsDatabaseHelper.COLUMN_ACQUIRING_INSTITUTION_CODE, config.getAcquiringInstitutionCode());
        values.put(SecureSettingsDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG, values, null, null);
        return rows > 0;
    }
    
    // Counter methods
    public void incrementTraceNumber() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.rawExecSQL("UPDATE " + SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG + 
            " SET " + SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER + 
            " = printf('%06d', CAST(" + SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER + " AS INTEGER) + 1)");
    }
    
    public void incrementInvoiceNumber() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.rawExecSQL("UPDATE " + SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG + 
            " SET " + SecureSettingsDatabaseHelper.COLUMN_INVOICE_NUMBER + 
            " = printf('%06d', CAST(" + SecureSettingsDatabaseHelper.COLUMN_INVOICE_NUMBER + " AS INTEGER) + 1)");
    }
    
    public void resetCounters() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SecureSettingsDatabaseHelper.COLUMN_BATCH_NUMBER, "000001");
        values.put(SecureSettingsDatabaseHelper.COLUMN_TRACE_NUMBER, "000001");
        values.put(SecureSettingsDatabaseHelper.COLUMN_INVOICE_NUMBER, "000001");
        db.update(SecureSettingsDatabaseHelper.TABLE_TERMINAL_CONFIG, values, null, null);
    }
    
    /**
     * Hash PIN using SHA-256
     */
    private String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
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
        return dbHelper.performIntegrityCheck();
    }
}