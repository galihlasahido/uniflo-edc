# PIN Management Flow Documentation

## Overview
PIN Management module allows users to create, change, and verify PINs for their payment cards, ensuring secure transaction authentication.

## Entry Points
- **Dashboard**: PIN Management section (if enabled)
- **Direct Access**: 
  - `BasCreatePinActivity.java` - Create new PIN
  - `BasChangePinActivity.java` - Change existing PIN
  - `BasVerifyPinActivity.java` - Verify PIN

## Main Components

### 1. BasCreatePinActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/BasCreatePinActivity.java`

#### Key Features:
- New PIN creation for cards without PIN
- PIN strength validation
- Confirmation entry
- Secure storage
- Card validation

#### Key Methods:

##### PIN Creation Flow
```java
onCreate()
├── initializeViews() - Setup UI
├── setupCardReader() - Initialize SDK
├── validateCard() - Check card status
└── setupPinEntry() - Configure PIN pad

createNewPin()
├── validatePinStrength() - Check complexity
├── confirmPin() - Re-enter for confirmation
├── encryptPin() - Generate PIN block
├── sendToIssuer() - Submit to bank
└── saveLocally() - Store encrypted reference
```

### 2. BasChangePinActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/BasChangePinActivity.java`

#### PIN Change Process:
```java
changePin()
├── verifyCurrentPin() - Authenticate user
├── enterNewPin() - Input new PIN
├── confirmNewPin() - Re-enter confirmation
├── validateDifference() - Ensure PIN changed
├── updateIssuer() - Send to bank
└── updateLocal() - Update stored reference
```

### 3. BasVerifyPinActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/BasVerifyPinActivity.java`

#### Verification Features:
- Online PIN verification
- Offline PIN verification
- Attempt counter
- Card blocking logic

## PIN Entry Interface

### Custom PIN Pad Layout
```
┌─────────────────────────┐
│   Enter 6-Digit PIN     │
│                         │
│   ● ● ○ ○ ○ ○          │
│                         │
│   ┌───┐ ┌───┐ ┌───┐   │
│   │ 1 │ │ 2 │ │ 3 │   │
│   └───┘ └───┘ └───┘   │
│   ┌───┐ ┌───┐ ┌───┐   │
│   │ 4 │ │ 5 │ │ 6 │   │
│   └───┘ └───┘ └───┘   │
│   ┌───┐ ┌───┐ ┌───┐   │
│   │ 7 │ │ 8 │ │ 9 │   │
│   └───┘ └───┘ └───┘   │
│   ┌───┐ ┌───┐ ┌───┐   │
│   │CLR│ │ 0 │ │ OK │   │
│   └───┘ └───┘ └───┘   │
└─────────────────────────┘
```

## Security Implementation

### PIN Block Generation
```java
generatePinBlock(String pin, String pan) {
    // Format 0 (ISO-0) PIN Block
    String pinField = String.format("%02d%s", 
        pin.length(), pin);
    
    // Pad with F
    while (pinField.length() < 16) {
        pinField += "F";
    }
    
    // PAN field (rightmost 12 digits excluding check)
    String panField = "0000" + 
        pan.substring(pan.length() - 13, pan.length() - 1);
    
    // XOR PIN field with PAN field
    return xor(pinField, panField);
}
```

### PIN Encryption Methods
```java
Encryption Types:
├── DUKPT (Derived Unique Key Per Transaction)
├── Master/Session Key
├── Triple DES
└── AES-256
```

## PIN Validation Rules

### Strength Requirements
```java
validatePinStrength(String pin) {
    // Length check
    if (pin.length() < 4 || pin.length() > 6) {
        return "PIN must be 4-6 digits";
    }
    
    // Sequential check
    if (isSequential(pin)) {
        return "PIN cannot be sequential (1234, 4321)";
    }
    
    // Repeated digits
    if (isAllSame(pin)) {
        return "PIN cannot be all same digits (1111)";
    }
    
    // Birthday patterns
    if (isBirthdayPattern(pin)) {
        return "Avoid birthday patterns";
    }
    
    return null; // Valid
}
```

### Common Weak PINs
```java
WEAK_PINS = [
    "0000", "1111", "1234", "4321",
    "1212", "2580", "0852", "1478",
    "2468", "1357", "9999", "0123"
];
```

## Transaction Flow

### Create PIN Flow
```
1. Insert Card
   └── Validate card status
       └── Check if PIN exists

2. Enter New PIN
   ├── Display secure keypad
   ├── Show strength indicator
   └── Validate format

3. Confirm PIN
   ├── Re-enter PIN
   └── Match verification

4. Process Creation
   ├── Generate PIN block
   ├── Encrypt with keys
   ├── Send to issuer
   └── Wait for response

5. Complete
   ├── Store reference
   ├── Print advice slip
   └── Activate card
```

### Change PIN Flow
```
1. Card Validation
   └── Check card status

2. Verify Current PIN
   ├── Enter existing PIN
   ├── Online verification
   └── Check attempts

3. Enter New PIN
   ├── Different from old
   ├── Strength check
   └── Format validation

4. Confirm New PIN
   └── Match verification

5. Update PIN
   ├── Generate new block
   ├── Send to issuer
   ├── Update local
   └── Confirmation

6. Result
   └── Success/Failure message
```

## Data Structure

### PIN Change Request
```java
{
  "MTI": "0200",
  "ProcessingCode": "920000",  // PIN change
  "PAN": "6234567890123456",
  "OldPINBlock": "[Encrypted]",
  "NewPINBlock": "[Encrypted]",
  "TerminalID": "12345678",
  "Track2Data": "[Card Data]",
  "ARQC": "[Cryptogram]",
  "SessionKey": "[Encrypted Key]"
}
```

### PIN Change Response
```java
{
  "responseCode": "00",
  "message": "PIN changed successfully",
  "attemptsRemaining": 3,
  "expiryDate": "2025-12-31"
}
```

## Error Handling

### PIN Errors
```java
ERROR_CODES = {
    "55": "Incorrect PIN",
    "75": "PIN tries exceeded",
    "86": "PIN not initialized",
    "87": "PIN change not allowed",
    "88": "Cryptographic failure"
};
```

### Retry Logic
```java
MAX_ATTEMPTS = 3;
BLOCK_DURATION = 24_HOURS;

handleWrongPin(attempts) {
    if (attempts >= MAX_ATTEMPTS) {
        blockCard();
        return "Card blocked. Contact bank.";
    } else {
        remaining = MAX_ATTEMPTS - attempts;
        return "Wrong PIN. " + remaining + " attempts left.";
    }
}
```

## Database Schema

### PIN Management Table
```sql
CREATE TABLE pin_management (
    id INTEGER PRIMARY KEY,
    card_hash TEXT UNIQUE,
    pin_offset TEXT,
    wrong_attempts INTEGER DEFAULT 0,
    last_attempt TIMESTAMP,
    blocked_until TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### PIN Transaction Log
```sql
CREATE TABLE pin_transactions (
    id INTEGER PRIMARY KEY,
    card_hash TEXT,
    action TEXT, -- CREATE/CHANGE/VERIFY/BLOCK
    result TEXT, -- SUCCESS/FAILED
    error_code TEXT,
    timestamp TIMESTAMP,
    terminal_id TEXT
);
```

## Offline PIN Verification

### Local PIN Storage
```java
// Never store actual PIN
// Store verification values only
StoredPINData {
    cardHash: SHA256(PAN),
    pinOffset: encryptedOffset,
    checkValue: HMAC(PIN + salt),
    attempts: 0,
    lastAttempt: timestamp
}
```

### Offline Verification
```java
verifyOffline(enteredPin, storedData) {
    // Generate check value
    checkValue = HMAC(enteredPin + salt);
    
    // Compare with stored
    if (checkValue == storedData.checkValue) {
        resetAttempts();
        return true;
    } else {
        incrementAttempts();
        return false;
    }
}
```

## UI Components

### Layouts
- `activity_create_pin.xml` - New PIN creation
- `activity_change_pin.xml` - PIN change screen
- `activity_verify_pin.xml` - PIN verification
- `dialog_pin_entry.xml` - Secure PIN pad
- `layout_pin_strength.xml` - Strength indicator

### Custom Views
```java
PinEntryView - Masked PIN input
PinPadView - Secure number pad
PinStrengthIndicator - Visual strength
PinDotsView - Visual PIN length
```

## Security Best Practices

### Implementation Guidelines
1. **Never log PINs** - Even for debugging
2. **Clear memory** - Overwrite PIN variables
3. **Timeout sessions** - 30 second PIN entry limit
4. **Secure display** - Prevent screenshots
5. **Tamper detection** - Check for modifications

### Code Example
```java
// Secure PIN handling
char[] pin = pinInput.getPassword();
try {
    processPIN(pin);
} finally {
    // Clear sensitive data
    Arrays.fill(pin, '\0');
    System.gc();
}
```

## Compliance Requirements

### PCI-DSS Standards
- PIN must be encrypted at rest
- Transmission must be encrypted
- No PIN storage in logs
- Secure key management
- Regular security audits

### Regional Regulations
- Minimum PIN length requirements
- Maximum retry attempts
- Cooling period after blocks
- PIN expiry policies
- Notification requirements

## Testing Scenarios

### Positive Tests
1. Create PIN successfully
2. Change PIN with correct old PIN
3. Verify PIN correctly
4. Reset PIN after block period

### Negative Tests
1. Wrong PIN entry (1st, 2nd, 3rd)
2. Card blocked after 3 attempts
3. Weak PIN rejection
4. Network failure during change
5. Timeout during entry

### Security Tests
1. PIN not visible in logs
2. Memory cleared after use
3. Encryption verified
4. Replay attack prevention
5. Man-in-the-middle protection

## Performance Metrics
- PIN entry timeout: 30 seconds
- Verification time: < 3 seconds
- Change processing: < 5 seconds
- Block release: Immediate after period

## Integration Points

### External Systems
- Issuer PIN management system
- Hardware Security Module (HSM)
- Key Management System (KMS)
- Fraud detection system
- Audit logging system

## Error Recovery

### Failed PIN Change
```java
rollbackPinChange() {
    // Revert to old PIN
    // Log failed attempt
    // Notify issuer
    // Clear new PIN data
    // Reset UI state
}
```

## Future Enhancements
- Biometric PIN replacement
- Dynamic PIN generation
- One-time PIN support
- PIN-less transactions
- Voice-guided PIN entry
- Contextual PIN strength

## Notes
- PIN is never transmitted in clear text
- All PIN operations require card presence
- Offline PIN limited to 3 attempts
- Online verification preferred
- Card must be EMV compliant
- Regular PIN change recommended