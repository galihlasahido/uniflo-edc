package id.uniflo.uniedc.sdk.interfaces;

/**
 * Card reader interface for POS SDK abstraction
 */
public interface ICardReader {
    
    // Card types
    int CARD_TYPE_MAG = 1;
    int CARD_TYPE_IC = 2;
    int CARD_TYPE_NFC = 4;
    
    /**
     * Initialize card reader
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Open card reader
     * @param cardTypes Combination of card types (MAG|IC|NFC)
     * @param timeout Timeout in seconds
     * @param listener Card detection listener
     * @return 0 for success, negative for error
     */
    int open(int cardTypes, int timeout, ICardDetectListener listener);
    
    /**
     * Close card reader
     * @return 0 for success, negative for error
     */
    int close();
    
    /**
     * Power on IC card
     * @return ATR data or null if failed
     */
    byte[] powerOn();
    
    /**
     * Power off IC card
     * @return 0 for success, negative for error
     */
    int powerOff();
    
    /**
     * Send APDU command to IC card
     * @param apdu APDU command
     * @return Response data or null if failed
     */
    byte[] sendApdu(byte[] apdu);
    
    /**
     * Get magnetic card track data
     * @param track Track number (1, 2, or 3)
     * @return Track data or null if not available
     */
    String getTrackData(int track);
    
    /**
     * Check if card is present
     * @return true if card present, false otherwise
     */
    boolean isCardPresent();
    
    /**
     * Get card type detected
     * @return Card type (MAG, IC, or NFC)
     */
    int getCardType();
    
    /**
     * Release card reader resources
     */
    void release();
    
    /**
     * Card detection listener interface
     */
    interface ICardDetectListener {
        /**
         * Called when card is detected
         * @param cardType Type of card detected
         */
        void onCardDetected(int cardType);
        
        /**
         * Called when card is removed
         */
        void onCardRemoved();
        
        /**
         * Called on timeout
         */
        void onTimeout();
        
        /**
         * Called on error
         * @param errorCode Error code
         * @param message Error message
         */
        void onError(int errorCode, String message);
    }
}