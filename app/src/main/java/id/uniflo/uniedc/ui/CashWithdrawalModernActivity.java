package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import id.uniflo.uniedc.R;
// SDK imports removed for simplicity

public class CashWithdrawalModernActivity extends Activity {
    
    private ImageView backButton;
    private TextView tvAmount;
    private TextView tvCardStatus;
    private EditText etCustomAmount;
    
    private Button btn50k, btn100k, btn200k, btn300k, btn500k, btn1m;
    private Button btnProcess;
    
    // PIN input removed - using SalesPinActivity instead
    
    private int selectedAmount = 0;
    
    // Request codes
    private static final int REQUEST_CARD_READING = 1001;
    private static final int REQUEST_PIN_ENTRY = 1002;
    
    // Card data from reader
    private String cardNumber = "";
    private int cardType = 0;
    private boolean cardDetected = false;
    private String enteredPin = "";
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_withdrawal_modern);
        
        initViews();
        setupListeners();
        launchCardReader();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        tvAmount = findViewById(R.id.tv_amount);
        tvCardStatus = findViewById(R.id.tv_card_status);
        etCustomAmount = findViewById(R.id.et_custom_amount);
        
        btn50k = findViewById(R.id.btn_50k);
        btn100k = findViewById(R.id.btn_100k);
        btn200k = findViewById(R.id.btn_200k);
        btn300k = findViewById(R.id.btn_300k);
        btn500k = findViewById(R.id.btn_500k);
        btn1m = findViewById(R.id.btn_1m);
        
        btnProcess = findViewById(R.id.btn_process);
        
        // PIN input elements removed - using SalesPinActivity instead
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        // Quick amount buttons
        btn50k.setOnClickListener(v -> selectAmount(50000));
        btn100k.setOnClickListener(v -> selectAmount(100000));
        btn200k.setOnClickListener(v -> selectAmount(200000));
        btn300k.setOnClickListener(v -> selectAmount(300000));
        btn500k.setOnClickListener(v -> selectAmount(500000));
        btn1m.setOnClickListener(v -> selectAmount(1000000));
        
        // Custom amount input
        etCustomAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String text = s.toString().replaceAll("[^0-9]", "");
                    if (!text.isEmpty()) {
                        selectedAmount = Integer.parseInt(text);
                        updateAmountDisplay();
                        resetQuickButtons();
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
        
        // PIN input removed - using SalesPinActivity instead
        
        // Process button
        btnProcess.setOnClickListener(v -> processWithdrawal());
    }
    
    // PIN input setup removed - using SalesPinActivity instead
    
    private void selectAmount(int amount) {
        selectedAmount = amount;
        updateAmountDisplay();
        etCustomAmount.setText("");
        
        // Update button states
        resetQuickButtons();
        
        // Highlight selected button
        Button selectedButton = getButtonForAmount(amount);
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.selected_card_background);
            selectedButton.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
    
    private void resetQuickButtons() {
        Button[] buttons = {btn50k, btn100k, btn200k, btn300k, btn500k, btn1m};
        for (Button button : buttons) {
            button.setBackgroundResource(R.drawable.button_outline_green);
            button.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }
    
    private Button getButtonForAmount(int amount) {
        switch (amount) {
            case 50000: return btn50k;
            case 100000: return btn100k;
            case 200000: return btn200k;
            case 300000: return btn300k;
            case 500000: return btn500k;
            case 1000000: return btn1m;
            default: return null;
        }
    }
    
    private void updateAmountDisplay() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(selectedAmount).replace("Rp", "Rp ");
        tvAmount.setText(formattedAmount);
    }
    
    private void launchCardReader() {
        Intent intent = new Intent(this, CardReaderActivity.class);
        startActivityForResult(intent, REQUEST_CARD_READING);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CARD_READING) {
            if (resultCode == RESULT_OK) {
                // Card reading successful, get card data
                if (data != null) {
                    cardNumber = data.getStringExtra(CardReaderActivity.EXTRA_CARD_NUMBER);
                    cardDetected = true;
                    // Show cash withdrawal interface
                    showCashWithdrawalInterface();
                } else {
                    Toast.makeText(this, "Gagal membaca data kartu", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Card reading failed or cancelled
                Toast.makeText(this, "Pembacaan kartu dibatalkan", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == REQUEST_PIN_ENTRY) {
            if (resultCode == RESULT_OK) {
                // PIN entered successfully
                if (data != null) {
                    enteredPin = data.getStringExtra("pin_entered");
                    // Process withdrawal with PIN
                    executeWithdrawal();
                } else {
                    Toast.makeText(this, "PIN tidak valid", Toast.LENGTH_SHORT).show();
                }
            } else {
                // PIN entry cancelled
                Toast.makeText(this, "PIN dibatalkan", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void showCashWithdrawalInterface() {
        tvCardStatus.setText("Kartu: " + maskCardNumber(cardNumber));
        tvCardStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        
        // PIN section already removed from layout
    }
    
    private void processWithdrawal() {
        // Validate amount
        if (selectedAmount == 0) {
            Toast.makeText(this, "Pilih jumlah penarikan", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedAmount < 50000) {
            Toast.makeText(this, "Jumlah minimum Rp 50.000", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedAmount > 5000000) {
            Toast.makeText(this, "Jumlah maksimum Rp 5.000.000", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate card
        if (!cardDetected) {
            Toast.makeText(this, "Masukkan atau tap kartu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Launch PIN entry using SalesPinActivity
        Intent intent = new Intent(this, SalesPinActivity.class);
        intent.putExtra(SalesPinActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(SalesPinActivity.EXTRA_AMOUNT, (long)selectedAmount);
        intent.putExtra(SalesPinActivity.EXTRA_TRANSACTION_TYPE, "cash_withdrawal");
        startActivityForResult(intent, REQUEST_PIN_ENTRY);
    }
    
    private void executeWithdrawal() {
        // Show processing dialog
        AlertDialog processingDialog = new AlertDialog.Builder(this)
            .setTitle("Memproses")
            .setMessage("Sedang memproses penarikan tunai...")
            .setCancelable(false)
            .create();
        processingDialog.show();
        
        // Simulate processing
        handler.postDelayed(() -> {
            processingDialog.dismiss();
            showWithdrawalResult();
        }, 3000);
    }
    
    private void showWithdrawalResult() {
        // Navigate to withdrawal result page
        Intent intent = new Intent(this, WithdrawalResultActivity.class);
        intent.putExtra(WithdrawalResultActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(WithdrawalResultActivity.EXTRA_AMOUNT, (long)selectedAmount);
        intent.putExtra(WithdrawalResultActivity.EXTRA_ACCOUNT_TYPE, "Tabungan");
        startActivity(intent);
        finish();
    }
    
    private String parseCardNumber(String cardData) {
        if (cardData != null && cardData.length() >= 16) {
            return cardData.substring(0, 16);
        }
        return "0000000000000000";
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() >= 16) {
            return "**** **** **** " + cardNumber.substring(12);
        }
        return cardNumber;
    }
    
    // PinTextWatcher class removed - using SalesPinActivity instead
    
}