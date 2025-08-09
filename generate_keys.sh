#!/bin/bash

# Script to generate RSA private and public key pair as separate files
# Uses OpenSSL to create keys suitable for various cryptographic operations

# Configuration
KEYS_DIR="keys"
PRIVATE_KEY_FILE="$KEYS_DIR/private_key.pem"
PUBLIC_KEY_FILE="$KEYS_DIR/public_key.pem"
KEY_SIZE=2048

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== RSA Key Pair Generator ===${NC}"
echo ""
echo "This script generates separate RSA private and public key files."
echo "These keys can be used for encryption, decryption, and digital signatures."
echo ""

# Check if OpenSSL is installed
if ! command -v openssl &> /dev/null; then
    echo -e "${RED}Error: OpenSSL is not installed!${NC}"
    echo "Please install OpenSSL to continue."
    exit 1
fi

# Create keys directory if it doesn't exist
if [ ! -d "$KEYS_DIR" ]; then
    mkdir -p "$KEYS_DIR"
    echo -e "${GREEN}Created directory: $KEYS_DIR${NC}"
fi

# Check if keys already exist
if [ -f "$PRIVATE_KEY_FILE" ] || [ -f "$PUBLIC_KEY_FILE" ]; then
    echo -e "${YELLOW}Warning: Key files already exist in $KEYS_DIR${NC}"
    echo "Files found:"
    [ -f "$PRIVATE_KEY_FILE" ] && echo "  - $PRIVATE_KEY_FILE"
    [ -f "$PUBLIC_KEY_FILE" ] && echo "  - $PUBLIC_KEY_FILE"
    echo ""
    read -p "Do you want to overwrite them? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${RED}Aborted.${NC}"
        exit 1
    fi
fi

echo ""
echo -e "${BLUE}Generating RSA key pair...${NC}"
echo "Key size: $KEY_SIZE bits"
echo ""

# Generate private key
echo -e "${GREEN}Step 1: Generating private key...${NC}"
openssl genrsa -out "$PRIVATE_KEY_FILE" $KEY_SIZE 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Private key generated successfully${NC}"
    # Set appropriate permissions for private key
    chmod 600 "$PRIVATE_KEY_FILE"
    echo "  File: $PRIVATE_KEY_FILE"
    echo "  Permissions: 600 (read/write for owner only)"
else
    echo -e "${RED}✗ Failed to generate private key${NC}"
    exit 1
fi

echo ""

# Extract public key from private key
echo -e "${GREEN}Step 2: Extracting public key from private key...${NC}"
openssl rsa -in "$PRIVATE_KEY_FILE" -pubout -out "$PUBLIC_KEY_FILE" 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Public key extracted successfully${NC}"
    # Set appropriate permissions for public key
    chmod 644 "$PUBLIC_KEY_FILE"
    echo "  File: $PUBLIC_KEY_FILE"
    echo "  Permissions: 644 (readable by all, writable by owner)"
else
    echo -e "${RED}✗ Failed to extract public key${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}=== Key generation completed successfully! ===${NC}"
echo ""

# Display key information
echo -e "${BLUE}Key Information:${NC}"
echo ""

echo "Private Key Details:"
openssl rsa -in "$PRIVATE_KEY_FILE" -text -noout 2>/dev/null | head -n 10
echo "..."
echo ""

echo "Public Key Details:"
openssl rsa -pubin -in "$PUBLIC_KEY_FILE" -text -noout 2>/dev/null | head -n 8
echo ""

# Generate fingerprints
echo -e "${BLUE}Key Fingerprints:${NC}"
echo ""

# MD5 fingerprint
echo -n "Private Key MD5:  "
openssl rsa -in "$PRIVATE_KEY_FILE" -pubout -outform DER 2>/dev/null | openssl md5 | cut -d' ' -f2

echo -n "Public Key MD5:   "
openssl rsa -pubin -in "$PUBLIC_KEY_FILE" -outform DER 2>/dev/null | openssl md5 | cut -d' ' -f2

# SHA256 fingerprint
echo -n "Private Key SHA256: "
openssl rsa -in "$PRIVATE_KEY_FILE" -pubout -outform DER 2>/dev/null | openssl dgst -sha256 | cut -d' ' -f2

echo -n "Public Key SHA256:  "
openssl rsa -pubin -in "$PUBLIC_KEY_FILE" -outform DER 2>/dev/null | openssl dgst -sha256 | cut -d' ' -f2

echo ""
echo -e "${YELLOW}Usage Examples:${NC}"
echo ""
echo "1. Encrypt data with public key:"
echo "   openssl rsautl -encrypt -inkey $PUBLIC_KEY_FILE -pubin -in plaintext.txt -out encrypted.bin"
echo ""
echo "2. Decrypt data with private key:"
echo "   openssl rsautl -decrypt -inkey $PRIVATE_KEY_FILE -in encrypted.bin -out decrypted.txt"
echo ""
echo "3. Sign data with private key:"
echo "   openssl dgst -sha256 -sign $PRIVATE_KEY_FILE -out signature.bin data.txt"
echo ""
echo "4. Verify signature with public key:"
echo "   openssl dgst -sha256 -verify $PUBLIC_KEY_FILE -signature signature.bin data.txt"
echo ""

echo -e "${RED}⚠️  SECURITY NOTES:${NC}"
echo "1. Keep your private key SECURE - never share it!"
echo "2. The public key can be shared freely"
echo "3. Back up your private key - losing it means losing access to encrypted data"
echo "4. Consider encrypting the private key with a passphrase for extra security"
echo ""

# Option to add passphrase to private key
echo -e "${YELLOW}Optional: Add passphrase protection to private key${NC}"
read -p "Do you want to add a passphrase to your private key? (y/N): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "Enter a strong passphrase for your private key:"
    openssl rsa -aes256 -in "$PRIVATE_KEY_FILE" -out "${PRIVATE_KEY_FILE}.encrypted"
    
    if [ $? -eq 0 ]; then
        echo ""
        echo -e "${GREEN}✓ Encrypted private key created: ${PRIVATE_KEY_FILE}.encrypted${NC}"
        echo "Use this file when passphrase protection is needed."
        chmod 600 "${PRIVATE_KEY_FILE}.encrypted"
    else
        echo -e "${RED}✗ Failed to create encrypted private key${NC}"
    fi
fi

echo ""
echo -e "${GREEN}Done! Your RSA key pair is ready in the '$KEYS_DIR' directory.${NC}"