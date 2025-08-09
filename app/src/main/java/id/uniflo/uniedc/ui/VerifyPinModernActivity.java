package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import id.uniflo.uniedc.R;

public class VerifyPinModernActivity extends Activity {
    
    private ImageView backButton;
    private TextView statusText;
    
    // Request codes
    private static final int REQUEST_CARD_READING = 1001;
    private static final int REQUEST_PIN_ENTRY = 1002;
    
    // Card data
    private String cardNumber = "";
    private String enteredPin = "";
    
    // PIN data
    private int attempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create a simple layout for status display
        setContentView(R.layout.activity_verify_pin_modern);
        
        initViews();
        
        // Start with card reading
        launchCardReader();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        statusText = findViewById(R.id.attempts_text);
        
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        // Hide all the old PIN input elements
        hideOldPinElements();
    }
    
    private void hideOldPinElements() {
        // Hide PIN bullets if they exist
        View[] bulletIds = {
            findViewById(R.id.pin_bullet_1),
            findViewById(R.id.pin_bullet_2),
            findViewById(R.id.pin_bullet_3),
            findViewById(R.id.pin_bullet_4),
            findViewById(R.id.pin_bullet_5),
            findViewById(R.id.pin_bullet_6)
        };
        
        for (View bullet : bulletIds) {
            if (bullet != null) {
                bullet.setVisibility(View.GONE);
            }
        }
        
        // Hide keypad buttons if they exist
        int[] keyIds = {
            R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
            R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9,
            R.id.key_clear, R.id.btn_verify_pin
        };
        
        for (int id : keyIds) {
            View key = findViewById(id);
            if (key != null) {
                key.setVisibility(View.GONE);
            }
        }
        
        // Hide keypad section if exists
        View keypadSection = findViewById(R.id.keypad_section);
        if (keypadSection != null) {
            keypadSection.setVisibility(View.GONE);
        }
    }
    
    private void launchCardReader() {
        Intent intent = new Intent(this, CardReaderActivity.class);
        startActivityForResult(intent, REQUEST_CARD_READING);
    }
    
    private void launchPinEntry() {
        Intent intent = new Intent(this, SalesPinActivity.class);
        intent.putExtra(SalesPinActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(SalesPinActivity.EXTRA_TRANSACTION_TYPE, "verify_pin");
        startActivityForResult(intent, REQUEST_PIN_ENTRY);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CARD_READING) {
            if (resultCode == RESULT_OK) {
                // Card reading successful
                if (data != null) {
                    cardNumber = data.getStringExtra(CardReaderActivity.EXTRA_CARD_NUMBER);
                    // Now launch PIN entry
                    launchPinEntry();
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
                    // Verify the PIN
                    verifyPin();
                } else {
                    Toast.makeText(this, "PIN tidak dimasukkan", Toast.LENGTH_SHORT).show();
                    handleFailedAttempt();
                }
            } else {
                // PIN entry cancelled
                Toast.makeText(this, "Verifikasi PIN dibatalkan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void handleFailedAttempt() {
        attempts++;
        if (attempts >= MAX_ATTEMPTS) {
            showBlockedDialog();
        } else {
            showErrorDialog();
        }
    }
    
    private void verifyPin() {
        if (enteredPin.length() < 4) {
            Toast.makeText(this, "PIN tidak valid", Toast.LENGTH_SHORT).show();
            handleFailedAttempt();
            return;
        }
        
        // Show processing
        if (statusText != null) {
            statusText.setText("Memverifikasi PIN...");
        }
        
        // Simulate PIN verification
        handler.postDelayed(() -> {
            // Get saved PIN from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("pin_prefs", MODE_PRIVATE);
            String savedPin = prefs.getString("user_pin", "123456"); // Default PIN
            
            if (enteredPin.equals(savedPin)) {
                showSuccessDialog();
            } else {
                handleFailedAttempt();
            }
        }, 1500);
    }
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Berhasil")
            .setMessage("PIN terverifikasi!")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    private void showErrorDialog() {
        int remainingAttempts = MAX_ATTEMPTS - attempts;
        
        new AlertDialog.Builder(this)
            .setTitle("PIN Salah")
            .setMessage("PIN yang Anda masukkan salah.\nSisa percobaan: " + remainingAttempts)
            .setPositiveButton("Coba Lagi", (dialog, which) -> {
                // Launch PIN entry again
                launchPinEntry();
            })
            .setNegativeButton("Batal", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    private void showBlockedDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Akses Diblokir")
            .setMessage("Anda telah memasukkan PIN salah 3 kali.\nSilakan hubungi customer service.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
}