package id.uniflo.uniedc.util;

/**
 * Utility class for byte array operations
 */
public class BytesUtils {
    
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    
    /**
     * Convert hex string to byte array
     * @param hexString Hex string (e.g., "0123456789ABCDEF")
     * @return Byte array
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.length() == 0) {
            return new byte[0];
        }
        
        // Remove spaces and convert to uppercase
        hexString = hexString.replaceAll("\\s", "").toUpperCase();
        
        // Check if length is even
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                + Character.digit(hexString.charAt(i + 1), 16));
        }
        
        return data;
    }
    
    /**
     * Convert byte array to hex string
     * @param bytes Byte array
     * @return Hex string in uppercase
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        
        return new String(hexChars);
    }
    
    /**
     * Convert byte array to hex string with spaces
     * @param bytes Byte array
     * @return Hex string with spaces between bytes
     */
    public static String bytesToHexStringWithSpaces(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        
        return sb.toString();
    }
    
    /**
     * Compare two byte arrays
     * @param array1 First array
     * @param array2 Second array
     * @return true if arrays are equal
     */
    public static boolean compareBytes(byte[] array1, byte[] array2) {
        if (array1 == null || array2 == null) {
            return array1 == array2;
        }
        
        if (array1.length != array2.length) {
            return false;
        }
        
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Concatenate multiple byte arrays
     * @param arrays Arrays to concatenate
     * @return Combined byte array
     */
    public static byte[] concatenate(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        
        byte[] result = new byte[totalLength];
        int offset = 0;
        
        for (byte[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        
        return result;
    }
    
    /**
     * Extract substring of bytes
     * @param source Source array
     * @param offset Start offset
     * @param length Length to extract
     * @return Extracted bytes
     */
    public static byte[] subBytes(byte[] source, int offset, int length) {
        if (source == null || offset < 0 || length < 0 || offset + length > source.length) {
            return new byte[0];
        }
        
        byte[] result = new byte[length];
        System.arraycopy(source, offset, result, 0, length);
        return result;
    }
    
    /**
     * Convert int to byte array (big endian)
     * @param value Integer value
     * @return 4-byte array
     */
    public static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
    }
    
    /**
     * Convert byte array to int (big endian)
     * @param bytes Byte array (up to 4 bytes)
     * @return Integer value
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }
        
        int result = 0;
        for (int i = 0; i < Math.min(bytes.length, 4); i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }
        
        return result;
    }
    
    /**
     * XOR two byte arrays
     * @param array1 First array
     * @param array2 Second array
     * @return XOR result (length of shorter array)
     */
    public static byte[] xor(byte[] array1, byte[] array2) {
        if (array1 == null || array2 == null) {
            return new byte[0];
        }
        
        int length = Math.min(array1.length, array2.length);
        byte[] result = new byte[length];
        
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (array1[i] ^ array2[i]);
        }
        
        return result;
    }
}