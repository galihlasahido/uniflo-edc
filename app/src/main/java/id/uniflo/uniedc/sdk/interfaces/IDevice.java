package id.uniflo.uniedc.sdk.interfaces;

/**
 * Device interface for POS SDK abstraction
 */
public interface IDevice {
    
    /**
     * Initialize device
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Get device serial number
     * @return Serial number or null if not available
     */
    String getSerialNumber();
    
    /**
     * Get device model
     * @return Model name
     */
    String getModel();
    
    /**
     * Get firmware version
     * @return Firmware version
     */
    String getFirmwareVersion();
    
    /**
     * Get battery level
     * @return Battery percentage (0-100) or -1 if not available
     */
    int getBatteryLevel();
    
    /**
     * Check if device is charging
     * @return true if charging, false otherwise
     */
    boolean isCharging();
    
    /**
     * Beep buzzer
     * @param duration Duration in milliseconds
     * @return 0 for success, negative for error
     */
    int beep(int duration);
    
    /**
     * Control LED
     * @param ledIndex LED index
     * @param on true to turn on, false to turn off
     * @return 0 for success, negative for error
     */
    int setLed(int ledIndex, boolean on);
    
    /**
     * Set LED color (for RGB LEDs)
     * @param ledIndex LED index
     * @param color RGB color value
     * @return 0 for success, negative for error
     */
    int setLedColor(int ledIndex, int color);
    
    /**
     * Get system time
     * @return System time in milliseconds
     */
    long getSystemTime();
    
    /**
     * Set system time
     * @param timeMillis Time in milliseconds
     * @return 0 for success, negative for error
     */
    int setSystemTime(long timeMillis);
    
    /**
     * Reboot device
     * @return 0 for success, negative for error
     */
    int reboot();
    
    /**
     * Get device capabilities
     * @return Capabilities flags
     */
    int getCapabilities();
    
    /**
     * Release device resources
     */
    void release();
}