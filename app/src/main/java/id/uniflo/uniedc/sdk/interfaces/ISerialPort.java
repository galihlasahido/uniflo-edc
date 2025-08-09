package id.uniflo.uniedc.sdk.interfaces;

/**
 * Serial Port interface for POS SDK abstraction
 */
public interface ISerialPort {
    
    // Common baud rates
    int BAUD_1200 = 1200;
    int BAUD_2400 = 2400;
    int BAUD_4800 = 4800;
    int BAUD_9600 = 9600;
    int BAUD_19200 = 19200;
    int BAUD_38400 = 38400;
    int BAUD_57600 = 57600;
    int BAUD_115200 = 115200;
    
    // Data bits
    int DATA_5 = 5;
    int DATA_6 = 6;
    int DATA_7 = 7;
    int DATA_8 = 8;
    
    // Stop bits
    int STOP_1 = 1;
    int STOP_1_5 = 3;  // 1.5 stop bits
    int STOP_2 = 2;
    
    // Parity
    int PARITY_NONE = 0;
    int PARITY_ODD = 1;
    int PARITY_EVEN = 2;
    int PARITY_MARK = 3;
    int PARITY_SPACE = 4;
    
    // Flow control
    int FLOW_NONE = 0;
    int FLOW_HARDWARE = 1;  // RTS/CTS
    int FLOW_SOFTWARE = 2;  // XON/XOFF
    
    /**
     * Initialize serial port module
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Open serial port
     * @param port Port number (0-based)
     * @param baudRate Baud rate
     * @return 0 for success, negative for error
     */
    int open(int port, int baudRate);
    
    /**
     * Configure serial port parameters
     * @param port Port number
     * @param baudRate Baud rate
     * @param dataBits Data bits (5-8)
     * @param stopBits Stop bits
     * @param parity Parity type
     * @return 0 for success, negative for error
     */
    int configure(int port, int baudRate, int dataBits, int stopBits, int parity);
    
    /**
     * Set flow control
     * @param port Port number
     * @param flowControl Flow control type
     * @return 0 for success, negative for error
     */
    int setFlowControl(int port, int flowControl);
    
    /**
     * Write data to serial port
     * @param port Port number
     * @param data Data to write
     * @return Number of bytes written or negative for error
     */
    int write(int port, byte[] data);
    
    /**
     * Write data with timeout
     * @param port Port number
     * @param data Data to write
     * @param timeout Timeout in milliseconds
     * @return Number of bytes written or negative for error
     */
    int write(int port, byte[] data, int timeout);
    
    /**
     * Read data from serial port
     * @param port Port number
     * @param maxLength Maximum bytes to read
     * @return Data read or null if no data/error
     */
    byte[] read(int port, int maxLength);
    
    /**
     * Read data with timeout
     * @param port Port number
     * @param maxLength Maximum bytes to read
     * @param timeout Timeout in milliseconds
     * @return Data read or null if timeout/error
     */
    byte[] read(int port, int maxLength, int timeout);
    
    /**
     * Get available bytes to read
     * @param port Port number
     * @return Number of bytes available or negative for error
     */
    int available(int port);
    
    /**
     * Flush input buffer
     * @param port Port number
     * @return 0 for success, negative for error
     */
    int flushInput(int port);
    
    /**
     * Flush output buffer
     * @param port Port number
     * @return 0 for success, negative for error
     */
    int flushOutput(int port);
    
    /**
     * Check if port is open
     * @param port Port number
     * @return true if open, false otherwise
     */
    boolean isOpen(int port);
    
    /**
     * Get port status
     * @param port Port number
     * @return Status flags or negative for error
     */
    int getStatus(int port);
    
    /**
     * Set DTR (Data Terminal Ready) line
     * @param port Port number
     * @param state true to set high, false to set low
     * @return 0 for success, negative for error
     */
    int setDTR(int port, boolean state);
    
    /**
     * Set RTS (Request To Send) line
     * @param port Port number
     * @param state true to set high, false to set low
     * @return 0 for success, negative for error
     */
    int setRTS(int port, boolean state);
    
    /**
     * Close serial port
     * @param port Port number
     * @return 0 for success, negative for error
     */
    int close(int port);
    
    /**
     * Release serial port resources
     */
    void release();
}