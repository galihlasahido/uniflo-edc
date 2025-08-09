package id.uniflo.uniedc.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.database.TerminalConfig;
import id.uniflo.uniedc.database.SecureSettingsDAO;

public class TerminalConfigActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextInputEditText etTerminalId;
    private TextInputEditText etMerchantId;
    private TextInputEditText etMerchantName;
    private TextInputEditText etMerchantAddress;
    private TextInputEditText etMerchantCity;
    private TextInputEditText etMerchantPhone;
    private TextInputEditText etAcquiringInstitutionCode;
    private Spinner spinnerCurrency;
    private Spinner spinnerLanguage;
    private Spinner spinnerDateFormat;
    private SwitchMaterial switchTipEnabled;
    private SwitchMaterial switchSignatureRequired;
    private SwitchMaterial switchOfflineMode;
    private TextInputEditText etBatchNumber;
    private TextInputEditText etTraceNumber;
    private TextInputEditText etInvoiceNumber;
    private Button btnSave;
    private Button btnResetCounters;
    
    private SecureSettingsDAO settingsDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_config);
        
        settingsDAO = new SecureSettingsDAO(this);
        
        initViews();
        setupToolbar();
        setupSpinners();
        loadCurrentSettings();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTerminalId = findViewById(R.id.et_terminal_id);
        etMerchantId = findViewById(R.id.et_merchant_id);
        etMerchantName = findViewById(R.id.et_merchant_name);
        etMerchantAddress = findViewById(R.id.et_merchant_address);
        etMerchantCity = findViewById(R.id.et_merchant_city);
        etMerchantPhone = findViewById(R.id.et_merchant_phone);
        etAcquiringInstitutionCode = findViewById(R.id.et_acquiring_institution_code);
        spinnerCurrency = findViewById(R.id.spinner_currency);
        spinnerLanguage = findViewById(R.id.spinner_language);
        spinnerDateFormat = findViewById(R.id.spinner_date_format);
        switchTipEnabled = findViewById(R.id.switch_tip_enabled);
        switchSignatureRequired = findViewById(R.id.switch_signature_required);
        switchOfflineMode = findViewById(R.id.switch_offline_mode);
        etBatchNumber = findViewById(R.id.et_batch_number);
        etTraceNumber = findViewById(R.id.et_trace_number);
        etInvoiceNumber = findViewById(R.id.et_invoice_number);
        btnSave = findViewById(R.id.btn_save);
        btnResetCounters = findViewById(R.id.btn_reset_counters);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Terminal Configuration");
        }
    }
    
    private void setupSpinners() {
        // Currency spinner
        String[] currencies = {"IDR - Indonesian Rupiah", "USD - US Dollar", "SGD - Singapore Dollar", "MYR - Malaysian Ringgit"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);
        
        // Language spinner
        String[] languages = {"English", "Bahasa Indonesia", "Chinese (Simplified)", "Chinese (Traditional)"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);
        
        // Date format spinner
        String[] dateFormats = {"DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"};
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, dateFormats);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateFormat.setAdapter(dateAdapter);
    }
    
    private void loadCurrentSettings() {
        TerminalConfig config = settingsDAO.getTerminalConfig();
        if (config != null) {
            etTerminalId.setText(config.getTerminalId());
            etMerchantId.setText(config.getMerchantId());
            etMerchantName.setText(config.getMerchantName());
            etMerchantAddress.setText(config.getMerchantAddress());
            etMerchantCity.setText(config.getMerchantCity());
            etMerchantPhone.setText(config.getMerchantPhone());
            etAcquiringInstitutionCode.setText(config.getAcquiringInstitutionCode());
            
            // Set currency selection
            String currency = config.getCurrency();
            if ("IDR".equals(currency)) {
                spinnerCurrency.setSelection(0);
            } else if ("USD".equals(currency)) {
                spinnerCurrency.setSelection(1);
            } else if ("SGD".equals(currency)) {
                spinnerCurrency.setSelection(2);
            } else if ("MYR".equals(currency)) {
                spinnerCurrency.setSelection(3);
            }
            
            // Set language selection
            String language = config.getLanguage();
            if ("English".equals(language)) {
                spinnerLanguage.setSelection(0);
            } else if ("Bahasa Indonesia".equals(language)) {
                spinnerLanguage.setSelection(1);
            } else if ("Chinese (Simplified)".equals(language)) {
                spinnerLanguage.setSelection(2);
            } else if ("Chinese (Traditional)".equals(language)) {
                spinnerLanguage.setSelection(3);
            }
            
            // Set date format selection
            String dateFormat = config.getDateFormat();
            if ("DD/MM/YYYY".equals(dateFormat)) {
                spinnerDateFormat.setSelection(0);
            } else if ("MM/DD/YYYY".equals(dateFormat)) {
                spinnerDateFormat.setSelection(1);
            } else if ("YYYY-MM-DD".equals(dateFormat)) {
                spinnerDateFormat.setSelection(2);
            }
            
            switchTipEnabled.setChecked(config.isTipEnabled());
            switchSignatureRequired.setChecked(config.isSignatureRequired());
            switchOfflineMode.setChecked(config.isOfflineMode());
            
            etBatchNumber.setText(config.getBatchNumber());
            etTraceNumber.setText(config.getTraceNumber());
            etInvoiceNumber.setText(config.getInvoiceNumber());
        }
    }
    
    private void setupListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
        
        btnResetCounters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCounters();
            }
        });
    }
    
    private void saveSettings() {
        // Validate required fields
        if (!validateInputs()) {
            return;
        }
        
        // Create config object
        TerminalConfig config = new TerminalConfig();
        
        config.setTerminalId(etTerminalId.getText().toString().trim());
        config.setMerchantId(etMerchantId.getText().toString().trim());
        config.setMerchantName(etMerchantName.getText().toString().trim());
        config.setMerchantAddress(etMerchantAddress.getText().toString().trim());
        config.setMerchantCity(etMerchantCity.getText().toString().trim());
        config.setMerchantPhone(etMerchantPhone.getText().toString().trim());
        config.setAcquiringInstitutionCode(etAcquiringInstitutionCode.getText().toString().trim());
        
        // Get selected currency
        String[] currencies = {"IDR", "USD", "SGD", "MYR"};
        config.setCurrency(currencies[spinnerCurrency.getSelectedItemPosition()]);
        
        // Get selected language
        String[] languages = {"English", "Bahasa Indonesia", "Chinese (Simplified)", "Chinese (Traditional)"};
        config.setLanguage(languages[spinnerLanguage.getSelectedItemPosition()]);
        
        // Get selected date format
        String[] dateFormats = {"DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"};
        config.setDateFormat(dateFormats[spinnerDateFormat.getSelectedItemPosition()]);
        
        config.setTipEnabled(switchTipEnabled.isChecked());
        config.setSignatureRequired(switchSignatureRequired.isChecked());
        config.setOfflineMode(switchOfflineMode.isChecked());
        
        config.setBatchNumber(etBatchNumber.getText().toString());
        config.setTraceNumber(etTraceNumber.getText().toString());
        config.setInvoiceNumber(etInvoiceNumber.getText().toString());
        
        // Save to database
        if (settingsDAO.saveTerminalConfig(config)) {
            Toast.makeText(this, "Terminal configuration saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save configuration", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validateInputs() {
        if (etTerminalId.getText().toString().trim().isEmpty()) {
            etTerminalId.setError("Terminal ID is required");
            return false;
        }
        
        if (etTerminalId.getText().toString().length() != 8) {
            etTerminalId.setError("Terminal ID must be 8 digits");
            return false;
        }
        
        if (etMerchantId.getText().toString().trim().isEmpty()) {
            etMerchantId.setError("Merchant ID is required");
            return false;
        }
        
        if (etMerchantId.getText().toString().length() != 15) {
            etMerchantId.setError("Merchant ID must be 15 digits");
            return false;
        }
        
        if (etMerchantName.getText().toString().trim().isEmpty()) {
            etMerchantName.setError("Merchant name is required");
            return false;
        }
        
        if (etMerchantAddress.getText().toString().trim().isEmpty()) {
            etMerchantAddress.setError("Merchant address is required");
            return false;
        }
        
        if (etMerchantCity.getText().toString().trim().isEmpty()) {
            etMerchantCity.setError("Merchant city is required");
            return false;
        }
        
        if (etAcquiringInstitutionCode.getText().toString().trim().isEmpty()) {
            etAcquiringInstitutionCode.setError("Acquiring Institution Code is required");
            return false;
        }
        
        if (etAcquiringInstitutionCode.getText().toString().length() != 6) {
            etAcquiringInstitutionCode.setError("Acquiring Institution Code must be 6 digits");
            return false;
        }
        
        return true;
    }
    
    private void resetCounters() {
        settingsDAO.resetCounters();
        etBatchNumber.setText("000001");
        etTraceNumber.setText("000001");
        etInvoiceNumber.setText("000001");
        Toast.makeText(this, "Counters reset successfully", Toast.LENGTH_SHORT).show();
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