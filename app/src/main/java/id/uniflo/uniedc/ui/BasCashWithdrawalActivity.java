package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import id.uniflo.uniedc.R;
// Database imports removed - using in-memory storage
import id.uniflo.uniedc.util.PinEncryptionUtil;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.ICardReader;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BasCashWithdrawalActivity extends Activity {
    
    private ImageView backButton;
    private TextView titleText;
    private EditText amountInput;
    private TextView amountDisplay;
    private GridLayout quickAmountGrid;
    private View btnProceed;
    private TextView btnProceedText;
    private TextView errorText;
    
    private long withdrawalAmount = 0;
    private static final int PIN_VERIFICATION_REQUEST = 1001;
    
    private ProgressDialog progressDialog;
    // Database removed - using SharedPreferences
    private SDKManager sdkManager;
    private String currentCardNumber = "";
    
    // Quick amount options
    private final long[] quickAmounts = {
        50000, 100000, 200000, 
        300000, 500000, 1000000
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_cash_withdrawal);
        
        // Database initialization removed - using SharedPreferences
        
        // Initialize SDK Manager
        sdkManager = SDKManager.getInstance();
        
        initViews();
        setupListeners();
        setupQuickAmounts();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        titleText.setText(getString(R.string.cash_withdrawal_title));
        
        amountInput = findViewById(R.id.amount_input);
        amountDisplay = findViewById(R.id.amount_display);
        quickAmountGrid = findViewById(R.id.quick_amount_grid);
        
        btnProceed = findViewById(R.id.btn_proceed);
        btnProceedText = findViewById(R.id.btn_proceed_text);
        btnProceedText.setText(getString(R.string.next));
        
        errorText = findViewById(R.id.error_text);
        
        // Initially hide error and disable proceed button
        errorText.setVisibility(View.GONE);
        btnProceed.setEnabled(false);
        updateAmountDisplay();
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String cleanString = s.toString().replaceAll("[Rp,.]", "").trim();
                    if (!cleanString.isEmpty()) {
                        withdrawalAmount = Long.parseLong(cleanString);
                    } else {
                        withdrawalAmount = 0;
                    }
                    updateAmountDisplay();
                    validateAmount();
                } catch (NumberFormatException e) {
                    withdrawalAmount = 0;
                    updateAmountDisplay();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        btnProceed.setOnClickListener(v -> {
            if (validateAmount()) {
                proceedWithWithdrawal();
            }
        });
    }
    
    private void setupQuickAmounts() {
        for (long amount : quickAmounts) {
            View quickAmountView = getLayoutInflater().inflate(R.layout.quick_amount_item, quickAmountGrid, false);
            TextView amountText = quickAmountView.findViewById(R.id.quick_amount_text);
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String formattedAmount = formatter.format(amount).replace("Rp", "Rp ");
            amountText.setText(formattedAmount);
            
            quickAmountView.setOnClickListener(v -> {
                withdrawalAmount = amount;
                amountInput.setText(String.valueOf(amount));
                updateAmountDisplay();
                validateAmount();
            });
            
            quickAmountGrid.addView(quickAmountView);
        }
    }
    
    private void updateAmountDisplay() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(withdrawalAmount).replace("Rp", "Rp ");
        amountDisplay.setText(formattedAmount);
    }
    
    private boolean validateAmount() {
        errorText.setVisibility(View.GONE);
        
        if (withdrawalAmount <= 0) {
            btnProceed.setEnabled(false);
            return false;
        }
        
        if (withdrawalAmount < 50000) {
            showError(getString(R.string.min_amount));
            btnProceed.setEnabled(false);
            return false;
        }
        
        if (withdrawalAmount > 5000000) {
            showError(getString(R.string.max_amount));
            btnProceed.setEnabled(false);
            return false;
        }
        
        if (withdrawalAmount % 50000 != 0) {
            showError(getString(R.string.amount_multiple_50k));
            btnProceed.setEnabled(false);
            return false;
        }
        
        btnProceed.setEnabled(true);
        return true;
    }
    
    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
    
    private void proceedWithWithdrawal() {
        // First read card
        showProgressDialog(getString(R.string.insert_card));
        
        ICardReader cardReader = sdkManager.getCardReader();
        if (cardReader != null) {
            cardReader.open(ICardReader.CARD_TYPE_MAG | ICardReader.CARD_TYPE_IC | ICardReader.CARD_TYPE_NFC, 
                30, new ICardReader.ICardDetectListener() {
                @Override
                public void onCardDetected(int cardType) {
                    runOnUiThread(() -> {
                        hideProgressDialog();
                        currentCardNumber = "****-****-****-1234"; // Masked card number
                        // Then verify PIN
                        Intent pinIntent = new Intent(BasCashWithdrawalActivity.this, BasVerifyPinActivity.class);
                        startActivityForResult(pinIntent, PIN_VERIFICATION_REQUEST);
                    });
                }
                
                @Override
                public void onCardRemoved() {
                    runOnUiThread(() -> {
                        hideProgressDialog();
                        showError(getString(R.string.card_removed));
                    });
                }
                
                @Override
                public void onTimeout() {
                    runOnUiThread(() -> {
                        hideProgressDialog();
                        showError(getString(R.string.timeout_error));
                    });
                }
                
                @Override
                public void onError(int errorCode, String message) {
                    runOnUiThread(() -> {
                        hideProgressDialog();
                        showError(getString(R.string.card_read_failed) + ": " + message);
                    });
                }
            });
        } else {
            hideProgressDialog();
            showError(getString(R.string.card_reader_not_available));
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PIN_VERIFICATION_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("pin_verified", false)) {
                processWithdrawal();
            }
        }
    }
    
    private void processWithdrawal() {
        showProgressDialog(getString(R.string.transaction_processing));
        
        // Simulate withdrawal process
        new Handler().postDelayed(() -> {
            try {
                // Save transaction to SharedPreferences (for demo)
                String transactionId = "WD" + System.currentTimeMillis();
                getSharedPreferences("transactions", MODE_PRIVATE)
                    .edit()
                    .putString(transactionId, "WITHDRAWAL|" + withdrawalAmount + "|" + new Date().toString())
                    .apply();
                
                hideProgressDialog();
                
                // Show success message
                showSuccessDialog();
                
            } catch (Exception e) {
                hideProgressDialog();
                showError(getString(R.string.withdrawal_failed) + ": " + e.getMessage());
            }
        }, 3000);
    }
    
    private void showSuccessDialog() {
        // In a real app, you would show a proper success dialog
        Toast.makeText(this, getString(R.string.withdrawal_success) + "\n" + getString(R.string.amount) + ": " + 
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"))
                .format(withdrawalAmount).replace("Rp", "Rp "), 
            Toast.LENGTH_LONG).show();
        
        // Return to home after a delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, BasHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }, 2000);
    }
    
    private String generateSTAN() {
        return String.format("%06d", System.currentTimeMillis() % 1000000);
    }
    
    private String generateRRN() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }
    
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
}