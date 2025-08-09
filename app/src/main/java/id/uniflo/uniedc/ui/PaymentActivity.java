package id.uniflo.uniedc.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import id.uniflo.uniedc.R;

public class PaymentActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private CardView cardSale;
    private CardView cardVoid;
    private CardView cardRefund;
    private CardView cardSettlement;
    private CardView cardPreAuth;
    private CardView cardBalance;
    private CardView cardReports;
    private CardView cardSettings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        
        initViews();
        setupToolbar();
        setupClickListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardSale = findViewById(R.id.card_sale);
        cardVoid = findViewById(R.id.card_void);
        cardRefund = findViewById(R.id.card_refund);
        cardSettlement = findViewById(R.id.card_settlement);
        cardPreAuth = findViewById(R.id.card_pre_auth);
        cardBalance = findViewById(R.id.card_balance);
        cardReports = findViewById(R.id.card_reports);
        cardSettings = findViewById(R.id.card_settings);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }
    }
    
    private void setupClickListeners() {
        cardSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to sale transaction
            }
        });
        
        cardVoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to void transaction
            }
        });
        
        cardRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to refund transaction
            }
        });
        
        cardSettlement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to settlement
            }
        });
        
        cardPreAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to pre-authorization
            }
        });
        
        cardBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to balance inquiry
            }
        });
        
        cardReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to reports
            }
        });
        
        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to settings
            }
        });
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