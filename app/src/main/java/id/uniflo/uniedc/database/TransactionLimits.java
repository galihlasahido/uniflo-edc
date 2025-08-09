package id.uniflo.uniedc.database;

public class TransactionLimits {
    private boolean purchaseLimitEnabled;
    private long purchaseMin;
    private long purchaseMax;
    private boolean withdrawalLimitEnabled;
    private long withdrawalMin;
    private long withdrawalMax;
    private boolean transferLimitEnabled;
    private long transferMin;
    private long transferMax;
    private boolean refundLimitEnabled;
    private long refundMax;
    private boolean cashBackLimitEnabled;
    private long cashBackMax;
    private boolean dailyLimitEnabled;
    private int dailyTransactionLimit;
    private long dailyAmountLimit;
    
    public TransactionLimits() {
        // Default constructor
    }
    
    // Getters and Setters
    public boolean isPurchaseLimitEnabled() {
        return purchaseLimitEnabled;
    }
    
    public void setPurchaseLimitEnabled(boolean purchaseLimitEnabled) {
        this.purchaseLimitEnabled = purchaseLimitEnabled;
    }
    
    public long getPurchaseMin() {
        return purchaseMin;
    }
    
    public void setPurchaseMin(long purchaseMin) {
        this.purchaseMin = purchaseMin;
    }
    
    public long getPurchaseMax() {
        return purchaseMax;
    }
    
    public void setPurchaseMax(long purchaseMax) {
        this.purchaseMax = purchaseMax;
    }
    
    public boolean isWithdrawalLimitEnabled() {
        return withdrawalLimitEnabled;
    }
    
    public void setWithdrawalLimitEnabled(boolean withdrawalLimitEnabled) {
        this.withdrawalLimitEnabled = withdrawalLimitEnabled;
    }
    
    public long getWithdrawalMin() {
        return withdrawalMin;
    }
    
    public void setWithdrawalMin(long withdrawalMin) {
        this.withdrawalMin = withdrawalMin;
    }
    
    public long getWithdrawalMax() {
        return withdrawalMax;
    }
    
    public void setWithdrawalMax(long withdrawalMax) {
        this.withdrawalMax = withdrawalMax;
    }
    
    public boolean isTransferLimitEnabled() {
        return transferLimitEnabled;
    }
    
    public void setTransferLimitEnabled(boolean transferLimitEnabled) {
        this.transferLimitEnabled = transferLimitEnabled;
    }
    
    public long getTransferMin() {
        return transferMin;
    }
    
    public void setTransferMin(long transferMin) {
        this.transferMin = transferMin;
    }
    
    public long getTransferMax() {
        return transferMax;
    }
    
    public void setTransferMax(long transferMax) {
        this.transferMax = transferMax;
    }
    
    public boolean isRefundLimitEnabled() {
        return refundLimitEnabled;
    }
    
    public void setRefundLimitEnabled(boolean refundLimitEnabled) {
        this.refundLimitEnabled = refundLimitEnabled;
    }
    
    public long getRefundMax() {
        return refundMax;
    }
    
    public void setRefundMax(long refundMax) {
        this.refundMax = refundMax;
    }
    
    public boolean isCashBackLimitEnabled() {
        return cashBackLimitEnabled;
    }
    
    public void setCashBackLimitEnabled(boolean cashBackLimitEnabled) {
        this.cashBackLimitEnabled = cashBackLimitEnabled;
    }
    
    public long getCashBackMax() {
        return cashBackMax;
    }
    
    public void setCashBackMax(long cashBackMax) {
        this.cashBackMax = cashBackMax;
    }
    
    public boolean isDailyLimitEnabled() {
        return dailyLimitEnabled;
    }
    
    public void setDailyLimitEnabled(boolean dailyLimitEnabled) {
        this.dailyLimitEnabled = dailyLimitEnabled;
    }
    
    public int getDailyTransactionLimit() {
        return dailyTransactionLimit;
    }
    
    public void setDailyTransactionLimit(int dailyTransactionLimit) {
        this.dailyTransactionLimit = dailyTransactionLimit;
    }
    
    public long getDailyAmountLimit() {
        return dailyAmountLimit;
    }
    
    public void setDailyAmountLimit(long dailyAmountLimit) {
        this.dailyAmountLimit = dailyAmountLimit;
    }
}