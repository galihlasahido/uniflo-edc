package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import id.uniflo.uniedc.R;

public class ChangePinModernActivity extends Activity {
    
    private static final String TAG = "ChangePinModern";
    private static final int REQUEST_CARD_READ = 1001;
    
    private ImageView backButton;
    private LinearLayout cardSection, oldPinSection, newPinSection, confirmSection, keypadSection;
    private TextView statusMessage, cardStatusText;
    private ImageView cardAnimationView;
    private View stepIndicator1, stepIndicator2, stepIndicator3;
    
    // Card data
    private String cardAtr = "";
    
    // Bullet views for old PIN
    private View oldBullet1, oldBullet2, oldBullet3, oldBullet4, oldBullet5, oldBullet6;
    // Bullet views for new PIN
    private View newBullet1, newBullet2, newBullet3, newBullet4, newBullet5, newBullet6;
    // Bullet views for confirm PIN
    private View confirmBullet1, confirmBullet2, confirmBullet3, confirmBullet4, confirmBullet5, confirmBullet6;
    
    // Keypad buttons
    private Button key0, key1, key2, key3, key4, key5, key6, key7, key8, key9, keyClear;
    
    // PIN data
    private StringBuilder oldPinBuilder = new StringBuilder();
    private StringBuilder newPinBuilder = new StringBuilder();
    private StringBuilder confirmPinBuilder = new StringBuilder();
    
    // Current step: 0=old PIN, 1=new PIN, 2=confirm PIN
    private int currentStep = 0;
    
    // Card data from reader
    private String cardNumber = "";
    private boolean cardValidated = false;
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin_modern);
        
        initViews();
        setupListeners();
        updateBullets();
        updateStepIndicators();
        
        // Start with card reading
        startCardReading();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        
        cardSection = findViewById(R.id.card_section);
        cardStatusText = findViewById(R.id.tv_card_status);
        cardAnimationView = findViewById(R.id.iv_card_animation);
        
        oldPinSection = findViewById(R.id.old_pin_section);
        newPinSection = findViewById(R.id.new_pin_section);
        confirmSection = findViewById(R.id.confirm_section);
        keypadSection = findViewById(R.id.keypad_section);
        
        statusMessage = findViewById(R.id.status_message);
        stepIndicator1 = findViewById(R.id.step_indicator_1);
        stepIndicator2 = findViewById(R.id.step_indicator_2);
        stepIndicator3 = findViewById(R.id.step_indicator_3);
        
        // Old PIN bullets
        oldBullet1 = findViewById(R.id.old_bullet_1);
        oldBullet2 = findViewById(R.id.old_bullet_2);
        oldBullet3 = findViewById(R.id.old_bullet_3);
        oldBullet4 = findViewById(R.id.old_bullet_4);
        oldBullet5 = findViewById(R.id.old_bullet_5);
        oldBullet6 = findViewById(R.id.old_bullet_6);
        
        // New PIN bullets
        newBullet1 = findViewById(R.id.new_bullet_1);
        newBullet2 = findViewById(R.id.new_bullet_2);
        newBullet3 = findViewById(R.id.new_bullet_3);
        newBullet4 = findViewById(R.id.new_bullet_4);
        newBullet5 = findViewById(R.id.new_bullet_5);
        newBullet6 = findViewById(R.id.new_bullet_6);
        
        // Confirm PIN bullets
        confirmBullet1 = findViewById(R.id.confirm_bullet_1);
        confirmBullet2 = findViewById(R.id.confirm_bullet_2);
        confirmBullet3 = findViewById(R.id.confirm_bullet_3);
        confirmBullet4 = findViewById(R.id.confirm_bullet_4);
        confirmBullet5 = findViewById(R.id.confirm_bullet_5);
        confirmBullet6 = findViewById(R.id.confirm_bullet_6);
        
        // Keypad
        key0 = findViewById(R.id.key_0);
        key1 = findViewById(R.id.key_1);
        key2 = findViewById(R.id.key_2);
        key3 = findViewById(R.id.key_3);
        key4 = findViewById(R.id.key_4);
        key5 = findViewById(R.id.key_5);
        key6 = findViewById(R.id.key_6);
        key7 = findViewById(R.id.key_7);
        key8 = findViewById(R.id.key_8);
        key9 = findViewById(R.id.key_9);
        keyClear = findViewById(R.id.key_clear);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        // Keypad listeners
        key0.setOnClickListener(v -> addDigit("0"));
        key1.setOnClickListener(v -> addDigit("1"));
        key2.setOnClickListener(v -> addDigit("2"));
        key3.setOnClickListener(v -> addDigit("3"));
        key4.setOnClickListener(v -> addDigit("4"));
        key5.setOnClickListener(v -> addDigit("5"));
        key6.setOnClickListener(v -> addDigit("6"));
        key7.setOnClickListener(v -> addDigit("7"));
        key8.setOnClickListener(v -> addDigit("8"));
        key9.setOnClickListener(v -> addDigit("9"));
        keyClear.setOnClickListener(v -> clearLastDigit());
    }
    
    private void startCardReading() {
        Intent cardReaderIntent = new Intent(this, CardReaderActivity.class);
        cardReaderIntent.putExtra(CardReaderActivity.EXTRA_TITLE, "Ubah PIN");
        cardReaderIntent.putExtra(CardReaderActivity.EXTRA_SUBTITLE, "Masukkan kartu untuk mengubah PIN");
        startActivityForResult(cardReaderIntent, REQUEST_CARD_READ);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CARD_READ) {
            if (resultCode == RESULT_OK && data != null) {
                // Card reading successful, extract card data
                cardAtr = data.getStringExtra(CardReaderActivity.EXTRA_CARD_ATR);
                cardNumber = data.getStringExtra(CardReaderActivity.EXTRA_CARD_NUMBER);
                cardValidated = data.getBooleanExtra(CardReaderActivity.EXTRA_CARD_VALIDATED, false);
                
                Log.d(TAG, "Card reading successful - ATR: " + cardAtr + ", Number: " + cardNumber);
                
                // Show PIN change interface
                showPinChangeInterface();
            } else {
                // Card reading failed or canceled
                Log.d(TAG, "Card reading canceled or failed");
                finish();
            }
        }
    }
    
    private void showPinChangeInterface() {
        // Hide card section and show PIN change sections
        cardSection.setVisibility(View.GONE);
        oldPinSection.setVisibility(View.VISIBLE);
        keypadSection.setVisibility(View.VISIBLE);
        
        // Update card status
        if (cardNumber != null && !cardNumber.isEmpty()) {
            // Card number is already masked by CardReaderActivity
            cardStatusText.setText("Kartu valid - " + cardNumber);
            cardStatusText.setTextColor(0xFF00C853); // Green
        }
        
        // Update status
        updateStepIndicators();
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }
        
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < cardNumber.length(); i++) {
            if (i < 4 || i >= cardNumber.length() - 4) {
                masked.append(cardNumber.charAt(i));
            } else {
                masked.append("*");
            }
        }
        return masked.toString();
    }
    
    private void addDigit(String digit) {
        
        switch (currentStep) {
            case 0: // Old PIN
                if (oldPinBuilder.length() < 6) {
                    oldPinBuilder.append(digit);
                    updateBullets();
                    
                    if (oldPinBuilder.length() == 6) {
                        handler.postDelayed(this::verifyOldPin, 300);
                    }
                }
                break;
                
            case 1: // New PIN
                if (newPinBuilder.length() < 6) {
                    newPinBuilder.append(digit);
                    updateBullets();
                    
                    if (newPinBuilder.length() == 6) {
                        handler.postDelayed(this::showConfirmSection, 300);
                    }
                }
                break;
                
            case 2: // Confirm PIN
                if (confirmPinBuilder.length() < 6) {
                    confirmPinBuilder.append(digit);
                    updateBullets();
                    
                    if (confirmPinBuilder.length() == 6) {
                        handler.postDelayed(this::attemptPinChange, 300);
                    }
                }
                break;
        }
    }
    
    private void clearLastDigit() {
        
        switch (currentStep) {
            case 0: // Old PIN
                if (oldPinBuilder.length() > 0) {
                    oldPinBuilder.deleteCharAt(oldPinBuilder.length() - 1);
                    updateBullets();
                }
                break;
                
            case 1: // New PIN
                if (newPinBuilder.length() > 0) {
                    newPinBuilder.deleteCharAt(newPinBuilder.length() - 1);
                    updateBullets();
                }
                break;
                
            case 2: // Confirm PIN
                if (confirmPinBuilder.length() > 0) {
                    confirmPinBuilder.deleteCharAt(confirmPinBuilder.length() - 1);
                    updateBullets();
                }
                break;
        }
    }
    
    private void verifyOldPin() {
        // Get saved PIN from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("pin_prefs", MODE_PRIVATE);
        String savedPin = prefs.getString("user_pin", "123456"); // Default PIN
        
        if (oldPinBuilder.toString().equals(savedPin)) {
            // Correct old PIN, proceed to new PIN step
            currentStep = 1;
            updateStepIndicators();
            
            // Hide old PIN section and show new PIN section
            oldPinSection.setVisibility(View.GONE);
            newPinSection.setVisibility(View.VISIBLE);
            newPinSection.requestFocus();
        } else {
            // Wrong old PIN
            Toast.makeText(this, "PIN lama salah", Toast.LENGTH_SHORT).show();
            oldPinBuilder.setLength(0);
            updateBullets();
            // Keep old PIN section visible for retry
        }
    }
    
    private void showConfirmSection() {
        currentStep = 2;
        updateStepIndicators();
        
        // Hide new PIN section and show confirm section
        newPinSection.setVisibility(View.GONE);
        confirmSection.setVisibility(View.VISIBLE);
        confirmSection.requestFocus();
    }
    
    private void updateBullets() {
        // Update old PIN bullets
        View[] oldBullets = {oldBullet1, oldBullet2, oldBullet3, oldBullet4, oldBullet5, oldBullet6};
        for (int i = 0; i < 6; i++) {
            if (i < oldPinBuilder.length()) {
                oldBullets[i].setBackgroundResource(R.drawable.pin_bullet_filled);
            } else {
                oldBullets[i].setBackgroundResource(R.drawable.pin_bullet_empty);
            }
        }
        
        // Update new PIN bullets
        View[] newBullets = {newBullet1, newBullet2, newBullet3, newBullet4, newBullet5, newBullet6};
        for (int i = 0; i < 6; i++) {
            if (i < newPinBuilder.length()) {
                newBullets[i].setBackgroundResource(R.drawable.pin_bullet_filled);
            } else {
                newBullets[i].setBackgroundResource(R.drawable.pin_bullet_empty);
            }
        }
        
        // Update confirm PIN bullets
        View[] confirmBullets = {confirmBullet1, confirmBullet2, confirmBullet3, confirmBullet4, confirmBullet5, confirmBullet6};
        for (int i = 0; i < 6; i++) {
            if (i < confirmPinBuilder.length()) {
                confirmBullets[i].setBackgroundResource(R.drawable.pin_bullet_filled);
            } else {
                confirmBullets[i].setBackgroundResource(R.drawable.pin_bullet_empty);
            }
        }
    }
    
    private void attemptPinChange() {
        String newPin = newPinBuilder.toString();
        String confirmPin = confirmPinBuilder.toString();
        
        // Validate PIN length
        if (newPin.length() != 6 || confirmPin.length() != 6) {
            Toast.makeText(this, "Masukkan PIN 6 digit", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if PINs match
        if (!newPin.equals(confirmPin)) {
            Toast.makeText(this, "PIN baru tidak cocok", Toast.LENGTH_SHORT).show();
            confirmPinBuilder.setLength(0);
            updateBullets();
            // Keep confirm section visible for retry
            return;
        }
        
        // Update step indicator to show completion
        currentStep = 3;
        updateStepIndicators();
        
        // Hide confirm section to show completion
        confirmSection.setVisibility(View.GONE);
        
        // Show processing and save PIN
        handler.postDelayed(() -> {
            // Save new PIN
            SharedPreferences prefs = getSharedPreferences("pin_prefs", MODE_PRIVATE);
            prefs.edit().putString("user_pin", newPin).apply();
            
            new AlertDialog.Builder(this)
                .setTitle("Berhasil")
                .setMessage("PIN berhasil diubah!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
        }, 500);
    }
    
    private void updateStepIndicators() {
        switch (currentStep) {
            case 0:
                statusMessage.setText("Langkah 1 dari 3 - Masukkan PIN Lama");
                stepIndicator1.setBackgroundResource(R.drawable.pin_bullet_filled);
                stepIndicator2.setBackgroundResource(R.drawable.pin_bullet_empty);
                stepIndicator3.setBackgroundResource(R.drawable.pin_bullet_empty);
                break;
            case 1:
                statusMessage.setText("Langkah 2 dari 3 - Masukkan PIN Baru");
                stepIndicator1.setBackgroundResource(R.drawable.pin_bullet_filled);
                stepIndicator2.setBackgroundResource(R.drawable.pin_bullet_filled);
                stepIndicator3.setBackgroundResource(R.drawable.pin_bullet_empty);
                break;
            case 2:
                statusMessage.setText("Langkah 3 dari 3 - Konfirmasi PIN Baru");
                stepIndicator1.setBackgroundResource(R.drawable.pin_bullet_filled);
                stepIndicator2.setBackgroundResource(R.drawable.pin_bullet_filled);
                stepIndicator3.setBackgroundResource(R.drawable.pin_bullet_filled);
                break;
            case 3:
                statusMessage.setText("Selesai - PIN Berhasil Diubah");
                stepIndicator1.setBackgroundResource(R.drawable.pin_bullet_filled);
                stepIndicator2.setBackgroundResource(R.drawable.pin_bullet_filled);
                stepIndicator3.setBackgroundResource(R.drawable.pin_bullet_filled);
                break;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No SDK cleanup needed - handled by CardReaderActivity
    }
}