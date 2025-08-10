# Cash Withdrawal Flow Documentation

## Overview
Cash Withdrawal (Tarik Tunai) allows customers to withdraw cash from their accounts using debit/credit cards at merchant locations.

## Entry Points
- **Dashboard**: `DashboardModernActivity.java` → Cash Withdrawal button
- **Home Screen**: `BasHomeActivity.java` → Cash Withdrawal menu
- **Direct Access**: `BasCashWithdrawalActivity.java` or `CashWithdrawalModernActivity.java`

## Main Components

### 1. BasCashWithdrawalActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/BasCashWithdrawalActivity.java`

#### Key Features:
- Preset withdrawal amounts
- Custom amount entry
- Card validation
- PIN verification
- Balance checking
- Transaction processing

#### Key Methods:

##### Initialization
```java
onCreate()
├── initializeViews() - Setup UI components
├── setupAmountButtons() - Configure quick amounts
├── initializeCardReader() - Setup SDK
└── loadConfiguration() - Load terminal settings
```

##### Amount Selection
```java
Quick Amounts:
├── Rp 50,000
├── Rp 100,000
├── Rp 200,000
├── Rp 500,000
├── Rp 1,000,000
└── Custom Amount (Other)
```

##### Transaction Flow
```java
processWithdrawal()
├── validateAmount() - Check limits
├── readCard() - Get card data
├── verifyPIN() - Authenticate user
├── checkBalance() - Ensure sufficient funds
├── sendTransaction() - Process withdrawal
└── dispenseCash() - Complete transaction
```

### 2. CashWithdrawalModernActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/CashWithdrawalModernActivity.java`

#### Modern UI Features:
- Material Design interface
- Animated card insertion
- Real-time balance display
- Transaction progress tracking
- Enhanced error handling

## Transaction Flow

### Complete Withdrawal Process
```
1. Select Amount
   ├── Choose preset amount OR
   └── Enter custom amount
       └── Validate against limits

2. Insert/Tap Card
   ├── Detect card presence
   ├── Read card data
   └── Validate card type

3. Enter PIN
   ├── Display secure PIN pad
   ├── Capture 6-digit PIN
   └── Generate PIN block

4. Verify Balance
   ├── Send balance inquiry
   ├── Check sufficient funds
   └── Apply withdrawal limits

5. Process Transaction
   ├── Build withdrawal request
   ├── Generate security codes
   ├── Send to host
   └── Wait for approval

6. Complete Transaction
   ├── Display result
   ├── Print receipt
   ├── Log transaction
   └── Return card
```

## Data Structure

### Withdrawal Request
```java
{
  "MTI": "0200",
  "ProcessingCode": "010000",  // Cash withdrawal
  "Amount": "500000",
  "PAN": "6234567890123456",
  "ExpiryDate": "2512",
  "EntryMode": "051",
  "PINBlock": "[Encrypted]",
  "Track2Data": "[Card Track2]",
  "TerminalID": "12345678",
  "MerchantID": "123456789012345",
  "CurrencyCode": "360",
  "ARQC": "[Cryptogram]",
  "ATC": "0001",
  "TVR": "0000000000"
}
```

### Withdrawal Response
```java
{
  "responseCode": "00",
  "approvalCode": "789012",
  "availableBalance": "2500000",
  "ledgerBalance": "2500000",
  "referenceNumber": "202412201556",
  "message": "Withdrawal Approved"
}
```

## Limits and Validations

### Amount Limits
```java
Minimum: Rp 50,000
Maximum per transaction: Rp 5,000,000
Daily limit: Rp 10,000,000
Monthly limit: Rp 50,000,000
```

### Card Validations
- Card expiry check
- Card type verification (Debit only)
- BIN validation
- Blacklist checking
- Daily transaction count

## Security Features

### Multi-Layer Security
1. **Card Authentication**
   - EMV chip verification
   - ARQC cryptogram
   - Card validation codes

2. **PIN Verification**
   - Encrypted PIN block
   - Limited attempts (3)
   - Temporary card block

3. **Transaction Security**
   - End-to-end encryption
   - MAC verification
   - Session keys
   - Timeout protection

4. **Audit Trail**
   - Complete transaction log
   - Digital signatures
   - Timestamp verification

## Error Handling

### Common Error Scenarios

#### Insufficient Funds
```java
if (balance < withdrawalAmount + fee) {
    showError("Insufficient funds");
    offerLowerAmount();
}
```

#### Card Errors
- Card not supported
- Card expired
- Card blocked
- Read failure

#### Network Errors
- Timeout handling
- Automatic retry
- Store-and-forward
- Reversal processing

#### PIN Errors
```java
MAX_PIN_ATTEMPTS = 3;
if (pinAttempts >= MAX_PIN_ATTEMPTS) {
    blockCard();
    notifyIssuer();
}
```

## Receipt Format

### Customer Receipt
```
=================================
      UNIFLO MERCHANT
   Jl. Sudirman No. 123
        Jakarta
=================================
DATE: 20/12/2024    TIME: 15:56
TERMINAL ID: 12345678
MERCHANT ID: 123456789012345
---------------------------------
     CASH WITHDRAWAL
---------------------------------
CARD NO:      ****3456
CARD TYPE:    DEBIT
ENTRY MODE:   CHIP

AMOUNT:       Rp    500,000
FEE:          Rp      6,500
-------------
TOTAL:        Rp    506,500

BALANCE:      Rp  2,000,000

APPROVAL:     789012
REF NO:       202412201556
BATCH:        000001
TRACE:        000456

    ** CUSTOMER COPY **
=================================
```

### Merchant Copy
- Same format with "MERCHANT COPY"
- Additional settlement information
- Signature line (if required)

## Database Operations

### Transaction Storage
```sql
INSERT INTO transactions (
  type,
  amount,
  fee,
  status,
  card_number,
  approval_code,
  reference_number,
  balance_after,
  terminal_id,
  merchant_id,
  created_at
) VALUES (
  'WITHDRAWAL',
  500000,
  6500,
  'SUCCESS',
  '****3456',
  '789012',
  '202412201556',
  2000000,
  '12345678',
  '123456789012345',
  CURRENT_TIMESTAMP
);
```

### Audit Log
```sql
INSERT INTO audit_log (
  action,
  user_id,
  card_number,
  amount,
  result,
  timestamp,
  ip_address,
  device_info
) VALUES (...);
```

## Configuration

### Terminal Settings
```java
withdrawal.enabled = true
withdrawal.min_amount = 50000
withdrawal.max_amount = 5000000
withdrawal.fee_fixed = 6500
withdrawal.fee_percentage = 0
withdrawal.daily_limit = 10000000
withdrawal.require_pin = true
withdrawal.print_balance = true
```

### Network Configuration
```java
host.primary = "10.0.0.1"
host.backup = "10.0.0.2"
port = 8583
timeout = 30
retry_count = 3
use_ssl = true
```

## UI Components

### Layouts
- `activity_cash_withdrawal.xml` - Basic layout
- `activity_cash_withdrawal_modern.xml` - Modern UI
- `dialog_amount_selection.xml` - Amount picker
- `dialog_pin_entry.xml` - PIN pad
- `layout_withdrawal_result.xml` - Result display

### Custom Views
- `AmountSelector` - Quick amount grid
- `PinPadView` - Secure PIN entry
- `CardAnimationView` - Card insertion animation
- `ProgressIndicator` - Transaction progress

## Fee Structure

### Fee Calculation
```java
calculateFee(amount) {
    if (sameBank) {
        return 0;
    } else if (amount <= 1000000) {
        return 6500;
    } else {
        return 7500;
    }
}
```

### Fee Display
- Show before confirmation
- Include in total amount
- Print on receipt
- Log separately

## Settlement

### Daily Settlement
- Count withdrawal transactions
- Calculate total amount
- Compute fees collected
- Generate settlement report
- Submit to acquirer

## Compliance

### Regulatory Requirements
- Maximum withdrawal limits
- KYC verification for large amounts
- Suspicious transaction reporting
- Anti-money laundering checks
- Data retention policies

## Testing Scenarios

### Success Cases
1. Standard withdrawal with sufficient balance
2. Maximum amount withdrawal
3. Multiple withdrawals in session
4. Different card types

### Failure Cases
1. Insufficient balance
2. Exceeded daily limit
3. Wrong PIN (1st, 2nd, 3rd attempt)
4. Card blocked
5. Network timeout
6. Host decline

### Edge Cases
1. Exact balance withdrawal
2. Withdrawal with fee exceeding balance
3. Card removed during transaction
4. Power failure recovery
5. Duplicate request handling

## Performance Metrics
- Card read time: < 2 seconds
- PIN entry timeout: 30 seconds
- Transaction approval: < 10 seconds
- Receipt printing: < 5 seconds
- Total transaction: < 30 seconds

## Dependencies
- Card reader SDK
- PIN pad module
- Printer service
- Network manager
- Database layer
- Security module

## Notes
- Supports savings and current accounts
- Real-time balance updates
- Offline mode not supported for withdrawals
- Daily reconciliation required
- Cash management integration available
- Multi-currency support (future)