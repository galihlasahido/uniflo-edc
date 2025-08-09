package id.uniflo.uniedc.sdk.interfaces;

/**
 * Bluetooth Screen interface for POS SDK abstraction
 * Used for external Bluetooth-connected customer displays
 */
public interface IBtScreen {
    
    // Screen types
    int SCREEN_TYPE_TEXT = 0;
    int SCREEN_TYPE_GRAPHIC = 1;
    
    // Display modes
    int MODE_NORMAL = 0;
    int MODE_INVERTED = 1;
    int MODE_SCROLL = 2;
    
    // Alignment
    int ALIGN_LEFT = 0;
    int ALIGN_CENTER = 1;
    int ALIGN_RIGHT = 2;
    
    /**
     * Initialize Bluetooth screen module
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Scan for available Bluetooth screens
     * @param listener Scan result listener
     * @param timeout Scan timeout in seconds
     * @return 0 for success, negative for error
     */
    int scan(IScanListener listener, int timeout);
    
    /**
     * Connect to Bluetooth screen
     * @param address Bluetooth MAC address
     * @return 0 for success, negative for error
     */
    int connect(String address);
    
    /**
     * Check if connected
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Get connected device info
     * @return Device info or null if not connected
     */
    DeviceInfo getDeviceInfo();
    
    /**
     * Clear screen
     * @return 0 for success, negative for error
     */
    int clear();
    
    /**
     * Display text
     * @param text Text to display
     * @param line Line number (0-based)
     * @param alignment Text alignment
     * @return 0 for success, negative for error
     */
    int displayText(String text, int line, int alignment);
    
    /**
     * Display amount with currency
     * @param amount Amount in cents
     * @param currency Currency code (e.g., "IDR", "USD")
     * @return 0 for success, negative for error
     */
    int displayAmount(long amount, String currency);
    
    /**
     * Display bitmap image
     * @param bitmap Bitmap data
     * @param x X coordinate
     * @param y Y coordinate
     * @return 0 for success, negative for error
     */
    int displayBitmap(byte[] bitmap, int x, int y);
    
    /**
     * Set display mode
     * @param mode Display mode
     * @return 0 for success, negative for error
     */
    int setMode(int mode);
    
    /**
     * Set brightness
     * @param level Brightness level (0-100)
     * @return 0 for success, negative for error
     */
    int setBrightness(int level);
    
    /**
     * Show QR code
     * @param data QR code data
     * @param size QR code size
     * @return 0 for success, negative for error
     */
    int showQRCode(String data, int size);
    
    /**
     * Show barcode
     * @param data Barcode data
     * @param type Barcode type
     * @return 0 for success, negative for error
     */
    int showBarcode(String data, int type);
    
    /**
     * Scroll text
     * @param text Text to scroll
     * @param speed Scroll speed (1-10)
     * @return 0 for success, negative for error
     */
    int scrollText(String text, int speed);
    
    /**
     * Stop scrolling
     * @return 0 for success, negative for error
     */
    int stopScroll();
    
    /**
     * Disconnect from screen
     * @return 0 for success, negative for error
     */
    int disconnect();
    
    /**
     * Release resources
     */
    void release();
    
    /**
     * Scan listener interface
     */
    interface IScanListener {
        /**
         * Called when a device is found
         * @param device Found device info
         */
        void onDeviceFound(DeviceInfo device);
        
        /**
         * Called when scan is completed
         */
        void onScanCompleted();
        
        /**
         * Called on error
         * @param errorCode Error code
         * @param message Error message
         */
        void onError(int errorCode, String message);
    }
    
    /**
     * Device info class
     */
    class DeviceInfo {
        public String name;
        public String address;
        public int screenType;
        public int width;
        public int height;
        public int maxLines;
        
        public DeviceInfo(String name, String address, int screenType, 
                         int width, int height, int maxLines) {
            this.name = name;
            this.address = address;
            this.screenType = screenType;
            this.width = width;
            this.height = height;
            this.maxLines = maxLines;
        }
    }
}