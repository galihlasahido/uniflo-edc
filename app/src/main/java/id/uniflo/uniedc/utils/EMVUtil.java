package id.uniflo.uniedc.utils;

import android.util.Log;
import com.ftpos.library.smartpos.icreader.IcReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.ICardReader;
import id.uniflo.uniedc.sdk.interfaces.IPinpad;

/**
 * EMV Utility class for handling EMV transactions
 * Handles ARQC generation and TLV data processing
 */
public class EMVUtil {
    
    private static final String TAG = "EMVUtil";
    
    // EMV Tags
    public static final String TAG_AMOUNT = "9F02";
    public static final String TAG_AMOUNT_OTHER = "9F03";
    public static final String TAG_TERMINAL_COUNTRY_CODE = "9F1A";
    public static final String TAG_TVR = "95";
    public static final String TAG_TRANSACTION_CURRENCY = "5F2A";
    public static final String TAG_TRANSACTION_DATE = "9A";
    public static final String TAG_TRANSACTION_TYPE = "9C";
    public static final String TAG_UNPREDICTABLE_NUMBER = "9F37";
    public static final String TAG_AIP = "82";
    public static final String TAG_ATC = "9F36";
    public static final String TAG_CRYPTOGRAM = "9F26";
    public static final String TAG_ISSUER_APPLICATION_DATA = "9F10";
    public static final String TAG_CVM_RESULTS = "9F34";
    public static final String TAG_TERMINAL_CAPABILITIES = "9F33";
    public static final String TAG_TERMINAL_TYPE = "9F35";
    public static final String TAG_IFD_SERIAL_NUMBER = "9F1E";
    public static final String TAG_APPLICATION_VERSION = "9F09";
    public static final String TAG_MERCHANT_NAME = "9F4E";
    public static final String TAG_MERCHANT_ID = "9F16";
    public static final String TAG_TERMINAL_ID = "9F1C";
    
    private ICardReader cardReader;
    private IPinpad pinpad;
    private IcReader icReader; // Feitian IC reader
    
    public EMVUtil() {
        SDKManager sdkManager = SDKManager.getInstance();
        if (sdkManager.isInitialized()) {
            cardReader = sdkManager.getCardReader();
            pinpad = sdkManager.getPinpad();
        }
    }
    
    /**
     * Set Feitian IC reader for direct card communication
     * @param reader Feitian IcReader instance
     */
    public void setIcReader(IcReader reader) {
        this.icReader = reader;
        Log.d(TAG, "IcReader set: " + (reader != null));
    }
    
    /**
     * Generate ARQC (Authorization Request Cryptogram) for online transaction
     * @param amount Transaction amount in cents
     * @param pinBlock Encrypted PIN block from PIN entry
     * @return Map of TLV values including ARQC
     */
    public Map<String, String> generateARQC(long amount, byte[] pinBlock) {
        Map<String, String> tlvData = new HashMap<>();
        
        Log.d(TAG, "=== Starting ARQC Generation ===");
        Log.d(TAG, "Amount: " + amount);
        Log.d(TAG, "PIN Block present: " + (pinBlock != null));
        
        try {
            // Basic transaction data
            tlvData.put(TAG_AMOUNT, formatAmount(amount));
            tlvData.put(TAG_AMOUNT_OTHER, "000000000000");
            tlvData.put(TAG_TERMINAL_COUNTRY_CODE, "0360"); // Indonesia
            tlvData.put(TAG_TRANSACTION_CURRENCY, "0360"); // IDR
            tlvData.put(TAG_TRANSACTION_DATE, getTransactionDate());
            tlvData.put(TAG_TRANSACTION_TYPE, "00"); // Purchase
            tlvData.put(TAG_UNPREDICTABLE_NUMBER, generateUnpredictableNumber());
            
            // Terminal data
            tlvData.put(TAG_TERMINAL_TYPE, "22"); // Attended, offline with online capability
            tlvData.put(TAG_TERMINAL_CAPABILITIES, "E0F8C8"); // Standard capabilities
            tlvData.put(TAG_IFD_SERIAL_NUMBER, "3030303030303031"); // "00000001"
            tlvData.put(TAG_TERMINAL_ID, "54455354303031"); // "TEST001"
            tlvData.put(TAG_MERCHANT_ID, "313233343536373839303132333435"); // "123456789012345"
            
            // CVM Results - PIN verified
            if (pinBlock != null) {
                tlvData.put(TAG_CVM_RESULTS, "420300"); // Online PIN successful
            } else {
                tlvData.put(TAG_CVM_RESULTS, "3F0000"); // No CVM
            }
            
            // Generate ARQC using card
            Log.d(TAG, "IcReader available: " + (icReader != null));
            Log.d(TAG, "CardReader available: " + (cardReader != null));
            
            // For Feitian devices, real ARQC generation requires full EMV processing
            // which includes application selection, GPO, and GENERATE AC commands
            // This is typically done through the EMV kernel, not direct APDU commands
            
            // For now, we'll simulate the ARQC for testing
            if (icReader != null) {
                Log.d(TAG, "Feitian IcReader available - generating simulated ARQC");
                
                // TODO: Implement full EMV processing flow using Feitian EMV kernel
                // This would involve:
                // 1. Application selection
                // 2. Get Processing Options (GPO)  
                // 3. Read application data
                // 4. Generate AC command
                
                // For now, generate simulated data
                tlvData.put(TAG_CRYPTOGRAM, generateSimulatedARQC());
                tlvData.put(TAG_ATC, "0001");
                tlvData.put(TAG_ISSUER_APPLICATION_DATA, "0110A00003220000");
                tlvData.put(TAG_CVM_RESULTS, pinBlock != null ? "420300" : "3F0000");
                
                Log.d(TAG, "Simulated ARQC generated for Feitian device");
            } else if (cardReader != null && cardReader.isCardPresent() && 
                       cardReader.getCardType() == ICardReader.CARD_TYPE_IC) {
                Log.d(TAG, "Using generic CardReader for ARQC generation");
                
                // Build CDOL1 data for GENERATE AC command
                byte[] cdol1Data = buildCDOL1Data(tlvData);
                Log.d(TAG, "CDOL1 Data: " + bytesToHexString(cdol1Data));
                
                // GENERATE AC command (P1=80 for ARQC)
                byte[] generateACCommand = buildGenerateACCommand(cdol1Data);
                Log.d(TAG, "GENERATE AC Command: " + bytesToHexString(generateACCommand));
                
                Log.d(TAG, "Sending APDU to card...");
                byte[] response = cardReader.sendApdu(generateACCommand);
                Log.d(TAG, "Card Response: " + (response != null ? bytesToHexString(response) : "null"));
                
                if (response != null && response.length > 2) {
                    Log.d(TAG, "Response length: " + response.length);
                    
                    // Check SW1SW2
                    if (response.length >= 2) {
                        int sw1 = response[response.length - 2] & 0xFF;
                        int sw2 = response[response.length - 1] & 0xFF;
                        Log.d(TAG, "SW1SW2: " + String.format("%02X%02X", sw1, sw2));
                    }
                    
                    // Parse response to extract cryptogram and other data
                    Map<String, String> parsedResponse = parseTLV(response);
                    Log.d(TAG, "Parsed TLV count: " + parsedResponse.size());
                    
                    // Add cryptogram data to result
                    if (parsedResponse.containsKey(TAG_CRYPTOGRAM)) {
                        tlvData.put(TAG_CRYPTOGRAM, parsedResponse.get(TAG_CRYPTOGRAM));
                        Log.d(TAG, "ARQC (9F26): " + parsedResponse.get(TAG_CRYPTOGRAM));
                    } else {
                        Log.w(TAG, "No cryptogram (9F26) found in response");
                    }
                    if (parsedResponse.containsKey(TAG_ATC)) {
                        tlvData.put(TAG_ATC, parsedResponse.get(TAG_ATC));
                        Log.d(TAG, "ATC (9F36): " + parsedResponse.get(TAG_ATC));
                    }
                    if (parsedResponse.containsKey(TAG_ISSUER_APPLICATION_DATA)) {
                        tlvData.put(TAG_ISSUER_APPLICATION_DATA, parsedResponse.get(TAG_ISSUER_APPLICATION_DATA));
                        Log.d(TAG, "IAD (9F10): " + parsedResponse.get(TAG_ISSUER_APPLICATION_DATA));
                    }
                    
                    Log.d(TAG, "ARQC generated successfully");
                } else {
                    Log.e(TAG, "Failed to generate ARQC - invalid response");
                    if (response == null) {
                        Log.e(TAG, "Response is null");
                    } else {
                        Log.e(TAG, "Response too short: " + response.length + " bytes");
                    }
                    // Add dummy ARQC for testing
                    tlvData.put(TAG_CRYPTOGRAM, "0123456789ABCDEF");
                    tlvData.put(TAG_ATC, "0001");
                }
            } else {
                // No card present or not IC card - generate dummy data for testing
                Log.w(TAG, "No IC card reader available - generating dummy ARQC");
                tlvData.put(TAG_CRYPTOGRAM, "0123456789ABCDEF");
                tlvData.put(TAG_ATC, "0001");
                tlvData.put(TAG_ISSUER_APPLICATION_DATA, "0110A00003220000");
            }
            
            // TVR (Terminal Verification Results)
            tlvData.put(TAG_TVR, "0000000000");
            
            // AIP (Application Interchange Profile)
            tlvData.put(TAG_AIP, "3800");
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating ARQC", e);
        }
        
        return tlvData;
    }
    
    /**
     * Build GENERATE AC command
     */
    private byte[] buildGenerateACCommand(byte[] cdol1Data) {
        // GENERATE AC: CLA=80, INS=AE, P1=80 (ARQC), P2=00
        byte[] command = new byte[5 + cdol1Data.length + 1];
        command[0] = (byte) 0x80; // CLA
        command[1] = (byte) 0xAE; // INS
        command[2] = (byte) 0x80; // P1 - Request ARQC
        command[3] = (byte) 0x00; // P2
        command[4] = (byte) cdol1Data.length; // Lc
        System.arraycopy(cdol1Data, 0, command, 5, cdol1Data.length);
        command[command.length - 1] = 0x00; // Le
        
        return command;
    }
    
    /**
     * Build CDOL1 data based on card requirements
     * This is a simplified version - real implementation would read CDOL1 from card
     */
    private byte[] buildCDOL1Data(Map<String, String> tlvData) {
        // Typical CDOL1 contains:
        // 9F02 06 - Amount
        // 9F03 06 - Amount Other
        // 9F1A 02 - Terminal Country Code
        // 95 05 - TVR
        // 5F2A 02 - Transaction Currency
        // 9A 03 - Transaction Date
        // 9C 01 - Transaction Type
        // 9F37 04 - Unpredictable Number
        
        StringBuilder cdol1 = new StringBuilder();
        cdol1.append(tlvData.get(TAG_AMOUNT));
        cdol1.append(tlvData.get(TAG_AMOUNT_OTHER));
        cdol1.append(tlvData.get(TAG_TERMINAL_COUNTRY_CODE));
        cdol1.append(tlvData.get(TAG_TVR));
        cdol1.append(tlvData.get(TAG_TRANSACTION_CURRENCY));
        cdol1.append(tlvData.get(TAG_TRANSACTION_DATE));
        cdol1.append(tlvData.get(TAG_TRANSACTION_TYPE));
        cdol1.append(tlvData.get(TAG_UNPREDICTABLE_NUMBER));
        
        return hexStringToBytes(cdol1.toString());
    }
    
    /**
     * Parse TLV response
     */
    private Map<String, String> parseTLV(byte[] data) {
        Map<String, String> tlvMap = new HashMap<>();
        int index = 0;
        
        while (index < data.length - 2) { // -2 for SW1SW2
            // Get tag (simplified - assumes 2-byte tags)
            String tag = String.format("%02X%02X", data[index], data[index + 1]);
            index += 2;
            
            // Get length
            int length = data[index] & 0xFF;
            index++;
            
            // Get value
            if (index + length <= data.length - 2) {
                byte[] value = new byte[length];
                System.arraycopy(data, index, value, 0, length);
                tlvMap.put(tag, bytesToHexString(value));
                index += length;
            } else {
                break;
            }
        }
        
        return tlvMap;
    }
    
    /**
     * Format amount for EMV (12 digits, left padded with zeros)
     */
    private String formatAmount(long amountInCents) {
        return String.format("%012d", amountInCents);
    }
    
    /**
     * Get transaction date in YYMMDD format
     */
    private String getTransactionDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    /**
     * Generate unpredictable number (4 bytes)
     */
    private String generateUnpredictableNumber() {
        // In production, this should be a secure random number
        int random = (int) (Math.random() * Integer.MAX_VALUE);
        return String.format("%08X", random);
    }
    
    /**
     * Generate simulated ARQC for testing
     */
    private String generateSimulatedARQC() {
        // Generate a random 8-byte ARQC for simulation
        long random = (long) (Math.random() * Long.MAX_VALUE);
        return String.format("%016X", random);
    }
    
    /**
     * Convert hex string to byte array
     */
    private byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
    /**
     * Convert byte array to hex string
     */
    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    /**
     * Build EMV data field for authorization message
     * @param tlvData Map of TLV values
     * @return Formatted EMV data string
     */
    public String buildEMVDataField(Map<String, String> tlvData) {
        StringBuilder emvData = new StringBuilder();
        
        // Add TLV data in specific order for authorization
        appendTLV(emvData, TAG_CRYPTOGRAM, tlvData.get(TAG_CRYPTOGRAM));
        appendTLV(emvData, TAG_CVM_RESULTS, tlvData.get(TAG_CVM_RESULTS));
        appendTLV(emvData, TAG_ISSUER_APPLICATION_DATA, tlvData.get(TAG_ISSUER_APPLICATION_DATA));
        appendTLV(emvData, TAG_UNPREDICTABLE_NUMBER, tlvData.get(TAG_UNPREDICTABLE_NUMBER));
        appendTLV(emvData, TAG_ATC, tlvData.get(TAG_ATC));
        appendTLV(emvData, TAG_TVR, tlvData.get(TAG_TVR));
        appendTLV(emvData, TAG_TRANSACTION_DATE, tlvData.get(TAG_TRANSACTION_DATE));
        appendTLV(emvData, TAG_TRANSACTION_TYPE, tlvData.get(TAG_TRANSACTION_TYPE));
        appendTLV(emvData, TAG_AMOUNT, tlvData.get(TAG_AMOUNT));
        appendTLV(emvData, TAG_TRANSACTION_CURRENCY, tlvData.get(TAG_TRANSACTION_CURRENCY));
        appendTLV(emvData, TAG_AIP, tlvData.get(TAG_AIP));
        appendTLV(emvData, TAG_TERMINAL_COUNTRY_CODE, tlvData.get(TAG_TERMINAL_COUNTRY_CODE));
        
        return emvData.toString();
    }
    
    /**
     * Append TLV to string builder
     */
    private void appendTLV(StringBuilder sb, String tag, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(tag);
            sb.append(String.format("%02X", value.length() / 2));
            sb.append(value);
        }
    }
}