package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import id.uniflo.uniedc.R;

public class BalanceResultActivity extends Activity {
    
    public static final String EXTRA_CARD_NUMBER = "card_number";
    public static final String EXTRA_BALANCE = "balance";
    public static final String EXTRA_ACCOUNT_TYPE = "account_type";
    
    private ImageView backButton;
    private ImageView successIcon;
    private TextView cardNumberText;
    private TextView balanceAmountText;
    private TextView accountTypeText;
    private TextView transactionDateText;
    private TextView transactionTimeText;
    private Button printButton;
    private Button doneButton;
    
    private String cardNumber;
    private long balance;
    private String accountType;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_result);
        
        // Get data from intent
        Intent intent = getIntent();
        cardNumber = intent.getStringExtra(EXTRA_CARD_NUMBER);
        balance = intent.getLongExtra(EXTRA_BALANCE, -1);
        accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE);
        
        // Check if we have valid card data
        if (cardNumber == null || cardNumber.isEmpty()) {
            // No card data - show error and close
            Toast.makeText(this, "Error: No card data available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Check if balance was provided
        if (balance == -1) {
            // No balance provided - show error
            Toast.makeText(this, "Error: Unable to retrieve balance", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Default account type if not provided
        if (accountType == null) {
            accountType = "Tabungan";
        }
        
        initializeViews();
        setupListeners();
        displayBalanceInfo();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        successIcon = findViewById(R.id.success_icon);
        cardNumberText = findViewById(R.id.card_number_text);
        balanceAmountText = findViewById(R.id.balance_amount_text);
        accountTypeText = findViewById(R.id.account_type_text);
        transactionDateText = findViewById(R.id.transaction_date_text);
        transactionTimeText = findViewById(R.id.transaction_time_text);
        printButton = findViewById(R.id.print_button);
        doneButton = findViewById(R.id.done_button);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> navigateToHome());
        
        printButton.setOnClickListener(v -> printReceipt());
        
        doneButton.setOnClickListener(v -> navigateToHome());
    }
    
    private void displayBalanceInfo() {
        // Display masked card number
        cardNumberText.setText(maskCardNumber(cardNumber));
        
        // Format and display balance
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedBalance = formatter.format(balance);
        balanceAmountText.setText(formattedBalance);
        
        // Display account type
        accountTypeText.setText(accountType);
        
        // Display current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        
        transactionDateText.setText(dateFormat.format(now));
        transactionTimeText.setText(timeFormat.format(now));
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 16) {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return "**** **** **** ****";
    }
    
    private void printReceipt() {
        // TODO: Implement actual printing functionality
        Toast.makeText(this, "Mencetak struk...", Toast.LENGTH_SHORT).show();
        
        // For demo purposes, show a success message after a delay
        printButton.setEnabled(false);
        printButton.postDelayed(() -> {
            Toast.makeText(this, "Struk berhasil dicetak", Toast.LENGTH_SHORT).show();
            printButton.setEnabled(true);
        }, 2000);
    }
    
    private void navigateToHome() {
        // Navigate back to dashboard
        Intent intent = new Intent(this, BasHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        navigateToHome();
    }
}