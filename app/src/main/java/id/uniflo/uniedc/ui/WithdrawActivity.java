package id.uniflo.uniedc.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Locale;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.widget.AmountEditText;
import id.uniflo.uniedc.sdk.interfaces.ICardReader;
import id.uniflo.uniedc.sdk.interfaces.IPinpad;
import id.uniflo.uniedc.sdk.interfaces.ISDKProvider;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.util.EMVUtil;
import id.uniflo.uniedc.util.BytesUtils;
import android.util.Log;

public class WithdrawActivity extends AppCompatActivity {
    
    private static final String TAG = "WithdrawActivity";
    private static final int REQUEST_PIN_VERIFICATION = 1002;
    
    private Toolbar toolbar;
    private TextView tvBalance;
    private ChipGroup chipGroupAmount;
    private TextInputLayout tilCustomAmount;
    private AmountEditText etCustomAmount;
    private Button btnWithdraw;
    
    private long selectedAmount = 0;
    private SDKManager sdkManager;
    private ICardReader cardReader;
    private IPinpad pinpad;
    private byte[] cardData;
    private String cardNumber;
    private ProgressDialog cardDetectDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        
        initViews();
        setupToolbar();
        setupListeners();
        initializeSDK();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvBalance = findViewById(R.id.tv_balance);
        chipGroupAmount = findViewById(R.id.chip_group_amount);
        tilCustomAmount = findViewById(R.id.til_custom_amount);
        etCustomAmount = findViewById(R.id.et_custom_amount);
        btnWithdraw = findViewById(R.id.btn_withdraw);
        
        // Set demo balance
        tvBalance.setText("Rp 15,250,000");
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cash Withdrawal");
        }
    }
    
    private void setupListeners() {
        // Chip selection listener
        chipGroupAmount.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    String amountText = chip.getText().toString()
                            .replace("Rp ", "")
                            .replace(".", "");
                    
                    if (amountText.equals("Other")) {
                        tilCustomAmount.setVisibility(View.VISIBLE);
                        etCustomAmount.requestFocus();
                        selectedAmount = 0;
                    } else {
                        tilCustomAmount.setVisibility(View.GONE);
                        etCustomAmount.setText("");
                        selectedAmount = Long.parseLong(amountText);
                    }
                }
            }
        });
        
        btnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performWithdrawal();
            }
        });
    }
    
    private void performWithdrawal() {
        long amount = selectedAmount;
        
        // Check if custom amount is selected
        if (chipGroupAmount.getCheckedChipId() == R.id.chip_other) {
            if (!etCustomAmount.hasValidAmount()) {
                tilCustomAmount.setError("Please enter amount");
                return;
            }
            
            amount = etCustomAmount.getAmount();
            if (amount % 50000 != 0) {
                tilCustomAmount.setError("Amount must be multiple of Rp 50,000");
                return;
            }
        }
        
        if (amount == 0) {
            Toast.makeText(this, "Please select an amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (amount > 15250000) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Store withdrawal amount for later use
        getIntent().putExtra("withdrawal_amount", amount);
        
        // Check for card first
        detectCard();
    }
    
    private void processWithdrawal(long amount) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing withdrawal...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Simulate withdrawal process
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                
                // Format amount
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                String formattedAmount = formatter.format(amount);
                
                // Generate transaction ID
                String transactionId = "WDR" + System.currentTimeMillis();
                
                // Navigate to success page
                TransactionSuccessActivity.start(WithdrawActivity.this,
                    "withdrawal",
                    formattedAmount,
                    null,
                    transactionId,
                    "Please take your cash from the dispenser");
                
                // Close this activity
                finish();
            }
        }, 2000);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_PIN_VERIFICATION) {
            if (resultCode == PinVerificationActivity.RESULT_PIN_VERIFIED) {
                // Retrieve stored withdrawal amount
                long amount = getIntent().getLongExtra("withdrawal_amount", 0);
                processWithdrawal(amount);
            } else if (resultCode == PinVerificationActivity.RESULT_PIN_CANCELLED) {
                Toast.makeText(this, "Withdrawal cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void initializeSDK() {
        sdkManager = SDKManager.getInstance();
        sdkManager.initializeSDK(new ISDKProvider.IInitCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "SDK initialized successfully");
                runOnUiThread(() -> {
                    cardReader = sdkManager.getCardReader();
                    pinpad = sdkManager.getPinpad();
                });
            }
            
            @Override
            public void onError(int errorCode, String error) {
                Log.e(TAG, "SDK initialization failed: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(WithdrawActivity.this, "SDK initialization failed", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void detectCard() {
        if (cardReader == null) {
            Toast.makeText(this, "Card reader not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        cardDetectDialog = new ProgressDialog(this);
        cardDetectDialog.setMessage("Please insert or tap your card...");
        cardDetectDialog.setCancelable(true);
        cardDetectDialog.setOnCancelListener(dialog -> {
            if (cardReader != null) {
                cardReader.close();
            }
        });
        cardDetectDialog.show();
        
        // Open card reader for all card types
        int cardTypes = ICardReader.CARD_TYPE_MAG | ICardReader.CARD_TYPE_IC | ICardReader.CARD_TYPE_NFC;
        cardReader.open(cardTypes, 60, new ICardReader.ICardDetectListener() {
            @Override
            public void onCardDetected(int cardType) {
                Log.d(TAG, "Card detected: " + cardType);
                runOnUiThread(() -> {
                    if (cardDetectDialog != null) {
                        cardDetectDialog.setMessage("Reading card...");
                    }
                });
                
                // Read card data
                readCardAndProcessTransaction(cardType);
            }
            
            @Override
            public void onCardRemoved() {
                Log.d(TAG, "Card removed");
            }
            
            @Override
            public void onTimeout() {
                runOnUiThread(() -> {
                    if (cardDetectDialog != null) {
                        cardDetectDialog.dismiss();
                    }
                    Toast.makeText(WithdrawActivity.this, "Card detection timeout", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(int errorCode, String error) {
                runOnUiThread(() -> {
                    if (cardDetectDialog != null) {
                        cardDetectDialog.dismiss();
                    }
                    Toast.makeText(WithdrawActivity.this, "Card detection error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void readCardAndProcessTransaction(int cardType) {
        if (cardType == ICardReader.CARD_TYPE_IC) {
            // Power on the IC card
            byte[] atr = cardReader.powerOn();
            if (atr == null || atr.length == 0) {
                runOnUiThread(() -> {
                    if (cardDetectDialog != null) {
                        cardDetectDialog.dismiss();
                    }
                    Toast.makeText(this, "Failed to power on card", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            
            // Process EMV transaction
            processEMVTransaction();
        } else if (cardType == ICardReader.CARD_TYPE_NFC) {
            // Handle NFC card
            processNFCTransaction();
        } else if (cardType == ICardReader.CARD_TYPE_MAG) {
            // Handle magnetic card
            processMagneticTransaction();
        }
    }
    
    private void processEMVTransaction() {
        // Get withdrawal amount
        long amount = getIntent().getLongExtra("withdrawal_amount", 0);
        
        runOnUiThread(() -> {
            if (cardDetectDialog != null) {
                cardDetectDialog.setMessage("Processing EMV transaction...");
            }
        });
        
        // Build EMV data for withdrawal transaction
        byte[] emvData = EMVUtil.buildWithdrawalEMVData(amount);
        
        // Send APDU commands to read card data
        // Select PPSE
        byte[] selectPPSE = BytesUtils.hexStringToBytes("00A404000E325041592E5359532E444446303100");
        byte[] ppseResponse = cardReader.sendApdu(selectPPSE);
        
        if (ppseResponse != null && ppseResponse.length >= 2 && 
            ppseResponse[ppseResponse.length-2] == (byte)0x90 && 
            ppseResponse[ppseResponse.length-1] == 0x00) {
            // Parse PPSE response and select AID
            // For demo, using a common AID
            byte[] selectAID = BytesUtils.hexStringToBytes("00A4040008A000000003101000");
            byte[] aidResponse = cardReader.sendApdu(selectAID);
            
            if (aidResponse != null && aidResponse.length >= 2 && 
                aidResponse[aidResponse.length-2] == (byte)0x90 && 
                aidResponse[aidResponse.length-1] == 0x00) {
                // Get Processing Options
                byte[] gpo = BytesUtils.hexStringToBytes("80A8000002830000");
                byte[] gpoResponse = cardReader.sendApdu(gpo);
                
                if (gpoResponse != null && gpoResponse.length >= 2 && 
                    gpoResponse[gpoResponse.length-2] == (byte)0x90 && 
                    gpoResponse[gpoResponse.length-1] == 0x00) {
                    // Read records and get card number
                    readCardRecords();
                    
                    // Now request PIN verification
                    runOnUiThread(() -> {
                        if (cardDetectDialog != null) {
                            cardDetectDialog.dismiss();
                        }
                        verifyPINWithPinpad();
                    });
                } else {
                    handleTransactionError("GPO failed");
                }
            } else {
                handleTransactionError("AID selection failed");
            }
        } else {
            handleTransactionError("PPSE selection failed");
        }
    }
    
    private void processNFCTransaction() {
        // Similar to EMV but for contactless
        processEMVTransaction();
    }
    
    private void processMagneticTransaction() {
        // Read track 2 data
        String track2 = cardReader.getTrackData(2);
        if (track2 != null && !track2.isEmpty()) {
            // Extract PAN from track 2
            int separatorIndex = track2.indexOf('=');
            if (separatorIndex > 0) {
                cardNumber = track2.substring(0, separatorIndex);
                cardData = track2.getBytes();
            }
            
            runOnUiThread(() -> {
                if (cardDetectDialog != null) {
                    cardDetectDialog.dismiss();
                }
                // For magnetic cards, go directly to PIN verification
                verifyPINWithPinpad();
            });
        } else {
            handleTransactionError("Failed to read magnetic card");
        }
    }
    
    private void readCardRecords() {
        // Read SFI records to get card number
        // This is simplified - real implementation would parse GPO response
        byte[] readRecord = BytesUtils.hexStringToBytes("00B2010C00");
        byte[] recordResponse = cardReader.sendApdu(readRecord);
        
        if (recordResponse != null && recordResponse.length > 2 && 
            recordResponse[recordResponse.length-2] == (byte)0x90 && 
            recordResponse[recordResponse.length-1] == 0x00) {
            // Remove SW bytes
            byte[] responseData = new byte[recordResponse.length - 2];
            System.arraycopy(recordResponse, 0, responseData, 0, responseData.length);
            // Extract PAN from record - simplified
            cardNumber = EMVUtil.extractPAN(responseData);
            cardData = responseData;
        }
    }
    
    private void verifyPINWithPinpad() {
        if (pinpad == null) {
            Toast.makeText(this, "Pinpad not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        long amount = getIntent().getLongExtra("withdrawal_amount", 0);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(amount);
        
        // Show PIN entry dialog
        ProgressDialog pinDialog = new ProgressDialog(this);
        pinDialog.setMessage("Enter PIN for withdrawal " + formattedAmount);
        pinDialog.setCancelable(false);
        pinDialog.show();
        
        // Use hardware pinpad for secure PIN entry
        pinpad.inputPin(cardNumber, 0, 60, new IPinpad.IPinInputListener() {
            @Override
            public void onPinEntered(byte[] pinBlock) {
                runOnUiThread(() -> {
                    pinDialog.dismiss();
                    // Process transaction with PIN
                    processTransactionWithPIN(pinBlock);
                });
            }
            
            @Override
            public void onCancelled() {
                runOnUiThread(() -> {
                    pinDialog.dismiss();
                    Toast.makeText(WithdrawActivity.this, "PIN entry cancelled", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onTimeout() {
                runOnUiThread(() -> {
                    pinDialog.dismiss();
                    Toast.makeText(WithdrawActivity.this, "PIN entry timeout", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(int errorCode, String error) {
                runOnUiThread(() -> {
                    pinDialog.dismiss();
                    Toast.makeText(WithdrawActivity.this, "PIN entry error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void processTransactionWithPIN(byte[] pinBlock) {
        long amount = getIntent().getLongExtra("withdrawal_amount", 0);
        
        ProgressDialog processDialog = new ProgressDialog(this);
        processDialog.setMessage("Processing withdrawal transaction...");
        processDialog.setCancelable(false);
        processDialog.show();
        
        // Generate ARQC for online authorization
        byte[] arqc = generateARQC(amount, pinBlock);
        
        // Simulate sending to host for authorization
        new Handler().postDelayed(() -> {
            processDialog.dismiss();
            
            // Assume authorization successful
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String formattedAmount = formatter.format(amount);
            
            // Generate transaction ID
            String transactionId = "WDR" + System.currentTimeMillis();
            
            // Navigate to success page
            TransactionSuccessActivity.start(WithdrawActivity.this,
                "withdrawal",
                formattedAmount,
                null,
                transactionId,
                "Please take your cash from the dispenser");
            
            // Power off card
            if (cardReader != null) {
                cardReader.powerOff();
            }
            
            // Close this activity
            finish();
        }, 3000);
    }
    
    private byte[] generateARQC(long amount, byte[] pinBlock) {
        // Build transaction data for ARQC generation
        // In real implementation, this would use EMV kernel to generate ARQC
        // For demo, we'll return a dummy ARQC
        return new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
    }
    
    private void handleTransactionError(String error) {
        Log.e(TAG, "Transaction error: " + error);
        runOnUiThread(() -> {
            if (cardDetectDialog != null) {
                cardDetectDialog.dismiss();
            }
            Toast.makeText(this, "Transaction failed: " + error, Toast.LENGTH_SHORT).show();
            
            // Power off card if needed
            if (cardReader != null) {
                cardReader.powerOff();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
        if (cardReader != null) {
            cardReader.close();
        }
        if (cardDetectDialog != null && cardDetectDialog.isShowing()) {
            cardDetectDialog.dismiss();
        }
    }
}