package id.uniflo.uniedc.sdk.interfaces;

/**
 * Printer interface for POS SDK abstraction
 */
public interface IPrinter {
    
    /**
     * Initialize the printer
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Print text data
     * @param data Text to print
     * @return 0 for success, negative for error
     */
    int printText(String data);
    
    /**
     * Print raw byte data
     * @param data Bytes to print
     * @return 0 for success, negative for error
     */
    int printData(byte[] data);
    
    /**
     * Feed paper
     * @param lines Number of lines to feed
     * @return 0 for success, negative for error
     */
    int feedPaper(int lines);
    
    /**
     * Get printer status
     * @return Status code (0=OK, -1=Out of paper, -2=Error)
     */
    int getStatus();
    
    /**
     * Set text alignment
     * @param align 0=Left, 1=Center, 2=Right
     * @return 0 for success, negative for error
     */
    int setAlignment(int align);
    
    /**
     * Set text size
     * @param size 0=Normal, 1=Large, 2=Extra Large
     * @return 0 for success, negative for error
     */
    int setTextSize(int size);
    
    /**
     * Set bold text
     * @param bold true for bold, false for normal
     * @return 0 for success, negative for error
     */
    int setBold(boolean bold);
    
    /**
     * Print barcode
     * @param data Barcode data
     * @param type Barcode type
     * @return 0 for success, negative for error
     */
    int printBarcode(String data, int type);
    
    /**
     * Print QR code
     * @param data QR code data
     * @param size QR code size
     * @return 0 for success, negative for error
     */
    int printQRCode(String data, int size);
    
    /**
     * Cut paper (if supported)
     * @return 0 for success, negative for error
     */
    int cutPaper();
    
    /**
     * Release printer resources
     */
    void release();
}