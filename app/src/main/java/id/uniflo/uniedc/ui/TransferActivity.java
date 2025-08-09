package id.uniflo.uniedc.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.widget.AmountEditText;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.ICardReader;
import id.uniflo.uniedc.utils.EMVUtil;

public class TransferActivity extends AppCompatActivity {
    
    private static final String TAG = "TransferActivity";
    
    private Toolbar toolbar;
    private TextInputLayout tilBank;
    private AutoCompleteTextView autoCompleteBank;
    private TextInputLayout tilAccountNumber;
    private TextInputLayout tilAmount;
    private TextInputEditText etAccountNumber;
    private AmountEditText etAmount;
    private Button btnTransfer;
    private TextView tvBalance;
    
    // Indonesian Banks
    private static final String[] INDONESIAN_BANKS = {
        "Bank Central Asia (BCA)",
        "Bank Mandiri",
        "Bank Negara Indonesia (BNI)",
        "Bank Rakyat Indonesia (BRI)",
        "Bank Tabungan Negara (BTN)",
        "Bank CIMB Niaga",
        "Bank Danamon",
        "Bank Permata",
        "Bank Maybank Indonesia",
        "Bank OCBC NISP",
        "Bank Panin",
        "Bank UOB Indonesia",
        "Bank Sinarmas",
        "Bank Mega",
        "Bank Bukopin",
        "Bank BTPN",
        "Bank Muamalat",
        "Bank Syariah Indonesia (BSI)",
        "Bank Jago",
        "Bank Neo Commerce",
        "SeaBank",
        "Blu by BCA Digital",
        "Jenius (BTPN)",
        "TMRW by UOB",
        "Bank DKI",
        "Bank BJB",
        "Bank Jateng",
        "Bank Jatim",
        "Bank BPD DIY",
        "Bank Sumut",
        "Bank Sumsel Babel",
        "Bank Riau Kepri",
        "Bank Jambi",
        "Bank Bengkulu",
        "Bank Lampung",
        "Bank Kalbar",
        "Bank Kalteng",
        "Bank Kalsel",
        "Bank Kaltim",
        "Bank Sulsel",
        "Bank Sultra",
        "Bank BPD Sulteng",
        "Bank Sulut",
        "Bank BPD Bali",
        "Bank NTB",
        "Bank NTT",
        "Bank Maluku",
        "Bank Papua"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        
        initViews();
        setupToolbar();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilBank = findViewById(R.id.til_bank);
        autoCompleteBank = findViewById(R.id.auto_complete_bank);
        tilAccountNumber = findViewById(R.id.til_account_number);
        tilAmount = findViewById(R.id.til_amount);
        etAccountNumber = findViewById(R.id.et_account_number);
        etAmount = findViewById(R.id.et_amount);
        btnTransfer = findViewById(R.id.btn_transfer);
        tvBalance = findViewById(R.id.tv_balance);
        
        // Set demo balance
        tvBalance.setText("Rp 15,250,000");
        
        // Setup bank dropdown
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, INDONESIAN_BANKS);
        autoCompleteBank.setAdapter(bankAdapter);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.transfer_title));
        }
    }
    
    private void setupListeners() {
        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performTransfer();
            }
        });
    }
    
    private void performTransfer() {
        String selectedBank = autoCompleteBank.getText().toString().trim();
        String accountNumber = etAccountNumber.getText().toString().trim();
        
        // Clear errors
        tilBank.setError(null);
        tilAccountNumber.setError(null);
        tilAmount.setError(null);
        
        // Validate
        if (selectedBank.isEmpty()) {
            tilBank.setError(getString(R.string.select_bank));
            return;
        }
        
        if (accountNumber.isEmpty()) {
            tilAccountNumber.setError(getString(R.string.destination_account));
            return;
        }
        
        if (accountNumber.length() < 10) {
            tilAccountNumber.setError(getString(R.string.destination_account));
            return;
        }
        
        if (!etAmount.hasValidAmount()) {
            tilAmount.setError(getString(R.string.amount));
            return;
        }
        
        long amount = etAmount.getAmount();
        if (amount > 15250000) {
            tilAmount.setError(getString(R.string.insufficient_balance));
            return;
        }
        
        // Format amount for display
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(amount);
        
        // Store transfer details for later use
        getIntent().putExtra("transfer_account", accountNumber);
        getIntent().putExtra("transfer_amount", amount);
        
        // Check for card insertion before PIN verification
        checkCardAndProceed(formattedAmount, selectedBank, accountNumber);
    }
    
    private void checkCardAndProceed(String formattedAmount, String selectedBank, String accountNumber) {
        // Store transfer details
        Intent swipeIntent = new Intent(this, BasSwipeCardActivity.class);
        swipeIntent.putExtra("amount", formattedAmount);
        swipeIntent.putExtra("bank", selectedBank);
        swipeIntent.putExtra("account", accountNumber);
        startActivityForResult(swipeIntent, 1001);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Card was successfully read
            String cardNumber = data.getStringExtra("card_number");
            String cardHolder = data.getStringExtra("card_holder");
            
            // Get transfer details from extras
            String formattedAmount = getIntent().getStringExtra("amount");
            String selectedBank = getIntent().getStringExtra("bank");
            String accountNumber = getIntent().getStringExtra("account");
            
            // Now show PIN dialog
            showModernPinDialog(formattedAmount, selectedBank, accountNumber);
        }
    }
    
    private void processTransfer(String accountNumber, long amount, byte[] pinBlock) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.transaction_processing));
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Retrieve selected bank
        String selectedBank = autoCompleteBank.getText().toString().trim();
        
        // Generate ARQC and process transfer
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // Generate ARQC and TLV data
                    EMVUtil emvUtil = new EMVUtil();
                    Map<String, String> tlvData = emvUtil.generateARQC(amount, pinBlock);
                    
                    // Build EMV data field for authorization
                    String emvDataField = emvUtil.buildEMVDataField(tlvData);
                    
                    Log.d(TAG, "Generated ARQC: " + tlvData.get(EMVUtil.TAG_CRYPTOGRAM));
                    Log.d(TAG, "EMV Data Field: " + emvDataField);
                    
                    // In real implementation, send authorization request to host
                    // For demo, just show success
                    
                    progressDialog.dismiss();
                    
                    // Format amount
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    String formattedAmount = formatter.format(amount);
                    
                    // Generate transaction ID
                    String transactionId = "TRF" + System.currentTimeMillis();
                    
                    // Add approval code from ARQC
                    String approvalCode = tlvData.get(EMVUtil.TAG_CRYPTOGRAM);
                    if (approvalCode != null && approvalCode.length() > 6) {
                        approvalCode = approvalCode.substring(0, 6);
                    }
                    
                    // Navigate to success page
                    TransactionSuccessActivity.start(TransferActivity.this,
                        "transfer",
                        formattedAmount,
                        selectedBank + " - " + accountNumber,
                        transactionId,
                        "Approval: " + approvalCode);
                    
                    // Close card reader - disabled
                    /*
                    ICardReader cardReader = SDKManager.getInstance().getCardReader();
                    if (cardReader != null) {
                        cardReader.close();
                    }
                    */
                    
                    // Close this activity
                    finish();
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error processing transfer", e);
                    progressDialog.dismiss();
                    Toast.makeText(TransferActivity.this, getString(R.string.transfer_failed) + ": " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                }
            }
        }, 2000);
    }
    
    private void showModernPinDialog(String formattedAmount, String selectedBank, String accountNumber) {
        // Create modern PIN dialog
        Dialog pinDialog = new Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pinDialog.setContentView(R.layout.dialog_pin_input);
        pinDialog.setCancelable(false);
        
        // Make dialog width responsive
        if (pinDialog.getWindow() != null) {
            pinDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            pinDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        
        // Get views
        TextView tvMessage = pinDialog.findViewById(R.id.tv_pin_message);
        EditText etPinInput = pinDialog.findViewById(R.id.et_pin_input);
        TextView tvError = pinDialog.findViewById(R.id.tv_error);
        
        // PIN dots
        View dot1 = pinDialog.findViewById(R.id.dot1);
        View dot2 = pinDialog.findViewById(R.id.dot2);
        View dot3 = pinDialog.findViewById(R.id.dot3);
        View dot4 = pinDialog.findViewById(R.id.dot4);
        View dot5 = pinDialog.findViewById(R.id.dot5);
        View dot6 = pinDialog.findViewById(R.id.dot6);
        View[] dots = {dot1, dot2, dot3, dot4, dot5, dot6};
        
        // Number pad buttons
        Button btn0 = pinDialog.findViewById(R.id.btn_0);
        Button btn1 = pinDialog.findViewById(R.id.btn_1);
        Button btn2 = pinDialog.findViewById(R.id.btn_2);
        Button btn3 = pinDialog.findViewById(R.id.btn_3);
        Button btn4 = pinDialog.findViewById(R.id.btn_4);
        Button btn5 = pinDialog.findViewById(R.id.btn_5);
        Button btn6 = pinDialog.findViewById(R.id.btn_6);
        Button btn7 = pinDialog.findViewById(R.id.btn_7);
        Button btn8 = pinDialog.findViewById(R.id.btn_8);
        Button btn9 = pinDialog.findViewById(R.id.btn_9);
        Button btnCancel = pinDialog.findViewById(R.id.btn_cancel);
        Button btnOk = pinDialog.findViewById(R.id.btn_ok);
        
        // Set message
        tvMessage.setText(getString(R.string.confirm_transfer) + " " + formattedAmount + " ke " + selectedBank + " - " + accountNumber);
        
        // Track PIN input
        StringBuilder pinBuilder = new StringBuilder();
        
        // Update PIN dots display
        Runnable updatePinDisplay = () -> {
            int pinLength = pinBuilder.length();
            for (int i = 0; i < dots.length; i++) {
                if (i < pinLength) {
                    dots[i].setBackgroundResource(R.drawable.pin_dot_filled);
                } else {
                    dots[i].setBackgroundResource(R.drawable.pin_dot_empty);
                }
            }
        };
        
        // Number button click listener
        View.OnClickListener numberClickListener = v -> {
            if (pinBuilder.length() < 6) {
                Button btn = (Button) v;
                pinBuilder.append(btn.getText());
                updatePinDisplay.run();
                
                // Clear any previous error
                tvError.setVisibility(View.GONE);
            }
        };
        
        // Set number button listeners
        btn0.setOnClickListener(numberClickListener);
        btn1.setOnClickListener(numberClickListener);
        btn2.setOnClickListener(numberClickListener);
        btn3.setOnClickListener(numberClickListener);
        btn4.setOnClickListener(numberClickListener);
        btn5.setOnClickListener(numberClickListener);
        btn6.setOnClickListener(numberClickListener);
        btn7.setOnClickListener(numberClickListener);
        btn8.setOnClickListener(numberClickListener);
        btn9.setOnClickListener(numberClickListener);
        
        // Cancel/Delete button
        btnCancel.setOnClickListener(v -> {
            if (pinBuilder.length() > 0) {
                pinBuilder.deleteCharAt(pinBuilder.length() - 1);
                updatePinDisplay.run();
            }
        });
        
        // OK button
        btnOk.setOnClickListener(v -> {
            if (pinBuilder.length() >= 4) {
                String pin = pinBuilder.toString();
                pinDialog.dismiss();
                
                // Convert PIN to simple PIN block (for demo)
                byte[] pinBlock = pin.getBytes();
                
                // Retrieve stored transfer details
                String storedAccountNumber = getIntent().getStringExtra("transfer_account");
                long amount = getIntent().getLongExtra("transfer_amount", 0);
                
                processTransfer(storedAccountNumber, amount, pinBlock);
            } else {
                tvError.setText(getString(R.string.create_pin_desc));
                tvError.setVisibility(View.VISIBLE);
            }
        });
        
        pinDialog.show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}