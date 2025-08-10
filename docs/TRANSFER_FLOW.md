# Transfer Flow Documentation

## Overview
Transfer module enables fund transfers between accounts, supporting both intra-bank and inter-bank transactions through the national payment network.

## Entry Points
- **Dashboard**: `DashboardModernActivity.java` → Transfer button
- **Direct Access**: `TransferActivityEnhanced.java` or `TransferModernActivity.java`

## Main Components

### 1. TransferActivityEnhanced.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/TransferActivityEnhanced.java`

#### Key Features:
- Account number validation
- Bank selection
- Amount entry
- Beneficiary name verification
- Transfer confirmation
- Receipt generation

#### Key Methods:

##### Initialization
```java
onCreate()
├── initializeViews() - Setup UI components
├── loadBankList() - Load supported banks
├── setupValidators() - Input validation
└── initializeSDK() - Card reader setup
```

##### Transfer Types
```java
Transfer Options:
├── Intra-bank Transfer (Same bank)
├── Inter-bank Transfer (Different bank)
├── Virtual Account Transfer
└── Scheduled Transfer (Future)
```

### 2. TransferModernActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/TransferModernActivity.java`

#### Enhanced Features:
- Recent beneficiaries
- Saved beneficiaries
- Transfer templates
- Multi-step validation
- Real-time fee calculation

## Transaction Flow

### Complete Transfer Process
```
1. Select Transfer Type
   ├── Same Bank
   ├── Other Bank
   └── Virtual Account

2. Enter Beneficiary Details
   ├── Account Number
   ├── Bank Selection (if inter-bank)
   └── Validate Account

3. Verify Beneficiary
   ├── Display Account Name
   ├── Confirm Details
   └── Add to Favorites (optional)

4. Enter Amount
   ├── Input Transfer Amount
   ├── Check Balance
   ├── Calculate Fees
   └── Show Total

5. Insert Card & PIN
   ├── Read Card Data
   ├── Enter 6-digit PIN
   └── Generate Security Codes

6. Confirm Transaction
   ├── Review Details
   ├── Accept Terms
   └── Process Transfer

7. Complete Transaction
   ├── Show Result
   ├── Print Receipt
   └── Save Transaction
```

## Data Structure

### Transfer Request
```java
{
  "MTI": "0200",
  "ProcessingCode": "400000",  // Transfer
  "Amount": "1000000",
  "SourceAccount": "1234567890",
  "DestinationAccount": "0987654321",
  "DestinationBank": "014",  // Bank code
  "BeneficiaryName": "John Doe",
  "TransferType": "ONLINE",
  "PAN": "6234567890123456",
  "PINBlock": "[Encrypted]",
  "TerminalID": "12345678",
  "MerchantID": "123456789012345",
  "ReferenceNumber": "TRF202412201234",
  "ARQC": "[Cryptogram]"
}
```

### Transfer Response
```java
{
  "responseCode": "00",
  "approvalCode": "123456",
  "referenceNumber": "TRF202412201234",
  "destinationReference": "DEST123456",
  "balance": "5000000",
  "fee": "6500",
  "message": "Transfer Successful"
}
```

## Bank Codes

### Major Banks
```java
BCA: "014"
Mandiri: "008"
BNI: "009"
BRI: "002"
CIMB: "022"
Danamon: "011"
Permata: "013"
BTPN: "213"
// ... more banks
```

## Fee Structure

### Transfer Fees
```java
calculateTransferFee(amount, type) {
    switch(type) {
        case SAME_BANK:
            return 0;  // Free
        
        case OTHER_BANK_ONLINE:
            return 6500;  // Flat fee
        
        case OTHER_BANK_RTGS:
            if (amount < 100000000) {
                return 25000;
            } else {
                return 30000;
            }
        
        case VIRTUAL_ACCOUNT:
            return 4000;
    }
}
```

### Fee Display
- Show before confirmation
- Include in transaction details
- Separate line item on receipt
- Update based on amount/type

## Validation Rules

### Account Number Validation
```java
validateAccountNumber(accountNo, bankCode) {
    // Length validation
    if (bankCode == "014") {  // BCA
        return accountNo.length() == 10;
    } else if (bankCode == "008") {  // Mandiri
        return accountNo.length() == 13;
    }
    // ... other banks
    
    // Checksum validation
    return validateChecksum(accountNo);
}
```

### Amount Validation
```java
Minimum Transfer: Rp 10,000
Maximum Online: Rp 50,000,000
Maximum RTGS: Rp 500,000,000
Daily Limit: Rp 100,000,000
```

### Beneficiary Verification
- Real-time name lookup
- Confirmation required
- Mismatch warning
- Fraud detection

## Security Features

### Multi-Factor Security
1. **Card Authentication**
   - EMV verification
   - Card validity check
   - Issuer authorization

2. **PIN Verification**
   - Encrypted transmission
   - Limited attempts
   - Temporary blocking

3. **Transaction Security**
   - Unique reference numbers
   - Digital signatures
   - Audit trails
   - Fraud monitoring

4. **Network Security**
   - SSL/TLS encryption
   - Certificate pinning
   - Message authentication

## Receipt Format

### Customer Receipt
```
=================================
      UNIFLO MERCHANT
   Jl. Sudirman No. 123
        Jakarta
=================================
DATE: 20/12/2024    TIME: 12:34
TERMINAL ID: 12345678
---------------------------------
       TRANSFER
---------------------------------
FROM:
Card:         ****3456
Name:         JOHN SMITH

TO:
Bank:         BCA
Account:      1234567890
Name:         JANE DOE

AMOUNT:       Rp  1,000,000
FEE:          Rp      6,500
-------------
TOTAL:        Rp  1,006,500

BALANCE:      Rp  4,993,500

REF NO:       TRF202412201234
DEST REF:     DEST123456
APPROVAL:     123456

Status: SUCCESS

    ** CUSTOMER COPY **
=================================
```

## Beneficiary Management

### Save Beneficiary
```java
Beneficiary {
    id: "BEN001",
    nickname: "Wife",
    accountNumber: "1234567890",
    accountName: "Jane Doe",
    bankCode: "014",
    bankName: "BCA",
    lastUsed: "2024-12-20",
    frequency: 15
}
```

### Recent Transfers
- Last 10 transfers
- Quick repeat option
- Auto-fill details
- Update amounts

## Error Handling

### Common Errors

#### Invalid Account
```java
"Account number not found"
"Account inactive"
"Account blocked"
```

#### Insufficient Balance
```java
if (balance < amount + fee) {
    showError("Insufficient balance");
    showAvailableBalance(balance - fee);
}
```

#### Network Issues
- Timeout: 30 seconds
- Retry: 3 attempts
- Fallback to backup host
- Store for later retry

#### Daily Limit Exceeded
```java
if (dailyTotal + amount > dailyLimit) {
    showError("Daily limit exceeded");
    showRemainingLimit(dailyLimit - dailyTotal);
}
```

## Database Schema

### Transfer Table
```sql
CREATE TABLE transfers (
    id INTEGER PRIMARY KEY,
    reference_number TEXT UNIQUE,
    source_account TEXT,
    destination_account TEXT,
    destination_bank TEXT,
    beneficiary_name TEXT,
    amount BIGINT,
    fee INTEGER,
    status TEXT,
    approval_code TEXT,
    created_at TIMESTAMP,
    completed_at TIMESTAMP
);
```

### Beneficiary Table
```sql
CREATE TABLE beneficiaries (
    id INTEGER PRIMARY KEY,
    nickname TEXT,
    account_number TEXT,
    account_name TEXT,
    bank_code TEXT,
    bank_name TEXT,
    created_at TIMESTAMP,
    last_used TIMESTAMP,
    use_count INTEGER
);
```

## Scheduled Transfer

### Future Implementation
```java
ScheduledTransfer {
    scheduleType: "ONCE/RECURRING",
    scheduleDate: "2024-12-25",
    frequency: "MONTHLY",
    endDate: "2025-12-25",
    autoRetry: true,
    notifyBeforeExecution: true
}
```

## Settlement

### Daily Processing
- Compile transfer list
- Calculate net position
- Generate settlement file
- Submit to clearing house
- Reconciliation report

## UI Components

### Layouts
- `activity_transfer_enhanced.xml` - Main transfer screen
- `dialog_bank_selection.xml` - Bank picker
- `dialog_beneficiary_confirm.xml` - Name verification
- `layout_transfer_summary.xml` - Confirmation screen
- `activity_transfer_result.xml` - Result display

### Custom Components
- `AccountNumberInput` - Formatted input
- `BankSelector` - Searchable bank list
- `AmountInput` - Currency formatter
- `BeneficiaryCard` - Saved recipient

## Network Communication

### API Endpoints
```
POST /api/transfer/validate
POST /api/transfer/inquiry
POST /api/transfer/execute
GET /api/transfer/status/{ref}
GET /api/banks/list
```

### Timeout Configuration
```java
Connection timeout: 10 seconds
Read timeout: 30 seconds
Total timeout: 60 seconds
Retry delay: 2, 4, 8 seconds
```

## Testing Scenarios

### Success Cases
1. Same bank transfer
2. Inter-bank online transfer
3. Large amount RTGS transfer
4. Saved beneficiary transfer
5. Repeat recent transfer

### Failure Cases
1. Invalid account number
2. Insufficient balance
3. Wrong PIN
4. Network timeout
5. Host rejection
6. Daily limit exceeded

### Edge Cases
1. Exact balance transfer
2. Maximum amount transfer
3. Multiple transfers to same account
4. Concurrent transfers
5. Schedule date validation

## Performance Requirements
- Account validation: < 3 seconds
- Transfer execution: < 15 seconds
- Receipt generation: < 2 seconds
- Total transaction: < 30 seconds

## Compliance

### Regulatory Requirements
- KYC for large transfers
- Anti-money laundering checks
- Suspicious transaction reporting
- Cross-border restrictions
- Data privacy compliance

## Future Enhancements
- QR code transfers
- Voice-authenticated transfers
- Bulk transfer support
- International remittance
- Cryptocurrency integration
- AI fraud detection

## Notes
- Real-time processing for online transfers
- RTGS for amounts > 50 million
- Cutoff times apply for inter-bank
- Reversal possible within 24 hours
- Multi-currency support planned
- Integration with mobile wallet available