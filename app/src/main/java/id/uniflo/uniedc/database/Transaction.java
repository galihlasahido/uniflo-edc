package id.uniflo.uniedc.database;

import java.util.Date;

public class Transaction {
    
    // Transaction types
    public static final String TYPE_BALANCE_INQUIRY = "BALANCE_INQUIRY";
    public static final String TYPE_SALE = "SALE";
    public static final String TYPE_TRANSFER = "TRANSFER";
    public static final String TYPE_VOID = "VOID";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_SETTLEMENT = "SETTLEMENT";
    
    // Transaction status
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_REVERSED = "REVERSED";
    public static final String STATUS_VOIDED = "VOIDED";
    
    private long id;
    private String transactionType;
    private String status;
    private long amount;
    private String cardNumber;
    private String cardHolderName;
    private String cardType;
    private String entryMode;
    private String terminalId;
    private String merchantId;
    private String batchNumber;
    private String traceNumber;
    private String referenceNumber;
    private String approvalCode;
    private String responseCode;
    private String responseMessage;
    private String emvData;
    private String pinBlock;
    private String arqc;
    private String atc;
    private String tvr;
    private String tsi;
    private String aid;
    private String applicationLabel;
    private Date transactionDate;
    private boolean isVoided;
    private String voidReferenceNumber;
    private Date voidDate;
    private String rawRequest;
    private String rawResponse;
    private long createdAt;
    private long updatedAt;
    
    // Constructor
    public Transaction() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public String getEntryMode() {
        return entryMode;
    }
    
    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }
    
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
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public String getApprovalCode() {
        return approvalCode;
    }
    
    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
    
    public String getEmvData() {
        return emvData;
    }
    
    public void setEmvData(String emvData) {
        this.emvData = emvData;
    }
    
    public String getPinBlock() {
        return pinBlock;
    }
    
    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }
    
    public String getArqc() {
        return arqc;
    }
    
    public void setArqc(String arqc) {
        this.arqc = arqc;
    }
    
    public String getAtc() {
        return atc;
    }
    
    public void setAtc(String atc) {
        this.atc = atc;
    }
    
    public String getTvr() {
        return tvr;
    }
    
    public void setTvr(String tvr) {
        this.tvr = tvr;
    }
    
    public String getTsi() {
        return tsi;
    }
    
    public void setTsi(String tsi) {
        this.tsi = tsi;
    }
    
    public String getAid() {
        return aid;
    }
    
    public void setAid(String aid) {
        this.aid = aid;
    }
    
    public String getApplicationLabel() {
        return applicationLabel;
    }
    
    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;
    }
    
    public Date getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public boolean isVoided() {
        return isVoided;
    }
    
    public void setVoided(boolean voided) {
        isVoided = voided;
    }
    
    public String getVoidReferenceNumber() {
        return voidReferenceNumber;
    }
    
    public void setVoidReferenceNumber(String voidReferenceNumber) {
        this.voidReferenceNumber = voidReferenceNumber;
    }
    
    public Date getVoidDate() {
        return voidDate;
    }
    
    public void setVoidDate(Date voidDate) {
        this.voidDate = voidDate;
    }
    
    public String getRawRequest() {
        return rawRequest;
    }
    
    public void setRawRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }
    
    public String getRawResponse() {
        return rawResponse;
    }
    
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper method to mask card number
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        int length = cardNumber.length();
        String first6 = cardNumber.substring(0, 6);
        String last4 = cardNumber.substring(length - 4);
        StringBuilder masked = new StringBuilder(first6);
        for (int i = 6; i < length - 4; i++) {
            masked.append("*");
        }
        masked.append(last4);
        return masked.toString();
    }
}