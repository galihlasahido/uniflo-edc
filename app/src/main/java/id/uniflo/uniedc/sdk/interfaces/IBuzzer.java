package id.uniflo.uniedc.sdk.interfaces;

/**
 * Buzzer interface for POS SDK abstraction
 */
public interface IBuzzer {
    
    // Common frequencies
    int FREQ_LOW = 1000;    // 1 kHz
    int FREQ_MEDIUM = 2000; // 2 kHz
    int FREQ_HIGH = 3000;   // 3 kHz
    int FREQ_ERROR = 500;   // 500 Hz
    
    /**
     * Initialize buzzer
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Open buzzer device
     * @return 0 for success, negative for error
     */
    int open();
    
    /**
     * Beep buzzer
     * @param frequency Frequency in Hz
     * @param duration Duration in milliseconds
     * @return 0 for success, negative for error
     */
    int beep(int frequency, int duration);
    
    /**
     * Play pattern
     * @param pattern Array of [frequency, duration] pairs
     * @return 0 for success, negative for error
     */
    int playPattern(int[] pattern);
    
    /**
     * Stop buzzer
     * @return 0 for success, negative for error
     */
    int stop();
    
    /**
     * Play success tone
     * @return 0 for success, negative for error
     */
    int playSuccess();
    
    /**
     * Play error tone
     * @return 0 for success, negative for error
     */
    int playError();
    
    /**
     * Play warning tone
     * @return 0 for success, negative for error
     */
    int playWarning();
    
    /**
     * Check if buzzer is playing
     * @return true if playing, false otherwise
     */
    boolean isPlaying();
    
    /**
     * Close buzzer device
     * @return 0 for success, negative for error
     */
    int close();
    
    /**
     * Release buzzer resources
     */
    void release();
}