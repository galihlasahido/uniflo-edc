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
import java.util.Locale;

public class BasCreatePinActivity extends Activity {
    
    private ImageView backButton;
    private TextView titleText;
    private EditText[] pinBoxes = new EditText[6];
    private EditText[] confirmPinBoxes = new EditText[6];
    private View btnSubmit;
    private TextView btnSubmitText;
    private TextView errorText;
    private TextView pinLabel, confirmPinLabel;
    
    private String currentPin = "";
    private String confirmPin = "";
    private boolean isEnteringConfirmPin = false;
    
    private ProgressDialog progressDialog;
    // Database removed - using SharedPreferences for demo
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_create_pin);
        
        // Database initialization removed - using SharedPreferences
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        titleText.setText(getString(R.string.create_pin_title));
        
        // PIN entry fields
        pinBoxes[0] = findViewById(R.id.pin_box_1);
        pinBoxes[1] = findViewById(R.id.pin_box_2);
        pinBoxes[2] = findViewById(R.id.pin_box_3);
        pinBoxes[3] = findViewById(R.id.pin_box_4);
        pinBoxes[4] = findViewById(R.id.pin_box_5);
        pinBoxes[5] = findViewById(R.id.pin_box_6);
        
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
        pinLabel = findViewById(R.id.pin_label);
        confirmPinLabel = findViewById(R.id.confirm_pin_label);
        
        // Initially hide error
        errorText.setVisibility(View.GONE);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        // Setup PIN boxes
        setupPinBoxes(pinBoxes, false);
        setupPinBoxes(confirmPinBoxes, true);
        
        btnSubmit.setOnClickListener(v -> {
            if (validatePins()) {
                createPin();
            }
        });
    }
    
    private void setupPinBoxes(EditText[] boxes, boolean isConfirm) {
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
                    updatePinString(boxes, isConfirm);
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
    
    private void updatePinString(EditText[] boxes, boolean isConfirm) {
        StringBuilder pin = new StringBuilder();
        for (EditText box : boxes) {
            pin.append(box.getText().toString());
        }
        
        if (isConfirm) {
            confirmPin = pin.toString();
        } else {
            currentPin = pin.toString();
        }
    }
    
    private boolean validatePins() {
        if (currentPin.length() != 6) {
            showError(getString(R.string.create_pin_desc));
            return false;
        }
        
        if (confirmPin.length() != 6) {
            showError(getString(R.string.confirm_pin_desc));
            return false;
        }
        
        if (!currentPin.equals(confirmPin)) {
            showError(getString(R.string.pin_not_match));
            clearConfirmPin();
            return false;
        }
        
        return true;
    }
    
    private void clearConfirmPin() {
        for (EditText box : confirmPinBoxes) {
            box.setText("");
        }
        confirmPinBoxes[0].requestFocus();
        confirmPin = "";
    }
    
    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
    
    private void createPin() {
        showProgressDialog(getString(R.string.please_wait));
        
        // Simulate PIN creation process
        new Handler().postDelayed(() -> {
            try {
                // Save PIN to SharedPreferences (for demo)
                String encryptedPin = PinEncryptionUtil.encryptPin(currentPin);
                getSharedPreferences("card_data", MODE_PRIVATE)
                    .edit()
                    .putString("pin", encryptedPin)
                    .putString("card_number", "****-****-****-0000")
                    .putBoolean("has_pin", true)
                    .apply();
                
                hideProgressDialog();
                Toast.makeText(this, getString(R.string.pin_created_success), Toast.LENGTH_SHORT).show();
                
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