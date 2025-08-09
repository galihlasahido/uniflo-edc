package id.uniflo.uniedc.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.database.SecuritySettings;
import id.uniflo.uniedc.database.SecureSettingsDAO;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecuritySettingsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private SwitchMaterial switchPinVerification;
    private SwitchMaterial switchVoidPassword;
    private SwitchMaterial switchSettlementPassword;
    private SwitchMaterial switchRefundPassword;
    private TextInputEditText etAdminPin;
    private TextInputEditText etMaxPinAttempts;
    private TextView tvKeyStatus;
    private TextView tvLastKeyDownload;
    private Button btnChangeAdminPin;
    private Button btnDownloadKeys;
    private Button btnClearKeys;
    private Button btnSave;
    
    private SecureSettingsDAO settingsDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_settings);
        
        settingsDAO = new SecureSettingsDAO(this);
        
        initViews();
        setupToolbar();
        loadCurrentSettings();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        switchPinVerification = findViewById(R.id.switch_pin_verification);
        switchVoidPassword = findViewById(R.id.switch_void_password);
        switchSettlementPassword = findViewById(R.id.switch_settlement_password);
        switchRefundPassword = findViewById(R.id.switch_refund_password);
        etAdminPin = findViewById(R.id.et_admin_pin);
        etMaxPinAttempts = findViewById(R.id.et_max_pin_attempts);
        tvKeyStatus = findViewById(R.id.tv_key_status);
        tvLastKeyDownload = findViewById(R.id.tv_last_key_download);
        btnChangeAdminPin = findViewById(R.id.btn_change_admin_pin);
        btnDownloadKeys = findViewById(R.id.btn_download_keys);
        btnClearKeys = findViewById(R.id.btn_clear_keys);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Security Settings");
        }
    }
    
    private void loadCurrentSettings() {
        SecuritySettings settings = settingsDAO.getSecuritySettings();
        if (settings != null) {
            switchPinVerification.setChecked(settings.isPinVerification());
            switchVoidPassword.setChecked(settings.isVoidPassword());
            switchSettlementPassword.setChecked(settings.isSettlementPassword());
            switchRefundPassword.setChecked(settings.isRefundPassword());
            
            etAdminPin.setText("****"); // Always show masked
            etMaxPinAttempts.setText(String.valueOf(settings.getMaxPinAttempts()));
            
            tvKeyStatus.setText(settings.getKeyStatus());
            tvLastKeyDownload.setText(settings.getLastKeyDownload() != null ? 
                settings.getLastKeyDownload() : "Never");
            
            // Update key status color
            if ("Active".equals(settings.getKeyStatus())) {
                tvKeyStatus.setTextColor(getResources().getColor(R.color.colorSuccess));
            } else {
                tvKeyStatus.setTextColor(getResources().getColor(R.color.colorDanger));
            }
        }
    }
    
    private void setupListeners() {
        btnChangeAdminPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeAdminPinDialog();
            }
        });
        
        btnDownloadKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadEncryptionKeys();
            }
        });
        
        btnClearKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearKeysConfirmation();
            }
        });
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }
    
    private void showChangeAdminPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Admin PIN");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        final EditText currentPinInput = new EditText(this);
        currentPinInput.setHint("Current PIN");
        currentPinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(currentPinInput);
        
        final EditText newPinInput = new EditText(this);
        newPinInput.setHint("New PIN");
        newPinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(newPinInput);
        
        final EditText confirmPinInput = new EditText(this);
        confirmPinInput.setHint("Confirm New PIN");
        confirmPinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(confirmPinInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currentPin = currentPinInput.getText().toString();
                String newPin = newPinInput.getText().toString();
                String confirmPin = confirmPinInput.getText().toString();
                
                if (validatePinChange(currentPin, newPin, confirmPin)) {
                    // Change PIN in database
                    SecuritySettings settings = settingsDAO.getSecuritySettings();
                    if (settings != null) {
                        settings.setAdminPin(newPin);
                        if (settingsDAO.saveSecuritySettings(settings)) {
                            Toast.makeText(SecuritySettingsActivity.this, 
                                "Admin PIN changed successfully", Toast.LENGTH_SHORT).show();
                            etAdminPin.setText("****");
                        }
                    }
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private boolean validatePinChange(String currentPin, String newPin, String confirmPin) {
        if (currentPin.isEmpty()) {
            Toast.makeText(this, "Please enter current PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPin.isEmpty()) {
            Toast.makeText(this, "Please enter new PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPin.length() < 4 || newPin.length() > 6) {
            Toast.makeText(this, "PIN must be 4-6 digits", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!newPin.equals(confirmPin)) {
            Toast.makeText(this, "New PIN and confirmation do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Verify current PIN from database
        if (!settingsDAO.verifyPin(currentPin)) {
            Toast.makeText(this, "Incorrect current PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void downloadEncryptionKeys() {
        // Show progress
        btnDownloadKeys.setEnabled(false);
        btnDownloadKeys.setText("Downloading...");
        
        // Simulate key download
        btnDownloadKeys.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnDownloadKeys.setEnabled(true);
                btnDownloadKeys.setText("Download Keys");
                
                // Update database
                SecuritySettings settings = settingsDAO.getSecuritySettings();
                if (settings != null) {
                    settings.setKeyStatus("Active");
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                    settings.setLastKeyDownload(sdf.format(new Date()));
                    settingsDAO.saveSecuritySettings(settings);
                    
                    tvKeyStatus.setText("Active");
                    tvKeyStatus.setTextColor(getResources().getColor(R.color.colorSuccess));
                    tvLastKeyDownload.setText(settings.getLastKeyDownload());
                    
                    Toast.makeText(SecuritySettingsActivity.this, 
                        "Encryption keys downloaded successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }, 3000);
    }
    
    private void showClearKeysConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Encryption Keys")
            .setMessage("This will remove all encryption keys. You will need to download new keys before processing transactions. Continue?")
            .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearEncryptionKeys();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void clearEncryptionKeys() {
        // Update database
        SecuritySettings settings = settingsDAO.getSecuritySettings();
        if (settings != null) {
            settings.setKeyStatus("Not Active");
            settings.setLastKeyDownload(null);
            settingsDAO.saveSecuritySettings(settings);
            
            tvKeyStatus.setText("Not Active");
            tvKeyStatus.setTextColor(getResources().getColor(R.color.colorDanger));
            tvLastKeyDownload.setText("Never");
            Toast.makeText(this, "Encryption keys cleared", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveSettings() {
        // Validate inputs
        String maxAttempts = etMaxPinAttempts.getText().toString();
        if (maxAttempts.isEmpty()) {
            etMaxPinAttempts.setError("Required");
            return;
        }
        
        try {
            int attempts = Integer.parseInt(maxAttempts);
            if (attempts < 1 || attempts > 5) {
                etMaxPinAttempts.setError("Must be between 1 and 5");
                return;
            }
        } catch (NumberFormatException e) {
            etMaxPinAttempts.setError("Invalid number");
            return;
        }
        
        // Create settings object
        SecuritySettings settings = settingsDAO.getSecuritySettings();
        if (settings == null) {
            settings = new SecuritySettings();
        }
        
        settings.setPinVerification(switchPinVerification.isChecked());
        settings.setMaxPinAttempts(Integer.parseInt(maxAttempts));
        settings.setVoidPassword(switchVoidPassword.isChecked());
        settings.setSettlementPassword(switchSettlementPassword.isChecked());
        settings.setRefundPassword(switchRefundPassword.isChecked());
        // Admin PIN and key status are managed separately
        
        // Save to database
        if (settingsDAO.saveSecuritySettings(settings)) {
            Toast.makeText(this, "Security settings saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}