Here's the complete flow for the balance inquiry feature:

1. Entry Point - DashboardModernActivity.java

- User clicks balance inquiry button in dashboard
- Launches BalanceInquiryActivityBasic (line 93)

2. Main Activity - BalanceInquiryActivityBasic.java

Key components:

Initialization (onCreate - line 73)

- Sets up UI views
- Initializes SDK service connection
- Sets up managers (SettingsManager, TransactionDAO, SecureSettingsDAO)

Card Detection Flow:

1. checkBalance() (line 138) - Entry point when user clicks check balance
2. showCardInsertDialog() (line 152) - Shows card insertion dialog
3. detectCard() (line 236) - Starts card detection process
4. performPowerCycleDetection() (line 249) - Power cycle approach for card detection
5. detectCardWithTimeout() (line 290) - Actual card detection with timeout
6. onCardATR callback (line 296) - Triggered when card is inserted
7. processCardWithTimeout() (line 555) - Sets up timeout mechanism
8. processCard() (line 634) - Main card processing logic
   - Tries multiple AIDs (NSICCS, Mastercard, Visa)
   - Sends APDU commands to select AID

PIN Entry Flow:

9. showPinInputDialog() (line 764) - Shows modern PIN input dialog
   - Custom number pad UI
   - 6-digit PIN input with visual dots
   - OK/Cancel functionality

Transaction Processing:

10. collectTLVDataAndProcessWithPin() (line 924) - Builds TLV data with PIN
    - Generates PIN block
    - Reads card PAN
    - Builds transaction data (MTI, Processing Code, STAN, etc.)
    - Generates ARQC using EMVUtil

Host Communication:

11. sendToHost() (line 1064) - Sends transaction to backend
    - Builds JSON request from TLV data
    - HTTP POST to balance inquiry API
    - Handles timeout and retries
12. handleHostResponse() (line 1156) - Processes backend response
13. showBalanceResultFromHost() (line 1188) - Updates UI with balance
    - Creates transaction record
    - Saves to database
    - Launches result activity

3. Result Display - BalanceInquiryResultActivity.java

- Displays balance inquiry result
- Shows transaction details
- Provides print/reprint functionality
- Allows user to close and return

Key Features:

- Card Detection: Multiple retry mechanisms with power cycling
- AID Support: NSICCS (Indonesian), Mastercard, Visa
- PIN Security: PIN block generation with PAN XOR
- EMV Processing: ARQC generation, TLV data handling
- Error Handling: Timeout mechanisms, retry logic, fallback options
- Transaction Logging: Saves all transactions to database
- Network Communication: Primary/secondary host support
- Debug Logging: Comprehensive debug logs for troubleshooting

Flow Summary:

1. Dashboard → Balance Inquiry Activity
2. Initialize card reader
3. Insert card → Detect card with ATR
4. Select AID → Process EMV data
5. Enter PIN → Generate PIN block
6. Build transaction data (TLV)
7. Send to host → Get balance
8. Display result → Save transaction
9. Print receipt (optional)

The system handles both online (with host) and offline (demo) modes, with comprehensive error handling and retry
mechanisms for card detection issues.
