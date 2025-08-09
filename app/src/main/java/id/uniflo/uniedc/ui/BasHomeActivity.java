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

public class BasHomeActivity extends Activity {
    
    private LinearLayout btnMenu;
    // PIN Management menu items
    private View menuCreatePin, menuChangePin, menuVerificationPin;
    // Transaction menu items
    private View menuSales, menuBalanceInquiry, menuTarikTunai, menuTransferOnUs, menuSettings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_home);
        
        initViews();
        setupMenuIcons();
        setupClickListeners();
    }
    
    private void initViews() {
        btnMenu = findViewById(R.id.btn_menu);
        
        // Transaction menu items
        menuSales = findViewById(R.id.menu_sales);
        menuBalanceInquiry = findViewById(R.id.menu_balance_inquiry);
        menuTarikTunai = findViewById(R.id.menu_tarik_tunai);
        menuTransferOnUs = findViewById(R.id.menu_transfer_on_us);
        
        // PIN Management
        menuCreatePin = findViewById(R.id.menu_create_pin);
        menuChangePin = findViewById(R.id.menu_change_pin);
        menuVerificationPin = findViewById(R.id.menu_verification_pin);
        
        // Settings
        menuSettings = findViewById(R.id.menu_settings);
    }
    
    private void setupMenuIcons() {
        // Transaction menu items with gradients
        setGridMenuItem(menuSales, "Sales", R.drawable.ic_cart, R.drawable.menu_sales);
        setGridMenuItem(menuBalanceInquiry, "Balance\nInquiry", R.drawable.ic_balance, R.drawable.menu_balance);
        setGridMenuItem(menuTarikTunai, getString(R.string.menu_cash_withdrawal), R.drawable.ic_tarik_tunai, R.drawable.menu_withdrawal);
        setGridMenuItem(menuTransferOnUs, getString(R.string.menu_transfer), R.drawable.ic_transfer_bank, R.drawable.menu_transfer_onus);
        
        // PIN Management with gradients
        setGridMenuItem(menuCreatePin, getString(R.string.menu_create_pin), R.drawable.ic_pin_create, R.drawable.menu_create_pin);
        setGridMenuItem(menuChangePin, getString(R.string.menu_change_pin), R.drawable.ic_pin_change, R.drawable.menu_change_pins);
        setGridMenuItem(menuVerificationPin, getString(R.string.menu_verify_pin), R.drawable.ic_pin_verify, R.drawable.menu_verification_pin);
        
        // Settings with gradient
        setGridMenuItem(menuSettings, getString(R.string.menu_settings), R.drawable.ic_settings, R.drawable.menu_settings);
    }
    
    private void setGridMenuItem(View menuItem, String title, int iconResId, int gradientResId) {
        if (menuItem != null) {
            // Set icon
            ImageView icon = menuItem.findViewById(R.id.menu_icon);
            if (icon != null) {
                icon.setImageResource(iconResId);
            }
            
            // Set gradient background for icon container
            View iconContainer = menuItem.findViewById(R.id.icon_container);
            if (iconContainer != null) {
                iconContainer.setBackgroundResource(gradientResId);
            }
            
            // Set title
            TextView titleView = menuItem.findViewById(R.id.menu_title);
            if (titleView != null) {
                titleView.setText(title);
            }
        }
    }
    
    private void setupClickListeners() {
        btnMenu.setOnClickListener(v -> {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
        });
        
        // Transaction menu items
        menuSales.setOnClickListener(v -> handleMenuClick("Sales"));
        menuBalanceInquiry.setOnClickListener(v -> handleMenuClick("Balance Inquiry"));
        menuTarikTunai.setOnClickListener(v -> handleMenuClick("Tarik Tunai"));
        menuTransferOnUs.setOnClickListener(v -> handleMenuClick("Transfer On Us"));
        
        // PIN Management
        menuCreatePin.setOnClickListener(v -> handleMenuClick("Create PIN"));
        menuChangePin.setOnClickListener(v -> handleMenuClick("Change PIN"));
        menuVerificationPin.setOnClickListener(v -> handleMenuClick("Verification PIN"));
        
        // Settings
        menuSettings.setOnClickListener(v -> handleMenuClick("Settings"));
    }
    
    private void handleMenuClick(String menuName) {
        Toast.makeText(this, menuName + " clicked", Toast.LENGTH_SHORT).show();
        
        // Handle specific menu navigation
        switch (menuName) {
            case "Create PIN":
                // Navigate to modern create PIN
                Intent createPinIntent = new Intent(this, CreatePinModernActivity.class);
                startActivity(createPinIntent);
                break;
            case "Change PIN":
                // Navigate to modern change PIN
                Intent changePinIntent = new Intent(this, ChangePinModernActivity.class);
                startActivity(changePinIntent);
                break;
            case "Verification PIN":
                // Navigate to modern PIN verification
                Intent verifyPinIntent = new Intent(this, VerifyPinModernActivity.class);
                startActivity(verifyPinIntent);
                break;
            case "Tarik Tunai":
                // Navigate to modern cash withdrawal
                Intent withdrawalIntent = new Intent(this, CashWithdrawalModernActivity.class);
                startActivity(withdrawalIntent);
                break;
            case "Transfer On Us":
                // Navigate to modern transfer
                Intent transferIntent = new Intent(this, TransferModernActivity.class);
                startActivity(transferIntent);
                break;
            case "Settings":
                // Navigate to settings
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case "Sales":
                // Navigate to modern sales page
                Intent salesIntent = new Intent(this, SalesModernActivity.class);
                startActivity(salesIntent);
                break;
            case "Balance Inquiry":
                // Navigate to modern balance inquiry
                Intent balanceIntent = new Intent(this, BalanceInquiryModernActivity.class);
                startActivity(balanceIntent);
                break;
            // Add more cases as needed
        }
    }
}