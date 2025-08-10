package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id.uniflo.uniedc.R;

public class SalesPinActivity extends Activity {
    
    public static final String EXTRA_CARD_NUMBER = "extra_card_number";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_TRANSACTION_TYPE = "extra_transaction_type";
    public static final String EXTRA_PIN_TITLE = "extra_pin_title";
    public static final String EXTRA_PIN_SUBTITLE = "extra_pin_subtitle";
    
    // UI elements
    private ImageView backButton;
    private TextView titleText;
    private TextView cardInfoText;
    private TextView amountText;
    
    // PIN bullets
    private View[] pinBullets;
    private LinearLayout pinSection;
    
    // Keypad
    private LinearLayout keypadSection;
    private Button[] keyButtons;
    private Button keyClear;
    
    // Data
    private String cardNumber;
    private long saleAmount;
    private StringBuilder currentPin = new StringBuilder();
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_pin);
        
        // Get intent data
        cardNumber = getIntent().getStringExtra(EXTRA_CARD_NUMBER);
        saleAmount = getIntent().getLongExtra(EXTRA_AMOUNT, 0);
        
        initViews();
        setupKeypad();
        updateUI();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        cardInfoText = findViewById(R.id.card_info_text);
        amountText = findViewById(R.id.amount_text);
        
        pinSection = findViewById(R.id.pin_section);
        keypadSection = findViewById(R.id.keypad_section);
        
        // PIN bullets
        pinBullets = new View[6];
        pinBullets[0] = findViewById(R.id.pin_bullet_1);
        pinBullets[1] = findViewById(R.id.pin_bullet_2);
        pinBullets[2] = findViewById(R.id.pin_bullet_3);
        pinBullets[3] = findViewById(R.id.pin_bullet_4);
        pinBullets[4] = findViewById(R.id.pin_bullet_5);
        pinBullets[5] = findViewById(R.id.pin_bullet_6);
        
        // Keypad buttons
        keyButtons = new Button[10];
        keyButtons[0] = findViewById(R.id.key_0);
        keyButtons[1] = findViewById(R.id.key_1);
        keyButtons[2] = findViewById(R.id.key_2);
        keyButtons[3] = findViewById(R.id.key_3);
        keyButtons[4] = findViewById(R.id.key_4);
        keyButtons[5] = findViewById(R.id.key_5);
        keyButtons[6] = findViewById(R.id.key_6);
        keyButtons[7] = findViewById(R.id.key_7);
        keyButtons[8] = findViewById(R.id.key_8);
        keyButtons[9] = findViewById(R.id.key_9);
        keyClear = findViewById(R.id.key_clear);
        
        // Back button
        backButton.setOnClickListener(v -> finish());
        
        // Show sections
        pinSection.setVisibility(View.VISIBLE);
        keypadSection.setVisibility(View.VISIBLE);
    }
    
    private void setupKeypad() {
        // Number buttons
        for (int i = 0; i < 10; i++) {
            final int number = i;
            keyButtons[i].setOnClickListener(v -> addDigit(String.valueOf(number)));
        }
        
        // Clear button
        keyClear.setOnClickListener(v -> clearLastDigit());
    }
    
    private void updateUI() {
        // Update title from intent extras or use defaults
        String pinTitle = getIntent().getStringExtra(EXTRA_PIN_TITLE);
        String pinSubtitle = getIntent().getStringExtra(EXTRA_PIN_SUBTITLE);
        String transactionType = getIntent().getStringExtra(EXTRA_TRANSACTION_TYPE);
        
        // Set header title based on transaction type or custom title
        if (titleText != null) {
            if (pinTitle != null && !pinTitle.isEmpty()) {
                titleText.setText(pinTitle);
            } else if ("balance_inquiry".equals(transactionType)) {
                titleText.setText("Cek Saldo");
            } else if ("withdrawal".equals(transactionType) || "cash_withdrawal".equals(transactionType)) {
                titleText.setText("Tarik Tunai");
            } else if ("transfer".equals(transactionType)) {
                titleText.setText("Transfer");
            } else if ("verify_pin".equals(transactionType)) {
                titleText.setText("Verifikasi PIN");
            } else {
                titleText.setText("PIN Pembayaran");
            }
        }
        
        // Set subtitle (the instruction text)
        TextView subtitleText = findViewById(R.id.pin_subtitle);
        if (subtitleText != null) {
            if (pinSubtitle != null && !pinSubtitle.isEmpty()) {
                subtitleText.setText(pinSubtitle);
            } else {
                subtitleText.setText("Masukkan PIN Kartu");
            }
        }
        
        // Hide the entire card info container for balance inquiry
        LinearLayout cardInfoContainer = findViewById(R.id.card_info_container);
        if ("balance_inquiry".equals(transactionType)) {
            // Hide the entire card and amount container for balance inquiry
            if (cardInfoContainer != null) {
                cardInfoContainer.setVisibility(View.GONE);
            }
        } else {
            // Show the container for other transactions
            if (cardInfoContainer != null) {
                cardInfoContainer.setVisibility(View.VISIBLE);
            }
            
            // Update card info
            if (cardNumber != null && !cardNumber.isEmpty()) {
                cardInfoText.setText("Kartu: " + cardNumber);
            } else {
                cardInfoText.setText("Kartu ICC terdeteksi");
            }
            
            // Update amount - hide for non-payment transactions
            if ("verify_pin".equals(transactionType)) {
                amountText.setVisibility(View.GONE);
            } else {
                amountText.setVisibility(View.VISIBLE);
                java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
                String formattedAmount = formatter.format(saleAmount).replace("Rp", "Rp ");
                amountText.setText(formattedAmount);
            }
        }
    }
    
    private void addDigit(String digit) {
        if (currentPin.length() < 6) {
            currentPin.append(digit);
            updatePinDisplay();
            
            // Auto-submit if 6 digits entered
            if (currentPin.length() == 6) {
                // Small delay before processing
                pinSection.postDelayed(this::processPinEntry, 300);
            }
        }
    }
    
    private void clearLastDigit() {
        if (currentPin.length() > 0) {
            currentPin.deleteCharAt(currentPin.length() - 1);
            updatePinDisplay();
        }
    }
    
    private void updatePinDisplay() {
        for (int i = 0; i < pinBullets.length; i++) {
            if (i < currentPin.length()) {
                pinBullets[i].setBackgroundResource(R.drawable.pin_bullet_filled);
            } else {
                pinBullets[i].setBackgroundResource(R.drawable.pin_bullet_empty);
            }
        }
    }
    
    private void processPinEntry() {
        if (currentPin.length() == 6) {
            String transactionType = getIntent().getStringExtra(EXTRA_TRANSACTION_TYPE);
            
            if ("balance_inquiry".equals(transactionType)) {
                // Show processing dialog for balance inquiry
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Memproses cek saldo...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                
                // Process balance inquiry and navigate directly to result
                handler.postDelayed(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    
                    // Navigate directly to BalanceResultActivity
                    if (cardNumber != null && !cardNumber.isEmpty()) {
                        long mockBalance = 5750000L; // Mock balance for testing
                        
                        Intent intent = new Intent(this, BalanceResultActivity.class);
                        intent.putExtra(BalanceResultActivity.EXTRA_CARD_NUMBER, cardNumber);
                        intent.putExtra(BalanceResultActivity.EXTRA_BALANCE, mockBalance);
                        intent.putExtra(BalanceResultActivity.EXTRA_ACCOUNT_TYPE, "Tabungan");
                        startActivity(intent);
                        finish();
                    } else {
                        android.widget.Toast.makeText(this, "Error: Invalid card data", android.widget.Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, 2000);
            } else {
                // For other transaction types, return to calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("pin_entered", currentPin.toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }
}