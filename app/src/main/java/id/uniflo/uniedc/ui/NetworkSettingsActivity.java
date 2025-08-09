package id.uniflo.uniedc.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.database.NetworkSettings;
import id.uniflo.uniedc.database.SecureSettingsDAO;

public class NetworkSettingsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private RadioGroup rgConnectionType;
    private TextInputEditText etPrimaryHost;
    private TextInputEditText etPrimaryPort;
    private TextInputEditText etSecondaryHost;
    private TextInputEditText etSecondaryPort;
    private TextInputEditText etTimeout;
    private TextInputEditText etRetryCount;
    private SwitchMaterial switchSsl;
    private SwitchMaterial switchKeepAlive;
    private Spinner spinnerProtocol;
    private Button btnSave;
    private Button btnTestConnection;
    
    private SecureSettingsDAO settingsDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_settings);
        
        settingsDAO = new SecureSettingsDAO(this);
        
        initViews();
        setupToolbar();
        setupSpinner();
        loadCurrentSettings();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rgConnectionType = findViewById(R.id.rg_connection_type);
        etPrimaryHost = findViewById(R.id.et_primary_host);
        etPrimaryPort = findViewById(R.id.et_primary_port);
        etSecondaryHost = findViewById(R.id.et_secondary_host);
        etSecondaryPort = findViewById(R.id.et_secondary_port);
        etTimeout = findViewById(R.id.et_timeout);
        etRetryCount = findViewById(R.id.et_retry_count);
        switchSsl = findViewById(R.id.switch_ssl);
        switchKeepAlive = findViewById(R.id.switch_keep_alive);
        spinnerProtocol = findViewById(R.id.spinner_protocol);
        btnSave = findViewById(R.id.btn_save);
        btnTestConnection = findViewById(R.id.btn_test_connection);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Network Settings");
        }
    }
    
    private void setupSpinner() {
        String[] protocols = {"ISO8583", "HTTP/JSON", "TCP/IP Raw"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, protocols);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol.setAdapter(adapter);
    }
    
    private void loadCurrentSettings() {
        NetworkSettings settings = settingsDAO.getNetworkSettings();
        if (settings != null) {
            // Set connection type
            if ("ETHERNET".equals(settings.getConnectionType())) {
                rgConnectionType.check(R.id.rb_ethernet);
            } else if ("WIFI".equals(settings.getConnectionType())) {
                rgConnectionType.check(R.id.rb_wifi);
            } else if ("GPRS".equals(settings.getConnectionType())) {
                rgConnectionType.check(R.id.rb_gprs);
            }
            
            etPrimaryHost.setText(settings.getPrimaryHost());
            etPrimaryPort.setText(String.valueOf(settings.getPrimaryPort()));
            etSecondaryHost.setText(settings.getSecondaryHost());
            etSecondaryPort.setText(String.valueOf(settings.getSecondaryPort()));
            etTimeout.setText(String.valueOf(settings.getTimeout()));
            etRetryCount.setText(String.valueOf(settings.getRetryCount()));
            switchSsl.setChecked(settings.isUseSsl());
            switchKeepAlive.setChecked(settings.isKeepAlive());
            
            // Set protocol selection
            String protocol = settings.getProtocol();
            if ("ISO8583".equals(protocol)) {
                spinnerProtocol.setSelection(0);
            } else if ("HTTP/JSON".equals(protocol)) {
                spinnerProtocol.setSelection(1);
            } else if ("TCP/IP Raw".equals(protocol)) {
                spinnerProtocol.setSelection(2);
            }
        }
    }
    
    private void setupListeners() {
        rgConnectionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateConnectionTypeUI(checkedId);
            }
        });
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
        
        btnTestConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testConnection();
            }
        });
    }
    
    private void updateConnectionTypeUI(int checkedId) {
        // Update UI based on connection type
        boolean isEthernet = checkedId == R.id.rb_ethernet;
        boolean isWifi = checkedId == R.id.rb_wifi;
        boolean isGprs = checkedId == R.id.rb_gprs;
        
        // Show/hide relevant fields based on connection type
    }
    
    private void saveSettings() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Create settings object
        NetworkSettings settings = new NetworkSettings();
        
        // Get connection type
        int checkedId = rgConnectionType.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_ethernet) {
            settings.setConnectionType("ETHERNET");
        } else if (checkedId == R.id.rb_wifi) {
            settings.setConnectionType("WIFI");
        } else if (checkedId == R.id.rb_gprs) {
            settings.setConnectionType("GPRS");
        }
        
        settings.setPrimaryHost(etPrimaryHost.getText().toString().trim());
        settings.setPrimaryPort(Integer.parseInt(etPrimaryPort.getText().toString()));
        settings.setSecondaryHost(etSecondaryHost.getText().toString().trim());
        
        String secPort = etSecondaryPort.getText().toString().trim();
        settings.setSecondaryPort(secPort.isEmpty() ? 0 : Integer.parseInt(secPort));
        
        settings.setTimeout(Integer.parseInt(etTimeout.getText().toString()));
        settings.setRetryCount(Integer.parseInt(etRetryCount.getText().toString()));
        settings.setUseSsl(switchSsl.isChecked());
        settings.setKeepAlive(switchKeepAlive.isChecked());
        
        // Get selected protocol
        String[] protocols = {"ISO8583", "HTTP/JSON", "TCP/IP Raw"};
        settings.setProtocol(protocols[spinnerProtocol.getSelectedItemPosition()]);
        
        // Save to database
        if (settingsDAO.saveNetworkSettings(settings)) {
            Toast.makeText(this, "Network settings saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void testConnection() {
        // Test connection with current settings
        Toast.makeText(this, "Testing connection...", Toast.LENGTH_SHORT).show();
        
        // Simulate connection test
        btnTestConnection.setEnabled(false);
        btnTestConnection.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnTestConnection.setEnabled(true);
                Toast.makeText(NetworkSettingsActivity.this, 
                    "Connection test successful!", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }
    
    private boolean validateInputs() {
        if (etPrimaryHost.getText().toString().trim().isEmpty()) {
            etPrimaryHost.setError("Primary host is required");
            return false;
        }
        
        if (etPrimaryPort.getText().toString().trim().isEmpty()) {
            etPrimaryPort.setError("Primary port is required");
            return false;
        }
        
        try {
            int port = Integer.parseInt(etPrimaryPort.getText().toString());
            if (port < 1 || port > 65535) {
                etPrimaryPort.setError("Invalid port number");
                return false;
            }
        } catch (NumberFormatException e) {
            etPrimaryPort.setError("Invalid port number");
            return false;
        }
        
        return true;
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