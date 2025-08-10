# Sales Flow Documentation

## Overview
Sales (Penjualan) module handles payment transactions where customers purchase goods/services using card or QRIS payment methods.

## Entry Points
- **Dashboard**: `DashboardModernActivity.java` → Purchase button
- **Direct Access**: `PurchaseActivityEnhanced.java` or `SalesModernActivity.java`

## Main Components

### 1. SalesModernActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/SalesModernActivity.java`

#### Key Features:
- Amount entry (manual or quick amounts)
- Payment method selection (Card/QRIS)
- Transaction processing
- Receipt generation

#### Key Methods:

##### Initialization
```java
onCreate() - Line 65
├── initViews() - Initialize UI components
├── setupListeners() - Set button click handlers
└── updateAmountDisplay() - Format amount display
```

##### Amount Entry
```java
Quick Amount Buttons:
├── btn10k → setAmount(10000)
├── btn20k → setAmount(20000)
├── btn50k → setAmount(50000)
├── btn100k → setAmount(100000)
├── btn200k → setAmount(200000)
└── btn500k → setAmount(500000)

Manual Entry:
└── TextWatcher on etAmount - Auto-format currency
```

##### Payment Processing
```java
processCardPayment() - Line 161
├── validateAmount() - Check minimum amount
├── showCardInsertDialog() - Prompt for card
├── launchCardReader() - Start card detection
└── processTransaction() - Send to host

processQrisPayment() - Line 216
├── generateQRCode() - Create QRIS code
├── showQRDialog() - Display QR for scanning
└── checkPaymentStatus() - Poll for completion
```

### 2. CardReaderActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/CardReaderActivity.java`

#### Card Reading Flow:
```java
startCardReading()
├── detectCardInsertion() - Wait for card
├── selectApplication() - Select AID
├── readCardData() - Extract card information
├── validateCard() - Check card validity
└── returnCardData() - Pass to caller
```

### 3. SalesPinActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/SalesPinActivity.java`

#### PIN Entry Features:
- 6-digit PIN input
- Visual masking with dots
- Custom number pad
- Error handling for wrong PIN
- Configurable title based on transaction type

### 4. SaleResultActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/SaleResultActivity.java`

#### Result Display:
- Transaction status (Success/Failed)
- Amount and change
- Card details (masked)
- Reference numbers
- Print receipt option

## Transaction Flow

### Card Payment Flow
```
1. Enter Amount
   └── Validate minimum amount (Rp 10,000)

2. Select Card Payment
   └── Initialize card reader

3. Insert/Tap Card
   ├── Detect card type (NSICCS/Visa/Mastercard)
   ├── Read card data (PAN, expiry, etc.)
   └── Validate card

4. Enter PIN
   ├── Display PIN pad
   ├── Capture 6-digit PIN
   └── Generate PIN block

5. Process Transaction
   ├── Build ISO 8583 message
   ├── Generate ARQC
   ├── Send to host
   └── Wait for response

6. Display Result
   ├── Show approval/decline
   ├── Print receipt
   └── Save transaction
```

### QRIS Payment Flow
```
1. Enter Amount
2. Select QRIS Payment
3. Generate QR Code
4. Display QR to Customer
5. Customer Scans with Mobile App
6. Poll for Payment Status
7. Display Success
8. Print Receipt
```

## Data Structure

### Transaction Request
```java
{
  "MTI": "0200",
  "ProcessingCode": "000000",  // Purchase
  "Amount": "50000",
  "PAN": "1234567890123456",
  "ExpiryDate": "2512",
  "EntryMode": "051",  // Chip
  "PINBlock": "[Encrypted]",
  "TerminalID": "12345678",
  "MerchantID": "123456789012345",
  "CurrencyCode": "360",  // IDR
  "ARQC": "[Cryptogram]"
}
```

### Transaction Response
```java
{
  "responseCode": "00",  // Approved
  "approvalCode": "123456",
  "referenceNumber": "202412201234",
  "message": "Transaction Approved"
}
```

## Database Schema

### Transaction Table
```sql
- id: INTEGER PRIMARY KEY
- type: "SALE"
- amount: BIGINT
- status: "SUCCESS/FAILED/PENDING"
- card_number: TEXT (masked)
- card_type: TEXT
- entry_mode: TEXT
- approval_code: TEXT
- reference_number: TEXT
- terminal_id: TEXT
- merchant_id: TEXT
- created_at: TIMESTAMP
```

## Security Features

### Card Security
- EMV chip authentication
- ARQC cryptogram generation
- PIN encryption (PIN block)
- PAN masking (6****4 format)
- Card validation checks

### Transaction Security
- End-to-end encryption
- Secure PIN entry
- Session timeout
- Transaction limits
- Duplicate detection

## Error Handling

### Common Errors
- **Insufficient Amount**: Minimum Rp 10,000
- **Card Read Error**: Retry or clean chip
- **Invalid PIN**: 3 attempts allowed
- **Network Timeout**: Retry or reversal
- **Host Decline**: Show reason code

### Error Recovery
1. Card read retry (up to 3 times)
2. Network retry with exponential backoff
3. Automatic reversal for timeout
4. Store-and-forward for offline mode

## Printing

### Receipt Components
```
UNIFLO MERCHANT
Jl. Sudirman No. 123
Jakarta

DATE/TIME: 20/12/2024 14:30:25
TERMINAL: 12345678
MERCHANT: 123456789012345

SALE
AMOUNT:        Rp 50,000
CARD:          ****3456
CARD TYPE:     NSICCS
ENTRY:         CHIP

APPROVAL CODE: 123456
REF NUMBER:    202412201234
BATCH:         000001
TRACE:         000123

** CUSTOMER COPY **
```

### Print Settings
- Font size configurable
- Double width for headers
- Logo support
- Paper feed control

## Configuration

### Terminal Configuration
- Terminal ID (8 digits)
- Merchant ID (15 digits)
- Merchant name and address
- Currency code
- Transaction limits

### Network Settings
- Primary host URL/IP
- Backup host URL/IP
- Port numbers
- Timeout values
- SSL/TLS settings

## UI Components

### Layouts
- `activity_sales_modern.xml` - Main sales screen
- `activity_card_reader.xml` - Card reading screen
- `activity_sales_pin.xml` - PIN entry screen
- `activity_sale_result.xml` - Result display
- `dialog_qris_payment.xml` - QRIS QR display

### UI Elements
- Amount display with currency formatting
- Quick amount buttons
- Payment method cards
- Progress dialogs
- Receipt preview

## Testing Scenarios

### Positive Cases
- Valid card with sufficient balance
- Correct PIN entry
- Successful host approval
- Receipt printing

### Negative Cases
- Declined transactions
- Invalid card
- Wrong PIN (lockout after 3)
- Network failure
- Printer out of paper

### Edge Cases
- Exact amount transactions
- Maximum amount limits
- Timeout during processing
- Card removed early
- Power failure recovery

## Dependencies
- Feitian SDK for card operations
- PrinterUtil for receipt printing
- EMVUtil for cryptogram generation
- Network libraries for host communication
- QR code library for QRIS

## Performance Considerations
- Card detection: < 3 seconds
- Transaction processing: < 10 seconds
- Receipt printing: < 5 seconds
- UI responsiveness: < 100ms

## Notes
- Supports both contact and contactless cards
- QRIS integration for Indonesian market
- Configurable quick amounts
- Transaction history available
- Batch upload for offline transactions
- Settlement required daily