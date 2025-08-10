# Balance Inquiry Flow Documentation

## Overview
Balance Inquiry (Cek Saldo) allows users to check their card balance by inserting/tapping their card and entering PIN.

## Entry Points
- **Dashboard**: `DashboardModernActivity.java` → Balance Inquiry button
- **Home Screen**: `BasHomeActivity.java` → Balance Inquiry menu item

## Main Components

### 1. BalanceInquiryActivityBasic.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/BalanceInquiryActivityBasic.java`

#### Key Methods:

##### Initialization
```java
onCreate() - Line 73
├── initViews() - Initialize UI components
├── setupToolbar() - Configure action bar
├── setupListeners() - Set button click handlers
└── initializeSdk() - Initialize card reader service
```

##### Card Detection Flow
```java
checkBalance() - Line 138
├── showCardInsertDialog() - Line 152: Display card insertion prompt
├── detectCard() - Line 236: Start card detection
├── performPowerCycleDetection() - Line 249: Power cycle approach
└── detectCardWithTimeout() - Line 290: Card detection with timeout
    └── onCardATR() - Line 296: Card detected callback
```

##### Card Processing
```java
processCardWithTimeout() - Line 555
└── processCard() - Line 634
    ├── Try NSICCS AIDs (A0000006021010, A0000006022020)
    ├── Try Mastercard AID (A0000000041010)
    └── Try Visa AID (A0000000031010)
```

##### PIN Entry
```java
showPinInputDialog() - Line 764
├── Display custom PIN pad
├── Handle 6-digit PIN input
└── Visual feedback with dots
```

##### Transaction Processing
```java
collectTLVDataAndProcessWithPin() - Line 924
├── generatePinBlock() - Line 1281: Create encrypted PIN block
├── readCardPAN() - Line 1346: Read card PAN from chip
├── Build TLV data structure
├── Generate ARQC using EMVUtil
└── sendToHost() - Line 1064: Send to backend
```

### 2. BalanceInquiryResultActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/BalanceInquiryResultActivity.java`

#### Display Components:
- Merchant information
- Terminal/Merchant ID
- Card details (masked)
- Balance amount
- Transaction reference
- Response/Approval codes

#### Actions:
- Print receipt
- Reprint functionality
- Close and return

## Data Flow

### Request Structure (TLV)
```
MTI: 0200
ProcessingCode: 310000 (Balance Inquiry)
Amount: 000000000000
PINBlock: [Encrypted PIN]
STAN: [Trace Number]
CardType: NSICCS/Mastercard/Visa
ARQC: [Cryptogram]
```

### Response Structure
```json
{
  "responseCode": "00",
  "balance": "25750000",
  "message": "Balance inquiry complete",
  "approvalCode": "123456"
}
```

## Database Operations

### Transaction Record
- Type: `TYPE_BALANCE_INQUIRY`
- Status: `STATUS_SUCCESS/STATUS_FAILED`
- Amount: 0 (no amount for inquiry)
- Card details (masked)
- EMV data (ARQC, ATC, TVR, TSI)
- Reference numbers

## Error Handling

### Card Detection Errors
- **Power Error (32778)**: Retry with power cycling
- **Timeout**: 30-70 second progressive timeout
- **No Card**: User prompt to insert card
- **Communication Error**: Retry with card reset

### Recovery Strategies
1. Simple retry with delay
2. Card reader reset
3. Service reconnection
4. Manual card detection mode
5. Card cleaning instructions

## Security Features
- PIN encryption using PIN block
- PAN masking (show first 6, last 4 digits)
- EMV cryptogram generation (ARQC)
- Secure storage of sensitive data
- Session timeout handling

## UI/UX Elements

### Layouts
- `activity_balance_inquiry.xml` - Main screen
- `activity_balance_inquiry_modern.xml` - Modern UI version
- `dialog_insert_card.xml` - Card insertion dialog
- `dialog_pin_input.xml` - PIN entry dialog
- `activity_balance_inquiry_result.xml` - Result display

### User Flow
1. Tap "Cek Saldo" button
2. System initializes card reader
3. PIN entry screen appears (card section removed)
4. Enter 6-digit PIN
5. Processing animation
6. Display balance result
7. Option to print receipt

## Configuration

### Terminal Settings Required
- Terminal ID
- Merchant ID
- Acquiring Institution Code
- Network settings (host URL, port)
- Timeout values

### AID Configuration
- NSICCS AIDs enabled by default
- International card AIDs as fallback
- Configurable in AID Settings

## Testing Scenarios

### Success Cases
- Valid card with correct PIN
- All supported card types
- Online and offline modes

### Error Cases
- Invalid PIN
- Card read failure
- Network timeout
- Host rejection
- Insufficient card power

## Dependencies
- Feitian SDK (FTSDK_api)
- EMVUtil for cryptogram generation
- Network communication libraries
- Database (TransactionDAO)
- Settings manager

## Related Files
- `EMVUtil.java` - EMV processing utilities
- `SettingsManager.java` - Configuration management
- `TransactionDAO.java` - Database operations
- `SecureSettingsDAO.java` - Secure settings storage

## Notes
- Card insertion UI has been removed per request
- Direct PIN entry on screen load
- Supports both chip and contactless cards
- Demo mode available when host unreachable
- Comprehensive debug logging for troubleshooting