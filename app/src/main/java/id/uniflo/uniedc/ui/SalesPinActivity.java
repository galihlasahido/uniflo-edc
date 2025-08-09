package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
        // Update card info
        if (cardNumber != null && !cardNumber.isEmpty()) {
            cardInfoText.setText("Kartu: " + cardNumber);
        } else {
            cardInfoText.setText("Kartu ICC terdeteksi");
        }
        
        // Update amount
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
        String formattedAmount = formatter.format(saleAmount).replace("Rp", "Rp ");
        amountText.setText(formattedAmount);
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
        if (currentPin.length() >= 4) {
            // PIN entered successfully, return to sales activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("pin_entered", currentPin.toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}