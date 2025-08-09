package id.uniflo.uniedc.sdk.interfaces;

/**
 * Cryptography interface for POS SDK abstraction
 */
public interface ICrypto {
    
    // Encryption algorithms
    int ALG_DES = 0;
    int ALG_3DES = 1;
    int ALG_AES = 2;
    int ALG_RSA = 3;
    int ALG_SM4 = 4;
    
    // Encryption modes
    int MODE_ECB = 0;
    int MODE_CBC = 1;
    int MODE_CFB = 2;
    int MODE_OFB = 3;
    
    // Padding types
    int PAD_NONE = 0;
    int PAD_PKCS5 = 1;
    int PAD_PKCS7 = 2;
    int PAD_ISO9797_1 = 3;
    int PAD_ISO9797_2 = 4;
    
    // Hash algorithms
    int HASH_SHA1 = 0;
    int HASH_SHA256 = 1;
    int HASH_SHA384 = 2;
    int HASH_SHA512 = 3;
    int HASH_MD5 = 4;
    int HASH_SM3 = 5;
    
    // MAC algorithms
    int MAC_X919 = 0;
    int MAC_ECB = 1;
    int MAC_CBC = 2;
    int MAC_X9_19 = 3;
    int MAC_CMAC = 4;
    
    /**
     * Initialize crypto module
     * @return 0 for success, negative for error
     */
    int init();
    
    /**
     * Encrypt data
     * @param algorithm Encryption algorithm
     * @param mode Encryption mode
     * @param keyIndex Key index in secure storage
     * @param iv Initialization vector (null for ECB mode)
     * @param data Data to encrypt
     * @return Encrypted data or null if failed
     */
    byte[] encrypt(int algorithm, int mode, int keyIndex, byte[] iv, byte[] data);
    
    /**
     * Decrypt data
     * @param algorithm Encryption algorithm
     * @param mode Encryption mode
     * @param keyIndex Key index in secure storage
     * @param iv Initialization vector (null for ECB mode)
     * @param data Data to decrypt
     * @return Decrypted data or null if failed
     */
    byte[] decrypt(int algorithm, int mode, int keyIndex, byte[] iv, byte[] data);
    
    /**
     * Calculate hash
     * @param algorithm Hash algorithm
     * @param data Data to hash
     * @return Hash value or null if failed
     */
    byte[] hash(int algorithm, byte[] data);
    
    /**
     * Calculate MAC
     * @param algorithm MAC algorithm
     * @param keyIndex Key index in secure storage
     * @param data Data to calculate MAC
     * @return MAC value or null if failed
     */
    byte[] calculateMac(int algorithm, int keyIndex, byte[] data);
    
    /**
     * Verify MAC
     * @param algorithm MAC algorithm
     * @param keyIndex Key index in secure storage
     * @param data Data to verify
     * @param mac MAC value to verify against
     * @return true if MAC is valid, false otherwise
     */
    boolean verifyMac(int algorithm, int keyIndex, byte[] data, byte[] mac);
    
    /**
     * Generate random data
     * @param length Length of random data
     * @return Random data or null if failed
     */
    byte[] getRandom(int length);
    
    /**
     * RSA encrypt
     * @param keyIndex RSA key index
     * @param data Data to encrypt
     * @return Encrypted data or null if failed
     */
    byte[] rsaEncrypt(int keyIndex, byte[] data);
    
    /**
     * RSA decrypt
     * @param keyIndex RSA key index
     * @param data Data to decrypt
     * @return Decrypted data or null if failed
     */
    byte[] rsaDecrypt(int keyIndex, byte[] data);
    
    /**
     * RSA sign
     * @param keyIndex RSA key index
     * @param hashAlgorithm Hash algorithm to use
     * @param data Data to sign
     * @return Signature or null if failed
     */
    byte[] rsaSign(int keyIndex, int hashAlgorithm, byte[] data);
    
    /**
     * RSA verify signature
     * @param keyIndex RSA key index
     * @param hashAlgorithm Hash algorithm used
     * @param data Original data
     * @param signature Signature to verify
     * @return true if signature is valid, false otherwise
     */
    boolean rsaVerify(int keyIndex, int hashAlgorithm, byte[] data, byte[] signature);
    
    /**
     * Release crypto resources
     */
    void release();
}