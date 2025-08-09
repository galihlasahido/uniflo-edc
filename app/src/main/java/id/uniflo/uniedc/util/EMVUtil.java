package id.uniflo.uniedc.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for EMV (Europay, MasterCard, and Visa) operations
 * This class provides methods to build EMV data structures, extract PAN,
 * generate cryptograms, and handle common EMV tags.
 */
public class EMVUtil {
    
    private static final String TAG = "EMVUtil";
    
    // EMV Tags
    public static final String TAG_AMOUNT = "9F02";
    public static final String TAG_AMOUNT_OTHER = "9F03";
    public static final String TAG_COUNTRY_CODE = "9F1A";
    public static final String TAG_CURRENCY_CODE = "5F2A";
    public static final String TAG_TRANSACTION_DATE = "9A";
    public static final String TAG_TRANSACTION_TYPE = "9C";
    public static final String TAG_TERMINAL_CAPABILITIES = "9F33";
    public static final String TAG_TERMINAL_TYPE = "9F35";
    public static final String TAG_TVR = "95";
    public static final String TAG_TSI = "9B";
    public static final String TAG_UNPREDICTABLE_NUMBER = "9F37";
    public static final String TAG_AIP = "82";
    public static final String TAG_ATC = "9F36";
    public static final String TAG_CRYPTOGRAM = "9F26";
    public static final String TAG_ISSUER_APP_DATA = "9F10";
    public static final String TAG_CVM_RESULTS = "9F34";
    public static final String TAG_TERMINAL_COUNTRY_CODE = "9F1A";
    public static final String TAG_PAN = "5A";
    public static final String TAG_PAN_SEQUENCE = "5F34";
    public static final String TAG_TRACK2 = "57";
    
    // Transaction Types
    public static final byte TRANSACTION_TYPE_PURCHASE = 0x00;
    public static final byte TRANSACTION_TYPE_WITHDRAWAL = 0x01;
    public static final byte TRANSACTION_TYPE_BALANCE_INQUIRY = 0x31;
    public static final byte TRANSACTION_TYPE_TRANSFER = 0x40;
    
    // Country and Currency Codes
    public static final String COUNTRY_CODE_INDONESIA = "0360";
    public static final String CURRENCY_CODE_IDR = "0360";
    
    public static class TransactionType {
        public static final int PURCHASE = 0;
        public static final int WITHDRAWAL = 1;
        public static final int BALANCE_INQUIRY = 2;
        public static final int TRANSFER = 3;
    }
    
    /**
     * Build EMV data for withdrawal transaction
     * @param amount Transaction amount in smallest currency unit
     * @return TLV formatted EMV data
     */
    public static byte[] buildWithdrawalEMVData(long amount) {
        Map<String, byte[]> emvData = new HashMap<>();
        
        // Amount (9F02)
        emvData.put(TAG_AMOUNT, longToBytes(amount, 6));
        
        // Amount Other (9F03) - for withdrawal, this is 0
        emvData.put(TAG_AMOUNT_OTHER, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        
        // Terminal Country Code (9F1A)
        emvData.put(TAG_TERMINAL_COUNTRY_CODE, BytesUtils.hexStringToBytes(COUNTRY_CODE_INDONESIA));
        
        // Currency Code (5F2A)
        emvData.put(TAG_CURRENCY_CODE, BytesUtils.hexStringToBytes(CURRENCY_CODE_IDR));
        
        // Transaction Date (9A) - YYMMDD
        emvData.put(TAG_TRANSACTION_DATE, getCurrentDate());
        
        // Transaction Type (9C)
        emvData.put(TAG_TRANSACTION_TYPE, new byte[]{TRANSACTION_TYPE_WITHDRAWAL});
        
        // Terminal Capabilities (9F33)
        emvData.put(TAG_TERMINAL_CAPABILITIES, new byte[]{(byte)0xE0, (byte)0xF8, (byte)0xC8});
        
        // Terminal Type (9F35)
        emvData.put(TAG_TERMINAL_TYPE, new byte[]{0x22}); // Attended, offline with online capability
        
        // Unpredictable Number (9F37)
        emvData.put(TAG_UNPREDICTABLE_NUMBER, generateUnpredictableNumber());
        
        return buildTLV(emvData);
    }
    
    /**
     * Extract PAN from EMV record data
     * @param recordData Raw record data
     * @return PAN as string
     */
    public static String extractPAN(byte[] recordData) {
        // Look for tag 5A (PAN)
        int index = findTag(recordData, TAG_PAN);
        if (index >= 0 && index + 2 < recordData.length) {
            int length = recordData[index + 1] & 0xFF;
            if (index + 2 + length <= recordData.length) {
                byte[] panBytes = new byte[length];
                System.arraycopy(recordData, index + 2, panBytes, 0, length);
                return BytesUtils.bytesToHexString(panBytes).replace("F", "");
            }
        }
        
        // If not found in tag 5A, try to extract from track 2 (tag 57)
        index = findTag(recordData, TAG_TRACK2);
        if (index >= 0 && index + 2 < recordData.length) {
            int length = recordData[index + 1] & 0xFF;
            if (index + 2 + length <= recordData.length) {
                byte[] track2Bytes = new byte[length];
                System.arraycopy(recordData, index + 2, track2Bytes, 0, length);
                String track2 = BytesUtils.bytesToHexString(track2Bytes);
                // PAN is before 'D' separator
                int dIndex = track2.indexOf('D');
                if (dIndex > 0) {
                    return track2.substring(0, dIndex);
                }
            }
        }
        
        return "";
    }
    
    /**
     * Generate ARQC (Authorization Request Cryptogram)
     * @param txnData Transaction data
     * @return ARQC bytes
     */
    public static byte[] generateARQC(TransactionData txnData) {
        // In a real implementation, this would:
        // 1. Build CDOL1 data
        // 2. Execute GENERATE AC command with ARQC request
        // 3. Parse response and extract cryptogram
        // For demo purposes, return dummy ARQC
        return new byte[]{
            (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78,
            (byte)0x9A, (byte)0xBC, (byte)0xDE, (byte)0xF0
        };
    }
    
    /**
     * Build TLV structure from map of tags and values
     * @param data Map of tag->value
     * @return TLV byte array
     */
    private static byte[] buildTLV(Map<String, byte[]> data) {
        int totalLength = 0;
        for (Map.Entry<String, byte[]> entry : data.entrySet()) {
            totalLength += entry.getKey().length() / 2; // Tag length
            totalLength += 1; // Length byte
            totalLength += entry.getValue().length; // Value length
        }
        
        byte[] tlv = new byte[totalLength];
        int offset = 0;
        
        for (Map.Entry<String, byte[]> entry : data.entrySet()) {
            // Add tag
            byte[] tag = BytesUtils.hexStringToBytes(entry.getKey());
            System.arraycopy(tag, 0, tlv, offset, tag.length);
            offset += tag.length;
            
            // Add length
            tlv[offset++] = (byte)entry.getValue().length;
            
            // Add value
            System.arraycopy(entry.getValue(), 0, tlv, offset, entry.getValue().length);
            offset += entry.getValue().length;
        }
        
        return tlv;
    }
    
    /**
     * Find tag in TLV data
     * @param data TLV data
     * @param tag Tag to find (hex string)
     * @return Index of tag, or -1 if not found
     */
    private static int findTag(byte[] data, String tag) {
        byte[] tagBytes = BytesUtils.hexStringToBytes(tag);
        for (int i = 0; i < data.length - tagBytes.length; i++) {
            boolean found = true;
            for (int j = 0; j < tagBytes.length; j++) {
                if (data[i + j] != tagBytes[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i + tagBytes.length;
            }
        }
        return -1;
    }
    
    /**
     * Convert long to BCD bytes
     * @param value Value to convert
     * @param length Desired byte length
     * @return BCD encoded bytes
     */
    private static byte[] longToBytes(long value, int length) {
        byte[] result = new byte[length];
        String valueStr = String.format("%0" + (length * 2) + "d", value);
        for (int i = 0; i < length; i++) {
            int digit1 = valueStr.charAt(i * 2) - '0';
            int digit2 = valueStr.charAt(i * 2 + 1) - '0';
            result[i] = (byte)((digit1 << 4) | digit2);
        }
        return result;
    }
    
    /**
     * Get current date in YYMMDD format
     * @return Date bytes
     */
    private static byte[] getCurrentDate() {
        // For demo, return fixed date
        // In real implementation, use Calendar
        return new byte[]{0x24, 0x12, 0x18}; // 2024-12-18
    }
    
    /**
     * Generate unpredictable number
     * @return 4 random bytes
     */
    private static byte[] generateUnpredictableNumber() {
        // In real implementation, use secure random
        return new byte[]{0x12, 0x34, 0x56, 0x78};
    }
    
    /**
     * Transaction data holder
     */
    public static class TransactionData {
        private long amount;
        private int transactionType;
        private byte[] cardData;
        private byte[] pinBlock;
        
        public long getAmount() {
            return amount;
        }
        
        public void setAmount(long amount) {
            this.amount = amount;
        }
        
        public int getTransactionType() {
            return transactionType;
        }
        
        public void setTransactionType(int transactionType) {
            this.transactionType = transactionType;
        }
        
        public byte[] getCardData() {
            return cardData;
        }
        
        public void setCardData(byte[] cardData) {
            this.cardData = cardData;
        }
        
        public byte[] getPinBlock() {
            return pinBlock;
        }
        
        public void setPinBlock(byte[] pinBlock) {
            this.pinBlock = pinBlock;
        }
    }
}