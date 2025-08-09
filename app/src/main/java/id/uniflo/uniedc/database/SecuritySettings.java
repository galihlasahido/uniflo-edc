package id.uniflo.uniedc.database;

public class SecuritySettings {
    private boolean pinVerification;
    private String adminPin;
    private int maxPinAttempts;
    private boolean voidPassword;
    private boolean settlementPassword;
    private boolean refundPassword;
    private String keyStatus;
    private String lastKeyDownload;
    
    public SecuritySettings() {
        // Default constructor
    }
    
    // Getters and Setters
    public boolean isPinVerification() {
        return pinVerification;
    }
    
    public void setPinVerification(boolean pinVerification) {
        this.pinVerification = pinVerification;
    }
    
    public String getAdminPin() {
        return adminPin;
    }
    
    public void setAdminPin(String adminPin) {
        this.adminPin = adminPin;
    }
    
    public int getMaxPinAttempts() {
        return maxPinAttempts;
    }
    
    public void setMaxPinAttempts(int maxPinAttempts) {
        this.maxPinAttempts = maxPinAttempts;
    }
    
    public boolean isVoidPassword() {
        return voidPassword;
    }
    
    public void setVoidPassword(boolean voidPassword) {
        this.voidPassword = voidPassword;
    }
    
    public boolean isSettlementPassword() {
        return settlementPassword;
    }
    
    public void setSettlementPassword(boolean settlementPassword) {
        this.settlementPassword = settlementPassword;
    }
    
    public boolean isRefundPassword() {
        return refundPassword;
    }
    
    public void setRefundPassword(boolean refundPassword) {
        this.refundPassword = refundPassword;
    }
    
    public String getKeyStatus() {
        return keyStatus;
    }
    
    public void setKeyStatus(String keyStatus) {
        this.keyStatus = keyStatus;
    }
    
    public String getLastKeyDownload() {
        return lastKeyDownload;
    }
    
    public void setLastKeyDownload(String lastKeyDownload) {
        this.lastKeyDownload = lastKeyDownload;
    }
}