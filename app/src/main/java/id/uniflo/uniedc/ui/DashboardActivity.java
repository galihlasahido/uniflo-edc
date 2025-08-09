package id.uniflo.uniedc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.uniflo.uniedc.R;

public class DashboardActivity extends AppCompatActivity {
    
    private TextView tvGreeting;
    private TextView tvDate;
    private LinearLayout cardTransfer;
    private LinearLayout cardWithdraw;
    private LinearLayout cardPurchase;
    private LinearLayout cardBalanceInquiry;
    private LinearLayout cardSettings;
    private LinearLayout cardLogout;
    private ImageView ivProfile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_modern);
        
        initViews();
        setupData();
        setupListeners();
    }
    
    private void initViews() {
        tvGreeting = findViewById(R.id.tv_greeting);
        tvDate = findViewById(R.id.tv_date);
        cardTransfer = findViewById(R.id.card_transfer);
        cardWithdraw = findViewById(R.id.card_withdraw);
        cardPurchase = findViewById(R.id.card_purchase);
        cardBalanceInquiry = findViewById(R.id.card_balance_inquiry);
        cardSettings = findViewById(R.id.card_settings);
        cardLogout = findViewById(R.id.card_logout);
        ivProfile = findViewById(R.id.iv_profile);
    }
    
    private void setupData() {
        // Set greeting
        tvGreeting.setText("Welcome back, Admin!");
        
        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
        
    }
    
    private void setupListeners() {
        cardTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, TransferActivityEnhanced.class));
            }
        });
        
        cardWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, WithdrawActivity.class));
            }
        });
        
        cardPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, PurchaseActivityEnhanced.class));
            }
        });
        
        cardBalanceInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, BalanceInquiryActivityBasic.class));
            }
        });
        
        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
            }
        });
        
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}