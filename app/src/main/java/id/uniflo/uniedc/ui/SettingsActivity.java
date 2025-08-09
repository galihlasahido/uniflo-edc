package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import id.uniflo.uniedc.R;

public class SettingsActivity extends Activity {
    
    // Back button
    private ImageView backButton;
    
    // Setting options - match layout IDs
    private LinearLayout itemDeviceInfo;
    private LinearLayout itemTerminalConfig;
    private LinearLayout itemNetworkSettings;
    private LinearLayout itemPrinterSettings;
    private LinearLayout itemSecuritySettings;
    private LinearLayout itemTransactionLimits;
    private LinearLayout itemAidSettings;
    private LinearLayout itemAbout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        
        // Setting options - match actual layout IDs
        itemDeviceInfo = findViewById(R.id.item_device_info);
        itemTerminalConfig = findViewById(R.id.item_terminal_config);
        itemNetworkSettings = findViewById(R.id.item_network_settings);
        itemPrinterSettings = findViewById(R.id.item_printer_settings);
        itemSecuritySettings = findViewById(R.id.item_security_settings);
        itemTransactionLimits = findViewById(R.id.item_transaction_limits);
        itemAidSettings = findViewById(R.id.item_aid_settings);
        itemAbout = findViewById(R.id.item_about);
    }
    
    private void setupListeners() {
        // Handle back button
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        itemDeviceInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeviceInfoActivity.class);
            startActivity(intent);
        });
        
        itemTerminalConfig.setOnClickListener(v -> {
            Intent intent = new Intent(this, TerminalConfigActivity.class);
            startActivity(intent);
        });
        
        itemNetworkSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, NetworkSettingsActivity.class);
            startActivity(intent);
        });
        
        itemPrinterSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrinterSettingsActivity.class);
            startActivity(intent);
        });
        
        itemSecuritySettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecuritySettingsActivity.class);
            startActivity(intent);
        });
        
        itemTransactionLimits.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionLimitsActivity.class);
            startActivity(intent);
        });
        
        itemAidSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, AidSettingsActivity.class);
            startActivity(intent);
        });
        
        itemAbout.setOnClickListener(v -> {
            // Navigate to app info
            Intent intent = new Intent(this, AppInfoActivity.class);
            startActivity(intent);
        });
    }
}