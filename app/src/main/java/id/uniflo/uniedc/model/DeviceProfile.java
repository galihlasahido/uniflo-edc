package id.uniflo.uniedc.model;

import java.util.Date;
import java.util.List;

public class DeviceProfile {
    
    // Device identification
    private String deviceId;
    private String serialNumber;
    private String merchantId;
    private String terminalId;
    private String deviceModel;
    
    // Profile information
    private String profileId;
    private String profileName;
    private String profileVersion;
    private Date lastUpdated;
    private boolean isActive;
    
    // Feature permissions
    private FeaturePermissions features;
    
    // Device settings
    private DeviceSettings settings;
    
    public DeviceProfile() {
        this.features = new FeaturePermissions();
        this.settings = new DeviceSettings();
        this.isActive = false;
    }
    
    // Getters and setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }
    
    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }
    
    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    
    public String getProfileVersion() { return profileVersion; }
    public void setProfileVersion(String profileVersion) { this.profileVersion = profileVersion; }
    
    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public FeaturePermissions getFeatures() { return features; }
    public void setFeatures(FeaturePermissions features) { this.features = features; }
    
    public DeviceSettings getSettings() { return settings; }
    public void setSettings(DeviceSettings settings) { this.settings = settings; }
    
    // Feature access methods
    public boolean canAccessSales() { return features.isSalesEnabled(); }
    public boolean canAccessBalanceInquiry() { return features.isBalanceInquiryEnabled(); }
    public boolean canAccessCashWithdrawal() { return features.isCashWithdrawalEnabled(); }
    public boolean canAccessTransfer() { return features.isTransferEnabled(); }
    public boolean canAccessPinManagement() { return features.isPinManagementEnabled(); }
    public boolean canAccessSettings() { return features.isSettingsEnabled(); }
    public boolean canAccessReports() { return features.isReportsEnabled(); }
    
    // Check if profile is valid and not expired
    public boolean isValidProfile() {
        if (!isActive || profileId == null || profileId.isEmpty()) {
            return false;
        }
        
        // Check if profile is not too old (e.g., older than 30 days)
        if (lastUpdated != null) {
            long daysSinceUpdate = (System.currentTimeMillis() - lastUpdated.getTime()) / (1000 * 60 * 60 * 24);
            return daysSinceUpdate < 30;
        }
        
        return true;
    }
    
    // Inner class for feature permissions
    public static class FeaturePermissions {
        private boolean salesEnabled = false;
        private boolean balanceInquiryEnabled = false;
        private boolean cashWithdrawalEnabled = false;
        private boolean transferEnabled = false;
        private boolean pinManagementEnabled = false;
        private boolean settingsEnabled = false;
        private boolean reportsEnabled = false;
        private boolean offlineMode = false;
        
        // Transaction limits
        private long maxTransactionAmount = 0;
        private long maxDailyAmount = 0;
        private int maxTransactionsPerDay = 0;
        
        // Getters and setters
        public boolean isSalesEnabled() { return salesEnabled; }
        public void setSalesEnabled(boolean salesEnabled) { this.salesEnabled = salesEnabled; }
        
        public boolean isBalanceInquiryEnabled() { return balanceInquiryEnabled; }
        public void setBalanceInquiryEnabled(boolean balanceInquiryEnabled) { this.balanceInquiryEnabled = balanceInquiryEnabled; }
        
        public boolean isCashWithdrawalEnabled() { return cashWithdrawalEnabled; }
        public void setCashWithdrawalEnabled(boolean cashWithdrawalEnabled) { this.cashWithdrawalEnabled = cashWithdrawalEnabled; }
        
        public boolean isTransferEnabled() { return transferEnabled; }
        public void setTransferEnabled(boolean transferEnabled) { this.transferEnabled = transferEnabled; }
        
        public boolean isPinManagementEnabled() { return pinManagementEnabled; }
        public void setPinManagementEnabled(boolean pinManagementEnabled) { this.pinManagementEnabled = pinManagementEnabled; }
        
        public boolean isSettingsEnabled() { return settingsEnabled; }
        public void setSettingsEnabled(boolean settingsEnabled) { this.settingsEnabled = settingsEnabled; }
        
        public boolean isReportsEnabled() { return reportsEnabled; }
        public void setReportsEnabled(boolean reportsEnabled) { this.reportsEnabled = reportsEnabled; }
        
        public boolean isOfflineMode() { return offlineMode; }
        public void setOfflineMode(boolean offlineMode) { this.offlineMode = offlineMode; }
        
        public long getMaxTransactionAmount() { return maxTransactionAmount; }
        public void setMaxTransactionAmount(long maxTransactionAmount) { this.maxTransactionAmount = maxTransactionAmount; }
        
        public long getMaxDailyAmount() { return maxDailyAmount; }
        public void setMaxDailyAmount(long maxDailyAmount) { this.maxDailyAmount = maxDailyAmount; }
        
        public int getMaxTransactionsPerDay() { return maxTransactionsPerDay; }
        public void setMaxTransactionsPerDay(int maxTransactionsPerDay) { this.maxTransactionsPerDay = maxTransactionsPerDay; }
    }
    
    // Inner class for device settings
    public static class DeviceSettings {
        private String timeZone = "Asia/Jakarta";
        private String language = "id";
        private String currency = "IDR";
        private boolean printReceipt = true;
        private boolean requireSignature = false;
        private int sessionTimeout = 300; // seconds
        
        // Network settings
        private String serverUrl;
        private int connectionTimeout = 30; // seconds
        private boolean useSSL = true;
        
        // Getters and setters
        public String getTimeZone() { return timeZone; }
        public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public boolean isPrintReceipt() { return printReceipt; }
        public void setPrintReceipt(boolean printReceipt) { this.printReceipt = printReceipt; }
        
        public boolean isRequireSignature() { return requireSignature; }
        public void setRequireSignature(boolean requireSignature) { this.requireSignature = requireSignature; }
        
        public int getSessionTimeout() { return sessionTimeout; }
        public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }
        
        public String getServerUrl() { return serverUrl; }
        public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
        
        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        
        public boolean isUseSSL() { return useSSL; }
        public void setUseSSL(boolean useSSL) { this.useSSL = useSSL; }
    }
}