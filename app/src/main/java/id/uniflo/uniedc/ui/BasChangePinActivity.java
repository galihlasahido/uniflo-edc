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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BasChangePinActivity extends Activity {
    
    private ImageView backButton;
    private TextView titleText;
    private EditText[] currentPinBoxes = new EditText[6];
    private EditText[] newPinBoxes = new EditText[6];
    private EditText[] confirmPinBoxes = new EditText[6];
    private View btnSubmit;
    private TextView btnSubmitText;
    private TextView errorText;
    private TextView currentPinLabel, newPinLabel, confirmPinLabel;
    
    private String currentPin = "";
    private String newPin = "";
    private String confirmPin = "";
    
    private ProgressDialog progressDialog;
    // Database removed - using SharedPreferences
    private String storedEncryptedPin;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_change_pin);
        
        // Database initialization removed - using SharedPreferences
        
        initViews();
        setupListeners();
        loadActiveCard();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        titleText.setText(getString(R.string.menu_change_pin));
        
        // Current PIN entry fields
        currentPinBoxes[0] = findViewById(R.id.current_pin_box_1);
        currentPinBoxes[1] = findViewById(R.id.current_pin_box_2);
        currentPinBoxes[2] = findViewById(R.id.current_pin_box_3);
        currentPinBoxes[3] = findViewById(R.id.current_pin_box_4);
        currentPinBoxes[4] = findViewById(R.id.current_pin_box_5);
        currentPinBoxes[5] = findViewById(R.id.current_pin_box_6);
        
        // New PIN entry fields
        newPinBoxes[0] = findViewById(R.id.new_pin_box_1);
        newPinBoxes[1] = findViewById(R.id.new_pin_box_2);
        newPinBoxes[2] = findViewById(R.id.new_pin_box_3);
        newPinBoxes[3] = findViewById(R.id.new_pin_box_4);
        newPinBoxes[4] = findViewById(R.id.new_pin_box_5);
        newPinBoxes[5] = findViewById(R.id.new_pin_box_6);
        
        // Confirm PIN entry fields
        confirmPinBoxes[0] = findViewById(R.id.confirm_pin_box_1);
        confirmPinBoxes[1] = findViewById(R.id.confirm_pin_box_2);
        confirmPinBoxes[2] = findViewById(R.id.confirm_pin_box_3);
        confirmPinBoxes[3] = findViewById(R.id.confirm_pin_box_4);
        confirmPinBoxes[4] = findViewById(R.id.confirm_pin_box_5);
        confirmPinBoxes[5] = findViewById(R.id.confirm_pin_box_6);
        
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmitText = findViewById(R.id.btn_submit_text);
        btnSubmitText.setText(getString(R.string.submit));
        
        errorText = findViewById(R.id.error_text);
        currentPinLabel = findViewById(R.id.current_pin_label);
        newPinLabel = findViewById(R.id.new_pin_label);
        confirmPinLabel = findViewById(R.id.confirm_pin_label);
        
        // Initially hide error
        errorText.setVisibility(View.GONE);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        // Setup PIN boxes
        setupPinBoxes(currentPinBoxes, 0);
        setupPinBoxes(newPinBoxes, 1);
        setupPinBoxes(confirmPinBoxes, 2);
        
        btnSubmit.setOnClickListener(v -> {
            if (validatePins()) {
                changePin();
            }
        });
    }
    
    private void setupPinBoxes(EditText[] boxes, int type) {
        for (int i = 0; i < boxes.length; i++) {
            final int index = i;
            boxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < boxes.length - 1) {
                        boxes[index + 1].requestFocus();
                    }
                    updatePinString(boxes, type);
                    errorText.setVisibility(View.GONE);
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
            
            boxes[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && 
                    event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (boxes[index].getText().toString().isEmpty() && index > 0) {
                        boxes[index - 1].requestFocus();
                        boxes[index - 1].setText("");
                    }
                    return true;
                }
                return false;
            });
        }
    }
    
    private void updatePinString(EditText[] boxes, int type) {
        StringBuilder pin = new StringBuilder();
        for (EditText box : boxes) {
            pin.append(box.getText().toString());
        }
        
        switch (type) {
            case 0:
                currentPin = pin.toString();
                break;
            case 1:
                newPin = pin.toString();
                break;
            case 2:
                confirmPin = pin.toString();
                break;
        }
    }
    
    private void loadActiveCard() {
        // Load PIN from SharedPreferences
        storedEncryptedPin = getSharedPreferences("card_data", MODE_PRIVATE)
            .getString("pin", PinEncryptionUtil.encryptPin("123456")); // Default PIN if not set
    }
    
    private boolean validatePins() {
        if (storedEncryptedPin == null || storedEncryptedPin.isEmpty()) {
            showError(getString(R.string.card_reader_not_available));
            return false;
        }
        
        if (currentPin.length() != 6) {
            showError(getString(R.string.enter_current_pin));
            return false;
        }
        
        // Verify current PIN
        if (!PinEncryptionUtil.verifyPin(currentPin, storedEncryptedPin)) {
            showError(getString(R.string.incorrect_pin));
            clearPinBoxes(currentPinBoxes);
            currentPinBoxes[0].requestFocus();
            return false;
        }
        
        if (newPin.length() != 6) {
            showError(getString(R.string.enter_new_pin));
            return false;
        }
        
        if (confirmPin.length() != 6) {
            showError(getString(R.string.confirm_new_pin));
            return false;
        }
        
        if (!newPin.equals(confirmPin)) {
            showError(getString(R.string.pin_not_match));
            clearPinBoxes(confirmPinBoxes);
            confirmPinBoxes[0].requestFocus();
            return false;
        }
        
        if (currentPin.equals(newPin)) {
            showError(getString(R.string.enter_new_pin));
            return false;
        }
        
        return true;
    }
    
    private void clearPinBoxes(EditText[] boxes) {
        for (EditText box : boxes) {
            box.setText("");
        }
    }
    
    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
    
    private void changePin() {
        showProgressDialog(getString(R.string.please_wait));
        
        // Simulate PIN change process
        new Handler().postDelayed(() -> {
            try {
                // Update PIN in SharedPreferences
                String encryptedPin = PinEncryptionUtil.encryptPin(newPin);
                getSharedPreferences("card_data", MODE_PRIVATE)
                    .edit()
                    .putString("pin", encryptedPin)
                    .apply();
                
                hideProgressDialog();
                Toast.makeText(this, getString(R.string.pin_changed_success), Toast.LENGTH_SHORT).show();
                
                // Return to home
                Intent intent = new Intent(this, BasHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                
            } catch (Exception e) {
                hideProgressDialog();
                showError(getString(R.string.failed) + ": " + e.getMessage());
            }
        }, 2000);
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