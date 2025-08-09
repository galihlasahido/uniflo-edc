package id.uniflo.uniedc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import id.uniflo.uniedc.R;

public class DashboardModernActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextView tvGreeting;
    private TextView tvDate;
    private LinearLayout cardTransfer;
    private LinearLayout cardWithdraw;
    private LinearLayout cardPurchase;
    private LinearLayout cardBalanceInquiry;
    private LinearLayout cardSettings;
    private LinearLayout cardLogout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_modern);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        updateDateDisplay();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvGreeting = findViewById(R.id.tv_greeting);
        tvDate = findViewById(R.id.tv_date);
        cardTransfer = findViewById(R.id.card_transfer);
        cardWithdraw = findViewById(R.id.card_withdraw);
        cardPurchase = findViewById(R.id.card_purchase);
        cardBalanceInquiry = findViewById(R.id.card_balance_inquiry);
        cardSettings = findViewById(R.id.card_settings);
        cardLogout = findViewById(R.id.card_logout);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Dashboard");
        }
    }
    
    private void setupClickListeners() {
        cardTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardModernActivity.this, TransferActivityEnhanced.class);
                startActivity(intent);
            }
        });
        
        cardWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardModernActivity.this, "Withdrawal coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        cardPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardModernActivity.this, PurchaseActivityEnhanced.class);
                startActivity(intent);
            }
        });
        
        cardBalanceInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardModernActivity.this, BalanceInquiryActivityBasic.class);
                startActivity(intent);
            }
        });
        
        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardModernActivity.this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout
                finish();
            }
        });
    }
    
    private void updateDateDisplay() {
        // Update date display
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());
        tvDate.setText(currentDate);
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