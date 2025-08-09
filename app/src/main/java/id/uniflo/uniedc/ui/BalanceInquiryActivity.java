package id.uniflo.uniedc.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ftpos.library.smartpos.emv.Emv;
import com.ftpos.library.smartpos.emv.EmvTags;
import com.ftpos.library.smartpos.emv.OnEmvResponse;
import com.ftpos.library.smartpos.emv.TransRequest;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.icreader.OnIcReaderCallback;
import com.ftpos.library.smartpos.util.BytesUtils;
import com.ftpos.library.smartpos.util.TLV;
import com.ftpos.library.smartpos.util.TlvUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.MainActivity;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.feitian.FeitianCardReader;
import id.uniflo.uniedc.sdk.feitian.FeitianEmvProcessor;
import id.uniflo.uniedc.sdk.interfaces.ICardReader;
import id.uniflo.uniedc.sdk.interfaces.IEmvProcessor;

public class BalanceInquiryActivity extends AppCompatActivity {
    
    private static final String TAG = "BalanceInquiry";
    private static final int REQUEST_PIN_VERIFICATION = 1004;
    
    private Toolbar toolbar;
    private TextView tvBalance;
    private TextView tvLastUpdate;
    private Button btnCheckBalance;
    
    private ICardReader cardReader;
    private IEmvProcessor emvProcessor;
    private Dialog cardInsertDialog;
    private Map<String, String> tlvData = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_inquiry);
        
        // Initialize SDK components through SdkManager
        initializeSdk();
        
        initViews();
        setupToolbar();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvBalance = findViewById(R.id.tv_balance);
        tvLastUpdate = findViewById(R.id.tv_last_update);
        btnCheckBalance = findViewById(R.id.btn_check_balance);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Balance Inquiry");
        }
    }
    
    private void setupListeners() {
        btnCheckBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBalance();
            }
        });
    }
    
    private void initializeSdk() {
        try {
            // Initialize card reader
            cardReader = new FeitianCardReader(this);
            int ret = cardReader.init();
            if (ret != 0) {
                Log.e(TAG, "Failed to initialize card reader: " + ret);
                Toast.makeText(this, "Failed to initialize card reader", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Initialize EMV processor
            emvProcessor = new FeitianEmvProcessor(this);
            ret = emvProcessor.init();
            if (ret != 0) {
                Log.e(TAG, "Failed to initialize EMV processor: " + ret);
                Toast.makeText(this, "Failed to initialize EMV processor", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d(TAG, "SDK components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SDK", e);
            Toast.makeText(this, "Error initializing SDK: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void checkBalance() {
        // Show card insertion dialog
        showCardInsertDialog();
    }
    
    private void performBalanceCheck() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking balance...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Simulate balance check
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                
                // Update balance (demo value)
                long balance = 25750000; // Rp 25,750,000
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                tvBalance.setText(formatter.format(balance));
                
                // Update last update time
                tvLastUpdate.setText("Last updated: Just now");
                
                // Show success message
                tvBalance.setVisibility(View.VISIBLE);
                tvLastUpdate.setVisibility(View.VISIBLE);
            }
        }, 2000);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_PIN_VERIFICATION) {
            if (resultCode == PinVerificationActivity.RESULT_PIN_VERIFIED) {
                performBalanceCheck();
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up SDK resources
        if (cardReader != null) {
            cardReader.release();
        }
        if (emvProcessor != null) {
            emvProcessor.release();
        }
    }
    
    private void showCardInsertDialog() {
        cardInsertDialog = new Dialog(this);
        cardInsertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cardInsertDialog.setContentView(R.layout.dialog_insert_card);
        cardInsertDialog.setCancelable(false);
        
        TextView tvTitle = cardInsertDialog.findViewById(R.id.tv_title);
        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
        ProgressBar progressBar = cardInsertDialog.findViewById(R.id.progress_bar);
        Button btnCancel = cardInsertDialog.findViewById(R.id.btn_cancel);
        
        tvTitle.setText("Insert Card");
        tvMessage.setText("Please insert your card for balance inquiry");
        
        btnCancel.setOnClickListener(v -> {
            if (cardReader != null) {
                cardReader.close();
            }
            cardInsertDialog.dismiss();
        });
        
        cardInsertDialog.show();
        
        // Start card detection
        detectCard();
    }
    
    private void detectCard() {
        if (cardReader == null) {
            Toast.makeText(this, "Card reader not initialized", Toast.LENGTH_SHORT).show();
            if (cardInsertDialog != null) {
                cardInsertDialog.dismiss();
            }
            return;
        }
        
        // Detect IC card
        cardReader.open(ICardReader.CARD_TYPE_IC, 30, new ICardReader.ICardDetectListener() {
            @Override
            public void onCardDetected(int cardType) {
                Log.d(TAG, "Card detected, type: " + cardType);
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        ProgressBar progressBar = cardInsertDialog.findViewById(R.id.progress_bar);
                        tvMessage.setText("Card detected, processing...");
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
                
                // Process EMV transaction for balance inquiry
                processBalanceInquiry();
            }
            
            @Override
            public void onCardRemoved() {
                Log.d(TAG, "Card removed");
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        cardInsertDialog.dismiss();
                    }
                    Toast.makeText(BalanceInquiryActivity.this, 
                        "Card removed", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onTimeout() {
                Log.e(TAG, "Card detection timeout");
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        cardInsertDialog.dismiss();
                    }
                    Toast.makeText(BalanceInquiryActivity.this, 
                        "Card detection timeout", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.e(TAG, "Card detection error: " + errorCode + " - " + errorMsg);
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        cardInsertDialog.dismiss();
                    }
                    Toast.makeText(BalanceInquiryActivity.this, 
                        "Card detection failed: " + errorMsg, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void processBalanceInquiry() {
        if (emvProcessor == null) {
            Log.e(TAG, "EMV processor not initialized");
            runOnUiThread(() -> {
                if (cardInsertDialog != null) {
                    cardInsertDialog.dismiss();
                }
                Toast.makeText(this, "EMV processor not initialized", Toast.LENGTH_SHORT).show();
            });
            return;
        }
        
        try {
            // Set up transaction data for balance inquiry
            Map<String, Object> transData = new HashMap<>();
            transData.put("transType", IEmvProcessor.TRANS_TYPE_BALANCE_INQUIRY);
            transData.put("amount", 0L); // 0 amount for balance inquiry
            transData.put("otherAmount", 0L);
            
            // Start EMV process
            emvProcessor.startTransaction(transData, new IEmvProcessor.IEmvTransactionListener() {
                @Override
                public void onSelectApplication(List<String> appList) {
                    Log.d(TAG, "Select application: " + appList);
                    // Auto-select first application
                    if (appList != null && !appList.isEmpty()) {
                        emvProcessor.selectApplication(0);
                    }
                }
                
                @Override
                public void onConfirmCardInfo(String cardNo, String cardType) {
                    Log.d(TAG, "Card number: " + cardNo);
                    emvProcessor.confirmCardInfo(true);
                }
                
                @Override
                public void onRequestPin(boolean isOnlinePin, int retryTimes) {
                    Log.d(TAG, "PIN requested, online: " + isOnlinePin + ", retries: " + retryTimes);
                    // For demo, bypass PIN
                    emvProcessor.cancelPin();
                }
                
                @Override
                public void onRequestOnline() {
                    Log.d(TAG, "Online request - collecting TLV data");
                    // Collect TLV data including ARQC
                    collectTLVData();
                    
                    // For demo, approve the transaction
                    Map<String, String> onlineResult = new HashMap<>();
                    onlineResult.put("responseCode", "00");
                    onlineResult.put("authCode", "123456");
                    emvProcessor.importOnlineResult(true, onlineResult);
                }
                
                @Override
                public void onTransactionResult(int result, Map<String, String> data) {
                    Log.d(TAG, "Transaction result: " + result);
                    if (data != null) {
                        tlvData.putAll(data);
                    }
                    handleTransactionResult(result);
                }
                
                @Override
                public void onError(int errorCode, String errorMsg) {
                    Log.e(TAG, "EMV error: " + errorCode + " - " + errorMsg);
                    runOnUiThread(() -> {
                        if (cardInsertDialog != null) {
                            cardInsertDialog.dismiss();
                        }
                        Toast.makeText(BalanceInquiryActivity.this, 
                            "EMV error: " + errorMsg, 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "EMV process error", e);
            runOnUiThread(() -> {
                if (cardInsertDialog != null) {
                    cardInsertDialog.dismiss();
                }
                Toast.makeText(this, "EMV process error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void collectTLVData() {
        try {
            // Collect required TLV data for ARQC
            String[] tags = {
                "84",   // DF Name (AID)
                "9F02", // Amount Authorized
                "9F03", // Amount Other
                "9F1A", // Terminal Country Code
                "95",   // TVR
                "5F2A", // Transaction Currency Code
                "9A",   // Transaction Date
                "9C",   // Transaction Type
                "9F37", // Unpredictable Number
                "82",   // AIP
                "9F36", // ATC
                "9F10", // IAD
                "9F26", // ARQC
                "5F34", // PAN Sequence Number
                "9F27", // CID
                "5A"    // PAN
            };
            
            // Clear previous data but keep any data from transaction result
            Map<String, String> existingData = new HashMap<>(tlvData);
            tlvData.clear();
            tlvData.putAll(existingData);
            
            // Get TLV data from EMV processor
            for (String tag : tags) {
                String value = emvProcessor.getTlvData(tag);
                if (value != null && !value.isEmpty()) {
                    tlvData.put(tag, value);
                    Log.d(TAG, "TLV " + tag + ": " + value);
                }
            }
            
            // Log ARQC specifically
            String arqc = tlvData.get("9F26");
            if (arqc != null) {
                Log.d(TAG, "ARQC Generated: " + arqc);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error collecting TLV data", e);
        }
    }
    
    private void handleTransactionResult(int result) {
        runOnUiThread(() -> {
            if (cardInsertDialog != null) {
                cardInsertDialog.dismiss();
            }
            
            if (result == 0) { // Success
                // Show TLV data in a dialog or process the balance
                showTLVDataDialog();
                
                // Update balance display (demo value)
                long balance = 25750000; // Rp 25,750,000
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                tvBalance.setText(formatter.format(balance));
                tvLastUpdate.setText("Last updated: Just now");
                tvBalance.setVisibility(View.VISIBLE);
                tvLastUpdate.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Transaction failed. Result: " + result, Toast.LENGTH_LONG).show();
            }
            
            // Close card reader
            if (cardReader != null) {
                cardReader.close();
            }
        });
    }
    
    private void showTLVDataDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EMV TLV Data ===\n\n");
        
        if (tlvData.get("9F26") != null) {
            sb.append("ARQC: ").append(tlvData.get("9F26")).append("\n\n");
        }
        
        for (Map.Entry<String, String> entry : tlvData.entrySet()) {
            sb.append("Tag ").append(entry.getKey()).append(": ")
              .append(entry.getValue()).append("\n");
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Transaction TLV Data")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .show();
    }
}