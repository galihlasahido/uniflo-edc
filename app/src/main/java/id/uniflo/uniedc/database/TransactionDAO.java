package id.uniflo.uniedc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionDAO {
    
    private TransactionDatabaseHelper dbHelper;
    private Context context;
    
    public TransactionDAO(Context context) {
        this.context = context;
        this.dbHelper = TransactionDatabaseHelper.getInstance(context);
    }
    
    /**
     * Insert a new transaction
     */
    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(TransactionDatabaseHelper.COLUMN_TRANSACTION_TYPE, transaction.getTransactionType());
        values.put(TransactionDatabaseHelper.COLUMN_STATUS, transaction.getStatus());
        values.put(TransactionDatabaseHelper.COLUMN_AMOUNT, transaction.getAmount());
        values.put(TransactionDatabaseHelper.COLUMN_CARD_NUMBER, transaction.getCardNumber());
        values.put(TransactionDatabaseHelper.COLUMN_CARD_HOLDER_NAME, transaction.getCardHolderName());
        values.put(TransactionDatabaseHelper.COLUMN_CARD_TYPE, transaction.getCardType());
        values.put(TransactionDatabaseHelper.COLUMN_ENTRY_MODE, transaction.getEntryMode());
        values.put(TransactionDatabaseHelper.COLUMN_TERMINAL_ID, transaction.getTerminalId());
        values.put(TransactionDatabaseHelper.COLUMN_MERCHANT_ID, transaction.getMerchantId());
        values.put(TransactionDatabaseHelper.COLUMN_BATCH_NUMBER, transaction.getBatchNumber());
        values.put(TransactionDatabaseHelper.COLUMN_TRACE_NUMBER, transaction.getTraceNumber());
        values.put(TransactionDatabaseHelper.COLUMN_REFERENCE_NUMBER, transaction.getReferenceNumber());
        values.put(TransactionDatabaseHelper.COLUMN_APPROVAL_CODE, transaction.getApprovalCode());
        values.put(TransactionDatabaseHelper.COLUMN_RESPONSE_CODE, transaction.getResponseCode());
        values.put(TransactionDatabaseHelper.COLUMN_RESPONSE_MESSAGE, transaction.getResponseMessage());
        values.put(TransactionDatabaseHelper.COLUMN_EMV_DATA, transaction.getEmvData());
        values.put(TransactionDatabaseHelper.COLUMN_PIN_BLOCK, transaction.getPinBlock());
        values.put(TransactionDatabaseHelper.COLUMN_ARQC, transaction.getArqc());
        values.put(TransactionDatabaseHelper.COLUMN_ATC, transaction.getAtc());
        values.put(TransactionDatabaseHelper.COLUMN_TVR, transaction.getTvr());
        values.put(TransactionDatabaseHelper.COLUMN_TSI, transaction.getTsi());
        values.put(TransactionDatabaseHelper.COLUMN_AID, transaction.getAid());
        values.put(TransactionDatabaseHelper.COLUMN_APPLICATION_LABEL, transaction.getApplicationLabel());
        values.put(TransactionDatabaseHelper.COLUMN_TRANSACTION_DATE, 
            transaction.getTransactionDate() != null ? transaction.getTransactionDate().getTime() : System.currentTimeMillis());
        values.put(TransactionDatabaseHelper.COLUMN_IS_VOIDED, transaction.isVoided() ? 1 : 0);
        values.put(TransactionDatabaseHelper.COLUMN_VOID_REFERENCE_NUMBER, transaction.getVoidReferenceNumber());
        values.put(TransactionDatabaseHelper.COLUMN_VOID_DATE, 
            transaction.getVoidDate() != null ? transaction.getVoidDate().getTime() : null);
        values.put(TransactionDatabaseHelper.COLUMN_RAW_REQUEST, transaction.getRawRequest());
        values.put(TransactionDatabaseHelper.COLUMN_RAW_RESPONSE, transaction.getRawResponse());
        values.put(TransactionDatabaseHelper.COLUMN_CREATED_AT, System.currentTimeMillis());
        values.put(TransactionDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        long id = db.insert(TransactionDatabaseHelper.TABLE_TRANSACTIONS, null, values);
        transaction.setId(id);
        return id;
    }
    
    /**
     * Update an existing transaction
     */
    public boolean updateTransaction(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(TransactionDatabaseHelper.COLUMN_STATUS, transaction.getStatus());
        values.put(TransactionDatabaseHelper.COLUMN_RESPONSE_CODE, transaction.getResponseCode());
        values.put(TransactionDatabaseHelper.COLUMN_RESPONSE_MESSAGE, transaction.getResponseMessage());
        values.put(TransactionDatabaseHelper.COLUMN_APPROVAL_CODE, transaction.getApprovalCode());
        values.put(TransactionDatabaseHelper.COLUMN_IS_VOIDED, transaction.isVoided() ? 1 : 0);
        values.put(TransactionDatabaseHelper.COLUMN_VOID_REFERENCE_NUMBER, transaction.getVoidReferenceNumber());
        values.put(TransactionDatabaseHelper.COLUMN_VOID_DATE, 
            transaction.getVoidDate() != null ? transaction.getVoidDate().getTime() : null);
        values.put(TransactionDatabaseHelper.COLUMN_RAW_RESPONSE, transaction.getRawResponse());
        values.put(TransactionDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis());
        
        int rows = db.update(TransactionDatabaseHelper.TABLE_TRANSACTIONS, values, 
            TransactionDatabaseHelper.COLUMN_ID + " = ?", 
            new String[]{String.valueOf(transaction.getId())});
        
        return rows > 0;
    }
    
    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TransactionDatabaseHelper.TABLE_TRANSACTIONS,
            null,
            TransactionDatabaseHelper.COLUMN_ID + " = ?",
            new String[]{String.valueOf(id)},
            null, null, null);
        
        Transaction transaction = null;
        if (cursor != null && cursor.moveToFirst()) {
            transaction = cursorToTransaction(cursor);
            cursor.close();
        }
        
        return transaction;
    }
    
    /**
     * Get transaction by reference number
     */
    public Transaction getTransactionByReferenceNumber(String referenceNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TransactionDatabaseHelper.TABLE_TRANSACTIONS,
            null,
            TransactionDatabaseHelper.COLUMN_REFERENCE_NUMBER + " = ?",
            new String[]{referenceNumber},
            null, null, null);
        
        Transaction transaction = null;
        if (cursor != null && cursor.moveToFirst()) {
            transaction = cursorToTransaction(cursor);
            cursor.close();
        }
        
        return transaction;
    }
    
    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return getTransactions(null, null, TransactionDatabaseHelper.COLUMN_CREATED_AT + " DESC");
    }
    
    /**
     * Get transactions by type
     */
    public List<Transaction> getTransactionsByType(String type) {
        return getTransactions(
            TransactionDatabaseHelper.COLUMN_TRANSACTION_TYPE + " = ?",
            new String[]{type},
            TransactionDatabaseHelper.COLUMN_CREATED_AT + " DESC"
        );
    }
    
    /**
     * Get transactions by date range
     */
    public List<Transaction> getTransactionsByDateRange(Date startDate, Date endDate) {
        return getTransactions(
            TransactionDatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?",
            new String[]{String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime())},
            TransactionDatabaseHelper.COLUMN_CREATED_AT + " DESC"
        );
    }
    
    /**
     * Get transactions by batch number
     */
    public List<Transaction> getTransactionsByBatch(String batchNumber) {
        return getTransactions(
            TransactionDatabaseHelper.COLUMN_BATCH_NUMBER + " = ?",
            new String[]{batchNumber},
            TransactionDatabaseHelper.COLUMN_CREATED_AT + " DESC"
        );
    }
    
    /**
     * Get today's transactions
     */
    public List<Transaction> getTodayTransactions() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        long startOfDay = 0;
        long endOfDay = 0;
        
        try {
            startOfDay = sdf.parse(today).getTime();
            endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return getTransactions(
            TransactionDatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?",
            new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)},
            TransactionDatabaseHelper.COLUMN_CREATED_AT + " DESC"
        );
    }
    
    /**
     * Get transaction summary for today
     */
    public TransactionSummary getTodayTransactionSummary() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        TransactionSummary summary = new TransactionSummary();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        long startOfDay = 0;
        long endOfDay = 0;
        
        try {
            startOfDay = sdf.parse(today).getTime();
            endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Get total count and amount
        String query = "SELECT COUNT(*), SUM(" + TransactionDatabaseHelper.COLUMN_AMOUNT + ") " +
                      "FROM " + TransactionDatabaseHelper.TABLE_TRANSACTIONS + " " +
                      "WHERE " + TransactionDatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ? " +
                      "AND " + TransactionDatabaseHelper.COLUMN_STATUS + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{
            String.valueOf(startOfDay), 
            String.valueOf(endOfDay),
            Transaction.STATUS_SUCCESS
        });
        
        if (cursor != null && cursor.moveToFirst()) {
            summary.setTotalCount(cursor.getInt(0));
            summary.setTotalAmount(cursor.getLong(1));
            cursor.close();
        }
        
        // Get count by type
        String typeQuery = "SELECT " + TransactionDatabaseHelper.COLUMN_TRANSACTION_TYPE + ", COUNT(*), SUM(" + TransactionDatabaseHelper.COLUMN_AMOUNT + ") " +
                          "FROM " + TransactionDatabaseHelper.TABLE_TRANSACTIONS + " " +
                          "WHERE " + TransactionDatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ? " +
                          "AND " + TransactionDatabaseHelper.COLUMN_STATUS + " = ? " +
                          "GROUP BY " + TransactionDatabaseHelper.COLUMN_TRANSACTION_TYPE;
        
        Cursor typeCursor = db.rawQuery(typeQuery, new String[]{
            String.valueOf(startOfDay), 
            String.valueOf(endOfDay),
            Transaction.STATUS_SUCCESS
        });
        
        if (typeCursor != null) {
            while (typeCursor.moveToNext()) {
                String type = typeCursor.getString(0);
                int count = typeCursor.getInt(1);
                long amount = typeCursor.getLong(2);
                
                switch (type) {
                    case Transaction.TYPE_SALE:
                        summary.setSaleCount(count);
                        summary.setSaleAmount(amount);
                        break;
                    case Transaction.TYPE_TRANSFER:
                        summary.setTransferCount(count);
                        summary.setTransferAmount(amount);
                        break;
                    case Transaction.TYPE_REFUND:
                        summary.setRefundCount(count);
                        summary.setRefundAmount(amount);
                        break;
                    case Transaction.TYPE_VOID:
                        summary.setVoidCount(count);
                        summary.setVoidAmount(amount);
                        break;
                }
            }
            typeCursor.close();
        }
        
        return summary;
    }
    
    /**
     * Delete old transactions (older than days specified)
     */
    public int deleteOldTransactions(int days) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);
        
        return db.delete(TransactionDatabaseHelper.TABLE_TRANSACTIONS,
            TransactionDatabaseHelper.COLUMN_CREATED_AT + " < ?",
            new String[]{String.valueOf(cutoffTime)});
    }
    
    /**
     * Generic method to get transactions with conditions
     */
    private List<Transaction> getTransactions(String selection, String[] selectionArgs, String orderBy) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(TransactionDatabaseHelper.TABLE_TRANSACTIONS,
            null, selection, selectionArgs, null, null, orderBy);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Transaction transaction = cursorToTransaction(cursor);
                transactions.add(transaction);
            }
            cursor.close();
        }
        
        return transactions;
    }
    
    /**
     * Convert cursor to Transaction object
     */
    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction transaction = new Transaction();
        
        transaction.setId(cursor.getLong(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_ID)));
        transaction.setTransactionType(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_TRANSACTION_TYPE)));
        transaction.setStatus(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_STATUS)));
        transaction.setAmount(cursor.getLong(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_AMOUNT)));
        transaction.setCardNumber(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_CARD_NUMBER)));
        transaction.setCardHolderName(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_CARD_HOLDER_NAME)));
        transaction.setCardType(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_CARD_TYPE)));
        transaction.setEntryMode(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_ENTRY_MODE)));
        transaction.setTerminalId(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_TERMINAL_ID)));
        transaction.setMerchantId(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_MERCHANT_ID)));
        transaction.setBatchNumber(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_BATCH_NUMBER)));
        transaction.setTraceNumber(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_TRACE_NUMBER)));
        transaction.setReferenceNumber(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_REFERENCE_NUMBER)));
        transaction.setApprovalCode(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_APPROVAL_CODE)));
        transaction.setResponseCode(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_RESPONSE_CODE)));
        transaction.setResponseMessage(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_RESPONSE_MESSAGE)));
        transaction.setEmvData(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_EMV_DATA)));
        transaction.setPinBlock(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_PIN_BLOCK)));
        transaction.setArqc(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_ARQC)));
        transaction.setAtc(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_ATC)));
        transaction.setTvr(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_TVR)));
        transaction.setTsi(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_TSI)));
        transaction.setAid(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_AID)));
        transaction.setApplicationLabel(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_APPLICATION_LABEL)));
        
        long transactionDateMillis = cursor.getLong(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_TRANSACTION_DATE));
        transaction.setTransactionDate(new Date(transactionDateMillis));
        
        transaction.setVoided(cursor.getInt(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_IS_VOIDED)) == 1);
        transaction.setVoidReferenceNumber(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_VOID_REFERENCE_NUMBER)));
        
        if (!cursor.isNull(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_VOID_DATE))) {
            long voidDateMillis = cursor.getLong(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_VOID_DATE));
            transaction.setVoidDate(new Date(voidDateMillis));
        }
        
        transaction.setRawRequest(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_RAW_REQUEST)));
        transaction.setRawResponse(cursor.getString(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_RAW_RESPONSE)));
        transaction.setCreatedAt(cursor.getLong(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_CREATED_AT)));
        transaction.setUpdatedAt(cursor.getLong(cursor.getColumnIndex(TransactionDatabaseHelper.COLUMN_UPDATED_AT)));
        
        return transaction;
    }
    
    /**
     * Inner class for transaction summary
     */
    public static class TransactionSummary {
        private int totalCount;
        private long totalAmount;
        private int saleCount;
        private long saleAmount;
        private int transferCount;
        private long transferAmount;
        private int refundCount;
        private long refundAmount;
        private int voidCount;
        private long voidAmount;
        
        // Getters and setters
        public int getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
        
        public long getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(long totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public int getSaleCount() {
            return saleCount;
        }
        
        public void setSaleCount(int saleCount) {
            this.saleCount = saleCount;
        }
        
        public long getSaleAmount() {
            return saleAmount;
        }
        
        public void setSaleAmount(long saleAmount) {
            this.saleAmount = saleAmount;
        }
        
        public int getTransferCount() {
            return transferCount;
        }
        
        public void setTransferCount(int transferCount) {
            this.transferCount = transferCount;
        }
        
        public long getTransferAmount() {
            return transferAmount;
        }
        
        public void setTransferAmount(long transferAmount) {
            this.transferAmount = transferAmount;
        }
        
        public int getRefundCount() {
            return refundCount;
        }
        
        public void setRefundCount(int refundCount) {
            this.refundCount = refundCount;
        }
        
        public long getRefundAmount() {
            return refundAmount;
        }
        
        public void setRefundAmount(long refundAmount) {
            this.refundAmount = refundAmount;
        }
        
        public int getVoidCount() {
            return voidCount;
        }
        
        public void setVoidCount(int voidCount) {
            this.voidCount = voidCount;
        }
        
        public long getVoidAmount() {
            return voidAmount;
        }
        
        public void setVoidAmount(long voidAmount) {
            this.voidAmount = voidAmount;
        }
    }
}