package id.uniflo.uniedc.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.widget.AmountEditText;
import id.uniflo.uniedc.database.TransactionLimits;
import id.uniflo.uniedc.database.SecureSettingsDAO;

public class TransactionLimitsActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    
    // Purchase Limits
    private SwitchMaterial switchPurchaseLimit;
    private AmountEditText etPurchaseMin;
    private AmountEditText etPurchaseMax;
    
    // Withdrawal Limits
    private SwitchMaterial switchWithdrawalLimit;
    private AmountEditText etWithdrawalMin;
    private AmountEditText etWithdrawalMax;
    
    // Transfer Limits
    private SwitchMaterial switchTransferLimit;
    private AmountEditText etTransferMin;
    private AmountEditText etTransferMax;
    
    // Refund Limits
    private SwitchMaterial switchRefundLimit;
    private AmountEditText etRefundMax;
    
    // Cash Back Limits
    private SwitchMaterial switchCashBackLimit;
    private AmountEditText etCashBackMax;
    
    // Daily Limits
    private SwitchMaterial switchDailyLimit;
    private AmountEditText etDailyTransactionLimit;
    private AmountEditText etDailyAmountLimit;
    
    private Button btnSave;
    
    private SecureSettingsDAO settingsDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_limits);
        
        settingsDAO = new SecureSettingsDAO(this);
        
        initViews();
        setupToolbar();
        loadCurrentSettings();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        
        // Purchase
        switchPurchaseLimit = findViewById(R.id.switch_purchase_limit);
        etPurchaseMin = findViewById(R.id.et_purchase_min);
        etPurchaseMax = findViewById(R.id.et_purchase_max);
        
        // Withdrawal
        switchWithdrawalLimit = findViewById(R.id.switch_withdrawal_limit);
        etWithdrawalMin = findViewById(R.id.et_withdrawal_min);
        etWithdrawalMax = findViewById(R.id.et_withdrawal_max);
        
        // Transfer
        switchTransferLimit = findViewById(R.id.switch_transfer_limit);
        etTransferMin = findViewById(R.id.et_transfer_min);
        etTransferMax = findViewById(R.id.et_transfer_max);
        
        // Refund
        switchRefundLimit = findViewById(R.id.switch_refund_limit);
        etRefundMax = findViewById(R.id.et_refund_max);
        
        // Cash Back
        switchCashBackLimit = findViewById(R.id.switch_cash_back_limit);
        etCashBackMax = findViewById(R.id.et_cash_back_max);
        
        // Daily
        switchDailyLimit = findViewById(R.id.switch_daily_limit);
        etDailyTransactionLimit = findViewById(R.id.et_daily_transaction_limit);
        etDailyAmountLimit = findViewById(R.id.et_daily_amount_limit);
        
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transaction Limits");
        }
    }
    
    private void loadCurrentSettings() {
        TransactionLimits limits = settingsDAO.getTransactionLimits();
        if (limits != null) {
            // Purchase limits
            switchPurchaseLimit.setChecked(limits.isPurchaseLimitEnabled());
            etPurchaseMin.setAmount(limits.getPurchaseMin());
            etPurchaseMax.setAmount(limits.getPurchaseMax());
            etPurchaseMin.setEnabled(limits.isPurchaseLimitEnabled());
            etPurchaseMax.setEnabled(limits.isPurchaseLimitEnabled());
            
            // Withdrawal limits
            switchWithdrawalLimit.setChecked(limits.isWithdrawalLimitEnabled());
            etWithdrawalMin.setAmount(limits.getWithdrawalMin());
            etWithdrawalMax.setAmount(limits.getWithdrawalMax());
            etWithdrawalMin.setEnabled(limits.isWithdrawalLimitEnabled());
            etWithdrawalMax.setEnabled(limits.isWithdrawalLimitEnabled());
            
            // Transfer limits
            switchTransferLimit.setChecked(limits.isTransferLimitEnabled());
            etTransferMin.setAmount(limits.getTransferMin());
            etTransferMax.setAmount(limits.getTransferMax());
            etTransferMin.setEnabled(limits.isTransferLimitEnabled());
            etTransferMax.setEnabled(limits.isTransferLimitEnabled());
            
            // Refund limit
            switchRefundLimit.setChecked(limits.isRefundLimitEnabled());
            etRefundMax.setAmount(limits.getRefundMax());
            etRefundMax.setEnabled(limits.isRefundLimitEnabled());
            
            // Cash back limit
            switchCashBackLimit.setChecked(limits.isCashBackLimitEnabled());
            etCashBackMax.setAmount(limits.getCashBackMax());
            etCashBackMax.setEnabled(limits.isCashBackLimitEnabled());
            
            // Daily limits
            switchDailyLimit.setChecked(limits.isDailyLimitEnabled());
            etDailyTransactionLimit.setText(String.valueOf(limits.getDailyTransactionLimit()));
            etDailyAmountLimit.setAmount(limits.getDailyAmountLimit());
            etDailyTransactionLimit.setEnabled(limits.isDailyLimitEnabled());
            etDailyAmountLimit.setEnabled(limits.isDailyLimitEnabled());
        }
    }
    
    private void setupListeners() {
        // Enable/disable fields based on switches
        switchPurchaseLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etPurchaseMin.setEnabled(isChecked);
            etPurchaseMax.setEnabled(isChecked);
        });
        
        switchWithdrawalLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etWithdrawalMin.setEnabled(isChecked);
            etWithdrawalMax.setEnabled(isChecked);
        });
        
        switchTransferLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etTransferMin.setEnabled(isChecked);
            etTransferMax.setEnabled(isChecked);
        });
        
        switchRefundLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etRefundMax.setEnabled(isChecked);
        });
        
        switchCashBackLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCashBackMax.setEnabled(isChecked);
        });
        
        switchDailyLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etDailyTransactionLimit.setEnabled(isChecked);
            etDailyAmountLimit.setEnabled(isChecked);
        });
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }
    
    private void saveSettings() {
        // Validate limits
        if (!validateLimits()) {
            return;
        }
        
        // Create limits object
        TransactionLimits limits = new TransactionLimits();
        
        // Purchase limits
        limits.setPurchaseLimitEnabled(switchPurchaseLimit.isChecked());
        limits.setPurchaseMin(etPurchaseMin.getAmount());
        limits.setPurchaseMax(etPurchaseMax.getAmount());
        
        // Withdrawal limits
        limits.setWithdrawalLimitEnabled(switchWithdrawalLimit.isChecked());
        limits.setWithdrawalMin(etWithdrawalMin.getAmount());
        limits.setWithdrawalMax(etWithdrawalMax.getAmount());
        
        // Transfer limits
        limits.setTransferLimitEnabled(switchTransferLimit.isChecked());
        limits.setTransferMin(etTransferMin.getAmount());
        limits.setTransferMax(etTransferMax.getAmount());
        
        // Refund limit
        limits.setRefundLimitEnabled(switchRefundLimit.isChecked());
        limits.setRefundMax(etRefundMax.getAmount());
        
        // Cash back limit
        limits.setCashBackLimitEnabled(switchCashBackLimit.isChecked());
        limits.setCashBackMax(etCashBackMax.getAmount());
        
        // Daily limits
        limits.setDailyLimitEnabled(switchDailyLimit.isChecked());
        limits.setDailyTransactionLimit(Integer.parseInt(etDailyTransactionLimit.getText().toString()));
        limits.setDailyAmountLimit(etDailyAmountLimit.getAmount());
        
        // Save to database
        if (settingsDAO.saveTransactionLimits(limits)) {
            Toast.makeText(this, "Transaction limits saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save limits", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validateLimits() {
        // Purchase limits validation
        if (switchPurchaseLimit.isChecked()) {
            if (!etPurchaseMin.hasValidAmount() || !etPurchaseMax.hasValidAmount()) {
                Toast.makeText(this, "Please enter valid purchase limits", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (etPurchaseMin.getAmount() >= etPurchaseMax.getAmount()) {
                Toast.makeText(this, "Purchase minimum must be less than maximum", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        // Withdrawal limits validation
        if (switchWithdrawalLimit.isChecked()) {
            if (!etWithdrawalMin.hasValidAmount() || !etWithdrawalMax.hasValidAmount()) {
                Toast.makeText(this, "Please enter valid withdrawal limits", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (etWithdrawalMin.getAmount() >= etWithdrawalMax.getAmount()) {
                Toast.makeText(this, "Withdrawal minimum must be less than maximum", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            // Check if minimum is multiple of 50,000 for ATM compatibility
            if (etWithdrawalMin.getAmount() % 50000 != 0) {
                Toast.makeText(this, "Withdrawal minimum must be multiple of Rp 50,000", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        // Transfer limits validation
        if (switchTransferLimit.isChecked()) {
            if (!etTransferMin.hasValidAmount() || !etTransferMax.hasValidAmount()) {
                Toast.makeText(this, "Please enter valid transfer limits", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (etTransferMin.getAmount() >= etTransferMax.getAmount()) {
                Toast.makeText(this, "Transfer minimum must be less than maximum", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        // Daily transaction count validation
        if (switchDailyLimit.isChecked()) {
            String transactionCount = etDailyTransactionLimit.getText().toString();
            if (transactionCount.isEmpty()) {
                Toast.makeText(this, "Please enter daily transaction limit", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            try {
                int count = Integer.parseInt(transactionCount);
                if (count < 1 || count > 1000) {
                    Toast.makeText(this, "Daily transaction count must be between 1 and 1000", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid transaction count", Toast.LENGTH_SHORT).show();
                return false;
            }
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