package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import id.uniflo.uniedc.R;

public class TransferValidationActivity extends Activity {
    
    public static final String EXTRA_CARD_NUMBER = "card_number";
    public static final String EXTRA_BANK_NAME = "bank_name";
    public static final String EXTRA_ACCOUNT_NUMBER = "account_number";
    public static final String EXTRA_ACCOUNT_HOLDER_NAME = "account_holder_name";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_NOTES = "notes";
    
    private ImageView backButton;
    private TextView bankNameText;
    private TextView accountNumberText;
    private TextView accountHolderNameText;
    private TextView amountText;
    private TextView notesText;
    private TextView sourceCardText;
    private LinearLayout notesSection;
    private Button btnEdit;
    private Button btnConfirm;
    
    // Transfer data
    private String cardNumber;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private long amount;
    private String notes;
    
    // Request code
    private static final int REQUEST_PIN_ENTRY = 1001;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_validation);
        
        // Get transfer data from intent
        Intent intent = getIntent();
        cardNumber = intent.getStringExtra(EXTRA_CARD_NUMBER);
        bankName = intent.getStringExtra(EXTRA_BANK_NAME);
        accountNumber = intent.getStringExtra(EXTRA_ACCOUNT_NUMBER);
        accountHolderName = intent.getStringExtra(EXTRA_ACCOUNT_HOLDER_NAME);
        amount = intent.getLongExtra(EXTRA_AMOUNT, 0);
        notes = intent.getStringExtra(EXTRA_NOTES);
        
        // Validate required data
        if (cardNumber == null || bankName == null || accountNumber == null || 
            accountHolderName == null || amount <= 0) {
            Toast.makeText(this, "Error: Data transfer tidak lengkap", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        initViews();
        setupListeners();
        displayTransferData();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        bankNameText = findViewById(R.id.bank_name_text);
        accountNumberText = findViewById(R.id.account_number_text);
        accountHolderNameText = findViewById(R.id.account_holder_name_text);
        amountText = findViewById(R.id.amount_text);
        notesText = findViewById(R.id.notes_text);
        sourceCardText = findViewById(R.id.source_card_text);
        notesSection = findViewById(R.id.notes_section);
        btnEdit = findViewById(R.id.btn_edit);
        btnConfirm = findViewById(R.id.btn_confirm);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        btnEdit.setOnClickListener(v -> goBackToEdit());
        
        btnConfirm.setOnClickListener(v -> proceedToPinEntry());
    }
    
    private void displayTransferData() {
        // Display destination bank info
        bankNameText.setText(bankName);
        accountNumberText.setText(accountNumber);
        accountHolderNameText.setText(accountHolderName);
        
        // Format and display amount
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(amount);
        amountText.setText(formattedAmount);
        
        // Display source card (masked)
        sourceCardText.setText(maskCardNumber(cardNumber));
        
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
    
    private void goBackToEdit() {
        // Go back to transfer form to edit data
        finish();
    }
    
    private void proceedToPinEntry() {
        // Launch PIN entry for transfer confirmation
        Intent intent = new Intent(this, SalesPinActivity.class);
        intent.putExtra(SalesPinActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(SalesPinActivity.EXTRA_AMOUNT, amount);
        intent.putExtra(SalesPinActivity.EXTRA_TRANSACTION_TYPE, "transfer");
        intent.putExtra(SalesPinActivity.EXTRA_PIN_TITLE, "Transfer Antar Bank");
        intent.putExtra(SalesPinActivity.EXTRA_PIN_SUBTITLE, "Masukkan PIN untuk konfirmasi transfer");
        startActivityForResult(intent, REQUEST_PIN_ENTRY);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_PIN_ENTRY) {
            if (resultCode == RESULT_OK) {
                // PIN entered successfully - proceed to success page
                if (data != null) {
                    String pin = data.getStringExtra("pin_entered");
                    proceedToSuccess();
                } else {
                    Toast.makeText(this, "PIN tidak valid", Toast.LENGTH_SHORT).show();
                }
            } else {
                // PIN entry cancelled
                Toast.makeText(this, "PIN dibatalkan", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void proceedToSuccess() {
        // Navigate to transfer success page
        Intent intent = new Intent(this, TransferSuccessActivity.class);
        intent.putExtra(TransferSuccessActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(TransferSuccessActivity.EXTRA_BANK_NAME, bankName);
        intent.putExtra(TransferSuccessActivity.EXTRA_ACCOUNT_NUMBER, accountNumber);
        intent.putExtra(TransferSuccessActivity.EXTRA_ACCOUNT_HOLDER_NAME, accountHolderName);
        intent.putExtra(TransferSuccessActivity.EXTRA_AMOUNT, amount);
        intent.putExtra(TransferSuccessActivity.EXTRA_NOTES, notes);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Allow going back to edit transfer data
        goBackToEdit();
    }
}