package id.uniflo.uniedc.sdk.interfaces;

/**
 * PSAM (Programmable Security Application Module) interface for POS SDK abstraction
 */
public interface IPsam {
    
    // PSAM slot numbers
    int SLOT_1 = 0;
    int SLOT_2 = 1;
    int SLOT_3 = 2;
    int SLOT_4 = 3;
    
    // Card types
    int CARD_TYPE_T0 = 0;
    int CARD_TYPE_T1 = 1;
    
    /**
     * Initialize PSAM reader
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Open PSAM reader
     * @return 0 for success, negative for error
     */
    int open();
    
    /**
     * Check if PSAM card is present
     * @param slot PSAM slot number
     * @return true if present, false otherwise
     */
    boolean isCardPresent(int slot);
    
    /**
     * Power on PSAM card
     * @param slot PSAM slot number
     * @return ATR (Answer To Reset) data or null if failed
     */
    byte[] powerOn(int slot);
    
    /**
     * Power off PSAM card
     * @param slot PSAM slot number
     * @return 0 for success, negative for error
     */
    int powerOff(int slot);
    
    /**
     * Send APDU command to PSAM
     * @param slot PSAM slot number
     * @param apdu APDU command
     * @return Response data or null if failed
     */
    byte[] sendApdu(int slot, byte[] apdu);
    
    /**
     * Get card type
     * @param slot PSAM slot number
     * @return Card type (T0 or T1) or negative for error
     */
    int getCardType(int slot);
    
    /**
     * Get card protocol
     * @param slot PSAM slot number
     * @return Protocol string or null if failed
     */
    String getProtocol(int slot);
    
    /**
     * Reset PSAM card
     * @param slot PSAM slot number
     * @param warm true for warm reset, false for cold reset
     * @return ATR data or null if failed
     */
    byte[] reset(int slot, boolean warm);
    
    /**
     * Set communication parameters
     * @param slot PSAM slot number
     * @param baudRate Baud rate
     * @return 0 for success, negative for error
     */
    int setParameters(int slot, int baudRate);
    
    /**
     * Close PSAM reader
     * @return 0 for success, negative for error
     */
    int close();
    
    /**
     * Release PSAM resources
     */
    void release();
}