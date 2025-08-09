package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import id.uniflo.uniedc.R;

public class CreatePinModernActivity extends Activity {
    
    private ImageView backButton;
    private LinearLayout confirmSection;
    
    // Bullet views for new PIN
    private View newBullet1, newBullet2, newBullet3, newBullet4, newBullet5, newBullet6;
    // Bullet views for confirm PIN
    private View confirmBullet1, confirmBullet2, confirmBullet3, confirmBullet4, confirmBullet5, confirmBullet6;
    
    // Hidden EditTexts
    private EditText newPin1, newPin2, newPin3, newPin4, newPin5, newPin6;
    
    // Keypad buttons
    private Button key0, key1, key2, key3, key4, key5, key6, key7, key8, key9, keyClear;
    private Button btnCreatePin;
    
    // PIN data
    private StringBuilder newPinBuilder = new StringBuilder();
    private StringBuilder confirmPinBuilder = new StringBuilder();
    private boolean isConfirmMode = false;
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin_modern);
        
        initViews();
        setupListeners();
        updateBullets();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        confirmSection = findViewById(R.id.confirm_section);
        
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
        
        // Hidden EditTexts
        newPin1 = findViewById(R.id.new_pin_1);
        newPin2 = findViewById(R.id.new_pin_2);
        newPin3 = findViewById(R.id.new_pin_3);
        newPin4 = findViewById(R.id.new_pin_4);
        newPin5 = findViewById(R.id.new_pin_5);
        newPin6 = findViewById(R.id.new_pin_6);
        
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
        
        btnCreatePin = findViewById(R.id.btn_create_pin);
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
        
        btnCreatePin.setOnClickListener(v -> createPin());
    }
    
    private void addDigit(String digit) {
        if (!isConfirmMode) {
            // Adding to new PIN
            if (newPinBuilder.length() < 6) {
                newPinBuilder.append(digit);
                updateBullets();
                
                // If new PIN is complete, show confirm section
                if (newPinBuilder.length() == 6) {
                    handler.postDelayed(() -> {
                        showConfirmSection();
                    }, 300);
                }
            }
        } else {
            // Adding to confirm PIN
            if (confirmPinBuilder.length() < 6) {
                confirmPinBuilder.append(digit);
                updateBullets();
                
                // If confirm PIN is complete, enable create button
                if (confirmPinBuilder.length() == 6) {
                    btnCreatePin.setEnabled(true);
                    btnCreatePin.setAlpha(1.0f);
                }
            }
        }
    }
    
    private void clearLastDigit() {
        if (!isConfirmMode) {
            // Clear from new PIN
            if (newPinBuilder.length() > 0) {
                newPinBuilder.deleteCharAt(newPinBuilder.length() - 1);
                updateBullets();
            }
        } else {
            // Clear from confirm PIN
            if (confirmPinBuilder.length() > 0) {
                confirmPinBuilder.deleteCharAt(confirmPinBuilder.length() - 1);
                updateBullets();
                
                // Disable create button if confirm PIN is incomplete
                if (confirmPinBuilder.length() < 6) {
                    btnCreatePin.setEnabled(false);
                    btnCreatePin.setAlpha(0.5f);
                }
            }
        }
    }
    
    private void showConfirmSection() {
        isConfirmMode = true;
        confirmSection.setVisibility(View.VISIBLE);
        
        // Scroll to show confirm section
        findViewById(R.id.confirm_section).requestFocus();
    }
    
    private void updateBullets() {
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
        
        // Update hidden EditTexts for compatibility
        updateHiddenEditTexts();
    }
    
    private void updateHiddenEditTexts() {
        EditText[] newPins = {newPin1, newPin2, newPin3, newPin4, newPin5, newPin6};
        for (int i = 0; i < 6; i++) {
            if (i < newPinBuilder.length()) {
                newPins[i].setText(String.valueOf(newPinBuilder.charAt(i)));
            } else {
                newPins[i].setText("");
            }
        }
    }
    
    private void createPin() {
        String newPin = newPinBuilder.toString();
        String confirmPin = confirmPinBuilder.toString();
        
        // Validate PIN length
        if (newPin.length() != 6) {
            Toast.makeText(this, "Masukkan PIN 6 digit", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (confirmPin.length() != 6) {
            Toast.makeText(this, "Masukkan konfirmasi PIN 6 digit", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if PINs match
        if (!newPin.equals(confirmPin)) {
            Toast.makeText(this, "PIN tidak cocok", Toast.LENGTH_SHORT).show();
            clearConfirmPin();
            return;
        }
        
        // Check for weak PINs
        if (isWeakPin(newPin)) {
            showWeakPinWarning(newPin);
            return;
        }
        
        // Save PIN
        savePinToPreferences(newPin);
    }
    
    private boolean isWeakPin(String pin) {
        // Check for sequential numbers
        if (pin.equals("123456") || pin.equals("654321") || 
            pin.equals("111111") || pin.equals("000000") ||
            pin.equals("123123") || pin.equals("112233")) {
            return true;
        }
        
        // Check if all digits are the same
        boolean allSame = true;
        char firstChar = pin.charAt(0);
        for (int i = 1; i < pin.length(); i++) {
            if (pin.charAt(i) != firstChar) {
                allSame = false;
                break;
            }
        }
        
        return allSame;
    }
    
    private void showWeakPinWarning(String pin) {
        new AlertDialog.Builder(this)
            .setTitle("PIN Lemah")
            .setMessage("PIN yang Anda masukkan terlalu mudah ditebak. Apakah Anda yakin ingin menggunakan PIN ini?")
            .setPositiveButton("Ganti PIN", (dialog, which) -> {
                clearAllPins();
            })
            .setNegativeButton("Tetap Gunakan", (dialog, which) -> savePinToPreferences(pin))
            .show();
    }
    
    private void savePinToPreferences(String pin) {
        // Show processing dialog
        AlertDialog processingDialog = new AlertDialog.Builder(this)
            .setTitle("Memproses")
            .setMessage("Menyimpan PIN...")
            .setCancelable(false)
            .create();
        processingDialog.show();
        
        // Simulate saving
        handler.postDelayed(() -> {
            // Save to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("pin_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_pin", pin);
            editor.putLong("pin_created_time", System.currentTimeMillis());
            editor.apply();
            
            processingDialog.dismiss();
            showSuccessDialog();
        }, 1500);
    }
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Berhasil")
            .setMessage("PIN berhasil dibuat!\n\nGunakan PIN ini untuk semua transaksi Anda.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    private void clearConfirmPin() {
        confirmPinBuilder.setLength(0);
        updateBullets();
        btnCreatePin.setEnabled(false);
        btnCreatePin.setAlpha(0.5f);
    }
    
    private void clearAllPins() {
        newPinBuilder.setLength(0);
        confirmPinBuilder.setLength(0);
        isConfirmMode = false;
        confirmSection.setVisibility(View.GONE);
        btnCreatePin.setEnabled(false);
        btnCreatePin.setAlpha(0.5f);
        updateBullets();
    }
}