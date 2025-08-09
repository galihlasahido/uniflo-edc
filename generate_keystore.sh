#!/bin/bash

# Script to generate Android keystore for Feitian SDK Demo app signing
# This creates a keystore with public/private key pair for APK signing

# Configuration
KEYSTORE_DIR="keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/feitian-release.jks"
KEY_ALIAS="feitian-demo"
VALIDITY_DAYS=10000  # ~27 years

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Android Keystore Generator for Feitian SDK Demo ===${NC}"
echo ""
echo "This script will create a production keystore for signing your Android app."
echo "The keystore contains a public/private key pair for secure app signing."
echo ""

# Create keystore directory if it doesn't exist
if [ ! -d "$KEYSTORE_DIR" ]; then
    mkdir -p "$KEYSTORE_DIR"
    echo -e "${GREEN}Created directory: $KEYSTORE_DIR${NC}"
fi

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo -e "${YELLOW}Warning: Keystore already exists at $KEYSTORE_FILE${NC}"
    read -p "Do you want to overwrite it? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${RED}Aborted.${NC}"
        exit 1
    fi
fi

echo ""
echo "Please provide the following information for your keystore:"
echo ""

# Run keytool command with verbose output
keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY_DAYS

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}=== Keystore created successfully! ===${NC}"
    echo ""
    echo "IMPORTANT: Please save the following information securely:"
    echo "1. Keystore file location: $KEYSTORE_FILE"
    echo "2. Key alias: $KEY_ALIAS"
    echo "3. Your keystore password (you entered during creation)"
    echo "4. Your key password (you entered during creation)"
    echo ""
    
    # Display certificate fingerprints
    echo -e "${GREEN}Certificate fingerprints (you may need these for API configurations):${NC}"
    echo "Enter your keystore password to view fingerprints:"
    keytool -list -v -keystore "$KEYSTORE_FILE" -alias "$KEY_ALIAS" | grep -E "SHA1:|SHA256:"
    
    echo ""
    echo -e "${YELLOW}To use this keystore in your app/build.gradle:${NC}"
    echo ""
    echo "signingConfigs {"
    echo "    release {"
    echo "        storeFile file('../$KEYSTORE_FILE')"
    echo "        storePassword 'YOUR_KEYSTORE_PASSWORD'"
    echo "        keyAlias '$KEY_ALIAS'"
    echo "        keyPassword 'YOUR_KEY_PASSWORD'"
    echo "    }"
    echo "}"
    
    echo ""
    echo -e "${RED}⚠️  SECURITY NOTES:${NC}"
    echo "1. NEVER commit the .jks file to version control!"
    echo "2. Keep your passwords secure and backed up"
    echo "3. You CANNOT update your app without this keystore"
    echo "4. Consider using gradle.properties or environment variables for passwords"
    echo ""
    echo "Recommended: Add '$KEYSTORE_FILE' to your .gitignore file"
    
else
    echo ""
    echo -e "${RED}✗ Failed to generate keystore${NC}"
    exit 1
fi