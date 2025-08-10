package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.utils.PrinterUtil;

public class TransferSuccessActivity extends Activity {
    
    public static final String EXTRA_CARD_NUMBER = "card_number";
    public static final String EXTRA_BANK_NAME = "bank_name";
    public static final String EXTRA_ACCOUNT_NUMBER = "account_number";
    public static final String EXTRA_ACCOUNT_HOLDER_NAME = "account_holder_name";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_NOTES = "notes";
    
    private ImageView backButton;
    private ImageView successIcon;
    private TextView recipientNameText;
    private TextView bankAccountText;
    private TextView amountText;
    private TextView sourceCardText;
    private TextView transactionDateText;
    private TextView transactionTimeText;
    private TextView notesText;
    private LinearLayout notesSection;
    private Button printButton;
    private Button doneButton;
    
    private String cardNumber;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private long amount;
    private String notes;
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_success);
        
        // Get data from intent
        Intent intent = getIntent();
        cardNumber = intent.getStringExtra(EXTRA_CARD_NUMBER);
        bankName = intent.getStringExtra(EXTRA_BANK_NAME);
        accountNumber = intent.getStringExtra(EXTRA_ACCOUNT_NUMBER);
        accountHolderName = intent.getStringExtra(EXTRA_ACCOUNT_HOLDER_NAME);
        amount = intent.getLongExtra(EXTRA_AMOUNT, 0);
        notes = intent.getStringExtra(EXTRA_NOTES);
        
        // Check if we have valid data
        if (cardNumber == null || bankName == null || accountNumber == null || 
            accountHolderName == null || amount <= 0) {
            Toast.makeText(this, "Error: Data transfer tidak lengkap", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        initializeViews();
        setupListeners();
        displayTransferInfo();
        
        // Automatically print receipt after 2 seconds
        handler.postDelayed(this::automaticPrint, 2000);
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        successIcon = findViewById(R.id.success_icon);
        recipientNameText = findViewById(R.id.recipient_name_text);
        bankAccountText = findViewById(R.id.bank_account_text);
        amountText = findViewById(R.id.amount_text);
        sourceCardText = findViewById(R.id.source_card_text);
        transactionDateText = findViewById(R.id.transaction_date_text);
        transactionTimeText = findViewById(R.id.transaction_time_text);
        notesText = findViewById(R.id.notes_text);
        notesSection = findViewById(R.id.notes_section);
        printButton = findViewById(R.id.print_button);
        doneButton = findViewById(R.id.done_button);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> navigateToHome());
        
        printButton.setOnClickListener(v -> printReceipt());
        
        doneButton.setOnClickListener(v -> navigateToHome());
    }
    
    private void displayTransferInfo() {
        // Display recipient info
        recipientNameText.setText(accountHolderName);
        bankAccountText.setText(bankName + " - " + accountNumber);
        
        // Format and display amount
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(amount);
        amountText.setText(formattedAmount);
        
        // Display source card (masked)
        sourceCardText.setText(maskCardNumber(cardNumber));
        
        // Display current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        
        transactionDateText.setText(dateFormat.format(now));
        transactionTimeText.setText(timeFormat.format(now));
        
        // Display notes if available
        if (notes != null && !notes.trim().isEmpty()) {
            notesSection.setVisibility(View.VISIBLE);
            notesText.setText(notes);
        } else {
            notesSection.setVisibility(View.GONE);
        }
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 16) {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return "**** **** **** ****";
    }
    
    private void automaticPrint() {
        // Automatically print receipt on page load
        Toast.makeText(this, "Mencetak struk otomatis...", Toast.LENGTH_SHORT).show();
        performPrint(false); // false = automatic print (no progress dialog)
    }
    
    private void printReceipt() {
        // Manual print when user clicks print button
        performPrint(true); // true = manual print (show progress dialog)
    }
    
    private void performPrint(boolean showProgress) {
        ProgressDialog progressDialog = null;
        
        if (showProgress) {
            // Show progress dialog for manual print
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Mencetak struk...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
            // Disable print button temporarily
            printButton.setEnabled(false);
        }
        
        final ProgressDialog finalProgressDialog = progressDialog;
        
        // Print using PrinterUtil with a delay to show progress
        handler.postDelayed(() -> {
            if (finalProgressDialog != null && finalProgressDialog.isShowing()) {
                finalProgressDialog.dismiss();
            }
            
            // Use PrinterUtil for actual printing
            String receiptContent = createReceiptContent();
            PrinterUtil.printRawReceipt(this, receiptContent);
            
            // Re-enable print button
            if (showProgress) {
                printButton.setEnabled(true);
            }
        }, showProgress ? 1500 : 500);
    }
    
    private String createReceiptContent() {
        StringBuilder receipt = new StringBuilder();
        
        // Transaction Type Header
        receipt.append("STRUK TRANSFER ANTAR BANK\n\n");
        
        // Date and Time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        
        receipt.append("Tanggal: ").append(dateFormat.format(now)).append("\n");
        receipt.append("Waktu  : ").append(timeFormat.format(now)).append("\n\n");
        
        receipt.append("--------------------------------\n");
        
        // Transfer details
        receipt.append("DARI\n");
        receipt.append("Kartu     : ").append(maskCardNumber(cardNumber)).append("\n\n");
        
        receipt.append("KEPADA\n");
        receipt.append("Nama      : ").append(accountHolderName).append("\n");
        receipt.append("Bank      : ").append(bankName).append("\n");
        receipt.append("Rekening  : ").append(accountNumber).append("\n");
        receipt.append("Jumlah    : ").append(formatCurrency(amount)).append("\n\n");
        
        if (notes != null && !notes.trim().isEmpty()) {
            receipt.append("Catatan   : ").append(notes).append("\n\n");
        }
        
        receipt.append("--------------------------------\n");
        
        // Status
        receipt.append("STATUS    : BERHASIL\n\n");
        
        // Instructions
        receipt.append("Transfer berhasil diproses.\n");
        receipt.append("Dana akan diterima dalam\n");
        receipt.append("1-3 menit.\n\n");
        
        // Footer
        receipt.append("Terima kasih telah menggunakan\n");
        receipt.append("layanan transfer kami\n");
        
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