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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import id.uniflo.uniedc.R;
// Database imports removed - using in-memory storage
import id.uniflo.uniedc.util.PinEncryptionUtil;

import java.util.List;

public class BasVerifyPinActivity extends Activity {
    
    private ImageView backButton;
    private TextView titleText;
    private EditText[] pinBoxes = new EditText[6];
    private View btnSubmit;
    private TextView btnSubmitText;
    private TextView errorText;
    private TextView instructionText;
    
    private String enteredPin = "";
    private int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 3;
    
    private ProgressDialog progressDialog;
    // Database removed - using SharedPreferences
    private String storedEncryptedPin;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_verify_pin);
        
        // Database initialization removed - using SharedPreferences
        
        initViews();
        setupListeners();
        loadActiveCard();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        titleText.setText(getString(R.string.menu_verify_pin));
        
        instructionText = findViewById(R.id.instruction_text);
        
        // PIN entry fields
        pinBoxes[0] = findViewById(R.id.pin_box_1);
        pinBoxes[1] = findViewById(R.id.pin_box_2);
        pinBoxes[2] = findViewById(R.id.pin_box_3);
        pinBoxes[3] = findViewById(R.id.pin_box_4);
        pinBoxes[4] = findViewById(R.id.pin_box_5);
        pinBoxes[5] = findViewById(R.id.pin_box_6);
        
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmitText = findViewById(R.id.btn_submit_text);
        btnSubmitText.setText(getString(R.string.confirm));
        
        errorText = findViewById(R.id.error_text);
        
        // Initially hide error
        errorText.setVisibility(View.GONE);
        
        // Request focus on first box
        pinBoxes[0].requestFocus();
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        // Setup PIN boxes
        setupPinBoxes();
        
        btnSubmit.setOnClickListener(v -> {
            if (validatePin()) {
                verifyPin();
            }
        });
    }
    
    private void setupPinBoxes() {
        for (int i = 0; i < pinBoxes.length; i++) {
            final int index = i;
            pinBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        if (index < pinBoxes.length - 1) {
                            pinBoxes[index + 1].requestFocus();
                        } else {
                            // Auto-submit when all 6 digits are entered
                            updatePinString();
                            if (enteredPin.length() == 6) {
                                verifyPin();
                            }
                        }
                    }
                    updatePinString();
                    errorText.setVisibility(View.GONE);
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
            
            pinBoxes[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && 
                    event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (pinBoxes[index].getText().toString().isEmpty() && index > 0) {
                        pinBoxes[index - 1].requestFocus();
                        pinBoxes[index - 1].setText("");
                    }
                    return true;
                }
                return false;
            });
        }
    }
    
    private void updatePinString() {
        StringBuilder pin = new StringBuilder();
        for (EditText box : pinBoxes) {
            pin.append(box.getText().toString());
        }
        enteredPin = pin.toString();
    }
    
    private void loadActiveCard() {
        // Load PIN from SharedPreferences
        storedEncryptedPin = getSharedPreferences("card_data", MODE_PRIVATE)
            .getString("pin", PinEncryptionUtil.encryptPin("123456")); // Default PIN if not set
    }
    
    private boolean validatePin() {
        if (storedEncryptedPin == null || storedEncryptedPin.isEmpty()) {
            showError(getString(R.string.card_reader_not_available));
            return false;
        }
        
        if (enteredPin.length() != 6) {
            showError(getString(R.string.create_pin_desc));
            return false;
        }
        
        return true;
    }
    
    private void clearPinBoxes() {
        for (EditText box : pinBoxes) {
            box.setText("");
        }
        pinBoxes[0].requestFocus();
        enteredPin = "";
    }
    
    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
    
    private void verifyPin() {
        if (!validatePin()) return;
        
        showProgressDialog(getString(R.string.please_wait));
        
        // Simulate PIN verification process
        new Handler().postDelayed(() -> {
            if (PinEncryptionUtil.verifyPin(enteredPin, storedEncryptedPin)) {
                hideProgressDialog();
                Toast.makeText(this, getString(R.string.pin_verification_success), Toast.LENGTH_SHORT).show();
                
                // Return success result
                Intent resultIntent = new Intent();
                resultIntent.putExtra("pin_verified", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                hideProgressDialog();
                attemptCount++;
                
                if (attemptCount >= MAX_ATTEMPTS) {
                    showError(getString(R.string.transaction_declined));
                    btnSubmit.setEnabled(false);
                    
                    // In a real app, you would block the card here
                    new Handler().postDelayed(() -> {
                        finish();
                    }, 3000);
                } else {
                    int remainingAttempts = MAX_ATTEMPTS - attemptCount;
                    showError(getString(R.string.incorrect_pin) + ". " + String.format(getString(R.string.attempts_remaining), remainingAttempts));
                    clearPinBoxes();
                }
            }
        }, 1500);
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