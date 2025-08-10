package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.utils.PrinterUtil;

public class WithdrawalResultActivity extends Activity {
    
    public static final String EXTRA_CARD_NUMBER = "card_number";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_ACCOUNT_TYPE = "account_type";
    
    private ImageView backButton;
    private ImageView successIcon;
    private TextView cardNumberText;
    private TextView amountText;
    private TextView accountTypeText;
    private TextView transactionDateText;
    private TextView transactionTimeText;
    private Button printButton;
    private Button doneButton;
    
    private String cardNumber;
    private long amount;
    private String accountType;
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal_result);
        
        // Get data from intent
        Intent intent = getIntent();
        cardNumber = intent.getStringExtra(EXTRA_CARD_NUMBER);
        amount = intent.getLongExtra(EXTRA_AMOUNT, 0);
        accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE);
        
        // Check if we have valid data
        if (cardNumber == null || cardNumber.isEmpty()) {
            Toast.makeText(this, "Error: No card data available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        if (amount <= 0) {
            Toast.makeText(this, "Error: Invalid withdrawal amount", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Default account type if not provided
        if (accountType == null) {
            accountType = "Tabungan";
        }
        
        initializeViews();
        setupListeners();
        displayWithdrawalInfo();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        successIcon = findViewById(R.id.success_icon);
        cardNumberText = findViewById(R.id.card_number_text);
        amountText = findViewById(R.id.amount_text);
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
    
    private void displayWithdrawalInfo() {
        // Display masked card number
        cardNumberText.setText(maskCardNumber(cardNumber));
        
        // Format and display amount
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(amount);
        amountText.setText(formattedAmount);
        
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
        // Show progress dialog for printing
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mencetak struk...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Disable print button to prevent multiple prints
        printButton.setEnabled(false);
        
        // Print using PrinterUtil with a delay to show progress
        handler.postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            
            // Use PrinterUtil for actual printing
            String receiptContent = createReceiptContent();
            PrinterUtil.printRawReceipt(this, receiptContent);
            
            // Re-enable print button
            printButton.setEnabled(true);
        }, 1500);
    }
    
    // Method removed - using PrinterUtil instead
    
    private String createReceiptContent() {
        StringBuilder receipt = new StringBuilder();
        
        // Transaction Type Header
        receipt.append("STRUK TARIK TUNAI\n\n");
        
        // Date and Time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        
        receipt.append("Tanggal: ").append(dateFormat.format(now)).append("\n");
        receipt.append("Waktu  : ").append(timeFormat.format(now)).append("\n\n");
        
        receipt.append("--------------------------------\n");
        
        // Transaction details
        receipt.append("Kartu     : ").append(maskCardNumber(cardNumber)).append("\n");
        receipt.append("Jenis     : ").append(accountType).append("\n");
        receipt.append("Jumlah    : ").append(formatCurrency(amount)).append("\n\n");
        
        receipt.append("--------------------------------\n");
        
        // Status
        receipt.append("STATUS    : BERHASIL\n\n");
        
        // Instructions
        receipt.append("Silakan ambil uang Anda dari\n");
        receipt.append("slot pengeluaran uang\n\n");
        
        // Footer
        receipt.append("Terima kasih telah menggunakan\n");
        receipt.append("layanan kami\n");
        
        return receipt.toString();
    }
    
    private String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(amount);
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