package id.uniflo.uniedc.sdk.interfaces;

import java.util.List;
import java.util.Map;

/**
 * EMV Processor Interface
 * Provides methods for EMV transaction processing
 */
public interface IEmvProcessor {
    
    // Transaction types
    int TRANS_TYPE_SALE = 1;
    int TRANS_TYPE_REFUND = 2;
    int TRANS_TYPE_BALANCE_INQUIRY = 3;
    int TRANS_TYPE_PREAUTH = 4;
    int TRANS_TYPE_CASH_ADVANCE = 5;
    
    /**
     * Initialize EMV processor
     * @return 0 on success, error code otherwise
     */
    int init();
    
    /**
     * Start an EMV transaction
     * @param transData Transaction data map containing:
     *                  - transType: Transaction type (required)
     *                  - amount: Transaction amount in cents (required)
     *                  - otherAmount: Other amount in cents (optional)
     *                  - currencyCode: Currency code (optional, default 360 for IDR)
     * @param listener Transaction event listener
     */
    void startTransaction(Map<String, Object> transData, IEmvTransactionListener listener);
    
    /**
     * Select application from list
     * @param index Application index to select
     */
    void selectApplication(int index);
    
    /**
     * Confirm card information
     * @param confirm true to confirm, false to cancel
     */
    void confirmCardInfo(boolean confirm);
    
    /**
     * Input PIN
     * @param pin PIN value (null to cancel)
     */
    void inputPin(String pin);
    
    /**
     * Cancel PIN input
     */
    void cancelPin();
    
    /**
     * Import online processing result
     * @param approved true if approved, false if declined
     * @param onlineData Online response data containing:
     *                   - responseCode: Response code (e.g., "00" for approval)
     *                   - authCode: Authorization code
     *                   - issuerScript: Issuer script (optional)
     */
    void importOnlineResult(boolean approved, Map<String, String> onlineData);
    
    /**
     * Get TLV data by tag
     * @param tag EMV tag (e.g., "9F26" for ARQC)
     * @return TLV value in hex string format, null if not found
     */
    String getTlvData(String tag);
    
    /**
     * Cancel current transaction
     */
    void cancelTransaction();
    
    /**
     * Release resources
     */
    void release();
    
    /**
     * EMV Transaction Event Listener
     */
    interface IEmvTransactionListener {
        /**
         * Called when application selection is required
         * @param appList List of application names
         */
        void onSelectApplication(List<String> appList);
        
        /**
         * Called to confirm card information
         * @param cardNo Card number (masked)
         * @param cardType Card type (e.g., "VISA", "MASTERCARD")
         */
        void onConfirmCardInfo(String cardNo, String cardType);
        
        /**
         * Called when PIN input is required
         * @param isOnlinePin true for online PIN, false for offline PIN
         * @param retryTimes Remaining retry times
         */
        void onRequestPin(boolean isOnlinePin, int retryTimes);
        
        /**
         * Called when online processing is required
         */
        void onRequestOnline();
        
        /**
         * Called when transaction is complete
         * @param result Transaction result code (0 for success)
         * @param data Transaction data including TLV values
         */
        void onTransactionResult(int result, Map<String, String> data);
        
        /**
         * Called on error
         * @param errorCode Error code
         * @param errorMsg Error message
         */
        void onError(int errorCode, String errorMsg);
    }
}