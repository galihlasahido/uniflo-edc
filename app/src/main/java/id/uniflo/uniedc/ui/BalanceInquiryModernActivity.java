package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.app.ProgressDialog;

import id.uniflo.uniedc.R;
// SDK imports removed for simplicity

public class BalanceInquiryModernActivity extends Activity {
    
    private ImageView backButton;
    private TextView tvBalance;
    private LinearLayout pinSection;
    private LinearLayout transactionsSection;
    private LinearLayout transactionsList;
    
    private EditText pin1, pin2, pin3, pin4, pin5, pin6;
    private Button btnCheckBalance;
    
    // Request codes
    private static final int REQUEST_CARD_READING = 1001;
    private static final int REQUEST_PIN_ENTRY = 1002;
    
    // Card data from reader
    private String cardNumber = "";
    private int cardType = 0;
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_inquiry_modern);
        
        initViews();
        setupListeners();
        launchCardReader();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        tvBalance = findViewById(R.id.tv_balance);
        pinSection = findViewById(R.id.pin_section);
        transactionsSection = findViewById(R.id.transactions_section);
        transactionsList = findViewById(R.id.transactions_list);
        
        pin1 = findViewById(R.id.pin_1);
        pin2 = findViewById(R.id.pin_2);
        pin3 = findViewById(R.id.pin_3);
        pin4 = findViewById(R.id.pin_4);
        pin5 = findViewById(R.id.pin_5);
        pin6 = findViewById(R.id.pin_6);
        
        btnCheckBalance = findViewById(R.id.btn_check_balance);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        setupPinInput();
        
        btnCheckBalance.setOnClickListener(v -> checkBalance());
    }
    
    private void setupPinInput() {
        pin1.addTextChangedListener(new PinTextWatcher(pin1, pin2));
        pin2.addTextChangedListener(new PinTextWatcher(pin2, pin3));
        pin3.addTextChangedListener(new PinTextWatcher(pin3, pin4));
        pin4.addTextChangedListener(new PinTextWatcher(pin4, pin5));
        pin5.addTextChangedListener(new PinTextWatcher(pin5, pin6));
        pin6.addTextChangedListener(new PinTextWatcher(pin6, null));
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
                    // Launch PIN entry
                    launchPinEntry();
                } else {
                    Toast.makeText(this, "Gagal membaca data kartu", Toast.LENGTH_LONG).show();
                    // Wait 2 seconds before closing to let user read the message
                    handler.postDelayed(() -> {
                        finish();
                    }, 2000);
                }
            } else {
                // Card reading failed or cancelled
                Toast.makeText(this, "Pembacaan kartu dibatalkan", Toast.LENGTH_LONG).show();
                // Wait 2 seconds before closing to let user read the message
                handler.postDelayed(() -> {
                    finish();
                }, 2000);
            }
        } else if (requestCode == REQUEST_PIN_ENTRY) {
            // PIN entry is now handled directly in SalesPinActivity for balance inquiry
            // This code is no longer used for balance inquiry flow
            Log.d("BalanceInquiry", "PIN entry completed, activity finishing");
            finish();
        }
    }
    
    private void launchPinEntry() {
        Intent intent = new Intent(this, SalesPinActivity.class);
        intent.putExtra(SalesPinActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(SalesPinActivity.EXTRA_TRANSACTION_TYPE, "balance_inquiry");
        intent.putExtra(SalesPinActivity.EXTRA_PIN_TITLE, "Cek Saldo");
        intent.putExtra(SalesPinActivity.EXTRA_PIN_SUBTITLE, "Masukkan PIN Kartu");
        startActivityForResult(intent, REQUEST_PIN_ENTRY);
    }
    
    private void showBalanceInquiryInterface() {
        // Show card read success message
        Toast.makeText(this, "Kartu dibaca: " + maskCardNumber(cardNumber), Toast.LENGTH_SHORT).show();
        
        // Hide PIN section since we're using SalesPinActivity
        pinSection.setVisibility(View.GONE);
        
        // Show balance and transactions directly after PIN verification
        tvBalance.setText("Rp 2,500,000");
        showTransactions();
    }
    
    private void processBalanceInquiry(String pin) {
        // This method is no longer used - processing moved to SalesPinActivity
        Log.d("BalanceInquiry", "processBalanceInquiry called but should not be used");
    }
    
    private void checkBalance() {
        // This method is kept for compatibility but not used anymore
        // PIN entry is handled by SalesPinActivity
    }
    
    private void showTransactions() {
        transactionsSection.setVisibility(View.VISIBLE);
        transactionsList.removeAllViews();
        
        // Add sample transactions
        addTransaction("Transfer masuk", "+Rp 1,000,000", "10 Jan 2025");
        addTransaction("Pembayaran", "-Rp 250,000", "9 Jan 2025");
        addTransaction("Top up", "+Rp 500,000", "8 Jan 2025");
        addTransaction("Transfer keluar", "-Rp 100,000", "7 Jan 2025");
    }
    
    private void addTransaction(String description, String amount, String date) {
        View transactionView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
        
        TextView text1 = transactionView.findViewById(android.R.id.text1);
        TextView text2 = transactionView.findViewById(android.R.id.text2);
        
        text1.setText(description + " - " + amount);
        text2.setText(date);
        
        if (amount.startsWith("+")) {
            text1.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            text1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        transactionsList.addView(transactionView);
        
        // Add divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(0xFFE0E0E0);
        transactionsList.addView(divider);
    }
    
    private String parseCardNumber(String cardData) {
        // Extract card number from track data
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
    
    private class PinTextWatcher implements TextWatcher {
        private EditText currentBox;
        private EditText nextBox;
        
        public PinTextWatcher(EditText currentBox, EditText nextBox) {
            this.currentBox = currentBox;
            this.nextBox = nextBox;
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextBox != null) {
                nextBox.requestFocus();
            }
        }
    }
    
}