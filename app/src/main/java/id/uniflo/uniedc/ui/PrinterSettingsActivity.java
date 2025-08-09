package id.uniflo.uniedc.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.IPrinter;
import id.uniflo.uniedc.database.PrinterSettings;
import id.uniflo.uniedc.database.SecureSettingsDAO;

public class PrinterSettingsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private SeekBar seekBarDensity;
    private TextView tvDensityValue;
    private SwitchMaterial switchLogo;
    private SwitchMaterial switchMerchantCopy;
    private SwitchMaterial switchCustomerCopy;
    private TextInputEditText etHeaderLine1;
    private TextInputEditText etHeaderLine2;
    private TextInputEditText etFooterLine1;
    private TextInputEditText etFooterLine2;
    private Button btnTestPrint;
    private Button btnSave;
    
    private SDKManager sdkManager;
    private IPrinter printer;
    private SecureSettingsDAO settingsDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_settings);
        
        settingsDAO = new SecureSettingsDAO(this);
        
        initViews();
        setupToolbar();
        initializeSDK();
        loadCurrentSettings();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        seekBarDensity = findViewById(R.id.seekbar_density);
        tvDensityValue = findViewById(R.id.tv_density_value);
        switchLogo = findViewById(R.id.switch_logo);
        switchMerchantCopy = findViewById(R.id.switch_merchant_copy);
        switchCustomerCopy = findViewById(R.id.switch_customer_copy);
        etHeaderLine1 = findViewById(R.id.et_header_line1);
        etHeaderLine2 = findViewById(R.id.et_header_line2);
        etFooterLine1 = findViewById(R.id.et_footer_line1);
        etFooterLine2 = findViewById(R.id.et_footer_line2);
        btnTestPrint = findViewById(R.id.btn_test_print);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Printer Settings");
        }
    }
    
    private void initializeSDK() {
        sdkManager = SDKManager.getInstance();
        printer = sdkManager.getPrinter();
    }
    
    private void loadCurrentSettings() {
        PrinterSettings settings = settingsDAO.getPrinterSettings();
        if (settings != null) {
            seekBarDensity.setProgress(settings.getPrintDensity());
            updateDensityText(settings.getPrintDensity());
            
            switchLogo.setChecked(settings.isPrintLogo());
            switchMerchantCopy.setChecked(settings.isPrintMerchantCopy());
            switchCustomerCopy.setChecked(settings.isPrintCustomerCopy());
            
            etHeaderLine1.setText(settings.getHeaderLine1());
            etHeaderLine2.setText(settings.getHeaderLine2());
            etFooterLine1.setText(settings.getFooterLine1());
            etFooterLine2.setText(settings.getFooterLine2());
        }
    }
    
    private void setupListeners() {
        seekBarDensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateDensityText(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        btnTestPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performTestPrint();
            }
        });
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }
    
    private void updateDensityText(int progress) {
        String density;
        if (progress < 33) {
            density = "Light";
        } else if (progress < 66) {
            density = "Medium";
        } else {
            density = "Dark";
        }
        tvDensityValue.setText(density);
    }
    
    private void performTestPrint() {
        if (printer == null) {
            Toast.makeText(this, "Printer not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Build test receipt
        StringBuilder receipt = new StringBuilder();
        
        // Header
        if (switchLogo.isChecked()) {
            receipt.append("[LOGO]\n\n");
        }
        
        receipt.append(centerText(etHeaderLine1.getText().toString())).append("\n");
        receipt.append(centerText(etHeaderLine2.getText().toString())).append("\n");
        receipt.append("================================\n");
        receipt.append("        PRINTER TEST PAGE       \n");
        receipt.append("================================\n\n");
        
        // Test content
        receipt.append("Date: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date())).append("\n");
        receipt.append("Density: ").append(tvDensityValue.getText()).append("\n");
        receipt.append("Print Quality Test:\n");
        receipt.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ\n");
        receipt.append("abcdefghijklmnopqrstuvwxyz\n");
        receipt.append("0123456789\n");
        receipt.append("!@#$%^&*()_+-=[]{}|;':\",./<>?\n\n");
        
        // Footer
        receipt.append("================================\n");
        receipt.append(centerText(etFooterLine1.getText().toString())).append("\n");
        receipt.append(centerText(etFooterLine2.getText().toString())).append("\n");
        receipt.append("\n\n\n\n"); // Feed paper
        
        // Print
        printer.init();
        printer.printText(receipt.toString());
        
        Toast.makeText(this, "Test print initiated", Toast.LENGTH_SHORT).show();
    }
    
    private String centerText(String text) {
        int width = 32; // Typical receipt width
        if (text.length() >= width) {
            return text;
        }
        
        int padding = (width - text.length()) / 2;
        StringBuilder centered = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            centered.append(" ");
        }
        centered.append(text);
        return centered.toString();
    }
    
    private void saveSettings() {
        // Create settings object
        PrinterSettings settings = new PrinterSettings();
        
        settings.setPrintDensity(seekBarDensity.getProgress());
        settings.setPrintLogo(switchLogo.isChecked());
        settings.setPrintMerchantCopy(switchMerchantCopy.isChecked());
        settings.setPrintCustomerCopy(switchCustomerCopy.isChecked());
        settings.setHeaderLine1(etHeaderLine1.getText().toString());
        settings.setHeaderLine2(etHeaderLine2.getText().toString());
        settings.setFooterLine1(etFooterLine1.getText().toString());
        settings.setFooterLine2(etFooterLine2.getText().toString());
        
        // Save to database
        if (settingsDAO.savePrinterSettings(settings)) {
            Toast.makeText(this, "Printer settings saved successfully", Toast.LENGTH_SHORT).show();
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