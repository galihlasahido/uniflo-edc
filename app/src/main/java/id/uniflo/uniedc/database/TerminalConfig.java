package id.uniflo.uniedc.database;

public class TerminalConfig {
    private String terminalId;
    private String merchantId;
    private String merchantName;
    private String merchantAddress;
    private String merchantCity;
    private String merchantPhone;
    private String currency;
    private String language;
    private String dateFormat;
    private boolean tipEnabled;
    private boolean signatureRequired;
    private boolean offlineMode;
    private String batchNumber;
    private String traceNumber;
    private String invoiceNumber;
    private String acquiringInstitutionCode;
    
    public TerminalConfig() {
        // Default constructor
    }
    
    // Getters and Setters
    public String getTerminalId() {
        return terminalId;
    }
    
    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    public String getMerchantAddress() {
        return merchantAddress;
    }
    
    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }
    
    public String getMerchantCity() {
        return merchantCity;
    }
    
    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }
    
    public String getMerchantPhone() {
        return merchantPhone;
    }
    
    public void setMerchantPhone(String merchantPhone) {
        this.merchantPhone = merchantPhone;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getDateFormat() {
        return dateFormat;
    }
    
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    public boolean isTipEnabled() {
        return tipEnabled;
    }
    
    public void setTipEnabled(boolean tipEnabled) {
        this.tipEnabled = tipEnabled;
    }
    
    public boolean isSignatureRequired() {
        return signatureRequired;
    }
    
    public void setSignatureRequired(boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }
    
    public boolean isOfflineMode() {
        return offlineMode;
    }
    
    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
    
    public String getTraceNumber() {
        return traceNumber;
    }
    
    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getAcquiringInstitutionCode() {
        return acquiringInstitutionCode;
    }
    
    public void setAcquiringInstitutionCode(String acquiringInstitutionCode) {
        this.acquiringInstitutionCode = acquiringInstitutionCode;
    }
}