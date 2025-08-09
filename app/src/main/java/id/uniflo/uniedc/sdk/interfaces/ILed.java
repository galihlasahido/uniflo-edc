package id.uniflo.uniedc.sdk.interfaces;

/**
 * LED interface for POS SDK abstraction
 */
public interface ILed {
    
    // LED colors
    int LED_COLOR_OFF = 0;
    int LED_COLOR_RED = 1;
    int LED_COLOR_GREEN = 2;
    int LED_COLOR_BLUE = 3;
    int LED_COLOR_YELLOW = 4;
    int LED_COLOR_MAGENTA = 5;
    int LED_COLOR_CYAN = 6;
    int LED_COLOR_WHITE = 7;
    
    // LED modes
    int LED_MODE_STATIC = 0;
    int LED_MODE_BLINK = 1;
    int LED_MODE_BREATH = 2;
    
    /**
     * Initialize LED
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Open LED device
     * @return 0 for success, negative for error
     */
    int open();
    
    /**
     * Turn on LED with specified color
     * @param ledIndex LED index (0-based)
     * @param color LED color constant
     * @return 0 for success, negative for error
     */
    int on(int ledIndex, int color);
    
    /**
     * Turn off LED
     * @param ledIndex LED index (0-based)
     * @return 0 for success, negative for error
     */
    int off(int ledIndex);
    
    /**
     * Set LED mode
     * @param ledIndex LED index (0-based)
     * @param mode LED mode (static, blink, breath)
     * @param onTime On time in milliseconds (for blink mode)
     * @param offTime Off time in milliseconds (for blink mode)
     * @return 0 for success, negative for error
     */
    int setMode(int ledIndex, int mode, int onTime, int offTime);
    
    /**
     * Get LED status
     * @param ledIndex LED index (0-based)
     * @return LED color if on, LED_COLOR_OFF if off, negative for error
     */
    int getStatus(int ledIndex);
    
    /**
     * Close LED device
     * @return 0 for success, negative for error
     */
    int close();
    
    /**
     * Release LED resources
     */
    void release();
}