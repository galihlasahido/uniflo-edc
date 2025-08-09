package id.uniflo.uniedc.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.utils.PrinterUtil;

public class TransactionSuccessActivity extends AppCompatActivity {
    
    public static final String EXTRA_TRANSACTION_TYPE = "transaction_type";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_RECIPIENT = "recipient";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_DETAILS = "details";
    
    private ImageView ivSuccess;
    private TextView tvSuccessTitle;
    private TextView tvReceiptContent;
    private Button btnDone;
    private Button btnPrintReceipt;
    
    // Store transaction data for printing
    private String transactionType;
    private String amount;
    private String recipient;
    private String transactionId;
    private String details;
    private String receiptContent = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_success_modern);
        
        initViews();
        displayTransactionInfo();
        setupListeners();
        
        // Automatically print receipt after displaying info
        autoPrintReceipt();
    }
    
    private void initViews() {
        // ivSuccess = findViewById(R.id.iv_success_icon); // Removed from layout
        tvSuccessTitle = findViewById(R.id.tv_success_title);
        tvReceiptContent = findViewById(R.id.tv_receipt_content);
        btnDone = findViewById(R.id.btn_done);
        btnPrintReceipt = findViewById(R.id.btn_print_receipt);
    }
    
    private void displayTransactionInfo() {
        Intent intent = getIntent();
        transactionType = intent.getStringExtra(EXTRA_TRANSACTION_TYPE);
        amount = intent.getStringExtra(EXTRA_AMOUNT);
        recipient = intent.getStringExtra(EXTRA_RECIPIENT);
        transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID);
        details = intent.getStringExtra(EXTRA_DETAILS);
        
        // Set title based on transaction type
        if (transactionType != null) {
            switch (transactionType) {
                case "transfer":
                    tvSuccessTitle.setText("Transfer Successful!");
                    break;
                case "withdrawal":
                    tvSuccessTitle.setText("Withdrawal Successful!");
                    break;
                case "sales":
                    tvSuccessTitle.setText("Payment Successful!");
                    break;
                default:
                    tvSuccessTitle.setText("Transaction Successful!");
            }
        }
        
        // Load and display receipt data from JSON
        loadReceiptData();
    }
    
    private void loadReceiptData() {
        try {
            // Load JSON from assets
            InputStream is = getAssets().open("json/response.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            
            // Parse JSON
            JSONObject jsonObject = new JSONObject(json);
            JSONObject data = jsonObject.getJSONObject("data");
            String screen = data.getString("screen");
            
            // Parse screen data with pipe handling
            // Multiple consecutive pipes = multiple newlines
            StringBuilder formattedReceipt = new StringBuilder();
            
            int i = 0;
            while (i < screen.length()) {
                char c = screen.charAt(i);
                
                if (c == '|') {
                    // Count consecutive pipes
                    int pipeCount = 0;
                    while (i < screen.length() && screen.charAt(i) == '|') {
                        pipeCount++;
                        i++;
                    }
                    
                    // Add newlines equal to pipe count
                    for (int j = 0; j < pipeCount; j++) {
                        formattedReceipt.append("\n");
                    }
                } else {
                    // Regular character, add to output
                    formattedReceipt.append(c);
                    i++;
                }
            }
            
            // Clean up: remove leading/trailing whitespace from each line
            // but preserve the structure
            String[] lines = formattedReceipt.toString().split("\n");
            StringBuilder cleanedReceipt = new StringBuilder();
            
            for (int j = 0; j < lines.length; j++) {
                String line = lines[j];
                // Add the line (trimmed of trailing spaces, but keep leading spaces for formatting)
                cleanedReceipt.append(line.replaceAll("\\s+$", ""));
                if (j < lines.length - 1) {
                    cleanedReceipt.append("\n");
                }
            }
            
            // Store receipt content for printing
            receiptContent = cleanedReceipt.toString();
            
            // Display the formatted receipt (left-aligned)
            tvReceiptContent.setText(receiptContent);
            
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            // Fallback display
            receiptContent = "Error loading receipt data";
            tvReceiptContent.setText(receiptContent);
        }
    }
    
    private void setupListeners() {
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to dashboard
                Intent intent = new Intent(TransactionSuccessActivity.this, BasHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        
        btnPrintReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReceiptContent();
            }
        });
    }
    
    private void autoPrintReceipt() {
        // Automatically print receipt after a short delay
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                printReceiptContent();
            }
        }, 1000); // 1000ms delay to ensure UI and JSON are fully loaded
    }
    
    private void printReceiptContent() {
        if (receiptContent == null || receiptContent.trim().isEmpty()) {
            android.widget.Toast.makeText(this, "No receipt content to print", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Print the exact receipt content from the JSON using PrinterUtil
        PrinterUtil.printRawReceipt(this, receiptContent);
    }
    
    private void printReceipt() {
        // Keep the old method for backward compatibility
        printReceiptContent();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back to transaction screens
        btnDone.performClick();
    }
    
    public static void start(Context context, String transactionType, String amount, 
                            String recipient, String transactionId, String details) {
        Intent intent = new Intent(context, TransactionSuccessActivity.class);
        intent.putExtra(EXTRA_TRANSACTION_TYPE, transactionType);
        intent.putExtra(EXTRA_AMOUNT, amount);
        intent.putExtra(EXTRA_RECIPIENT, recipient);
        intent.putExtra(EXTRA_TRANSACTION_ID, transactionId);
        intent.putExtra(EXTRA_DETAILS, details);
        context.startActivity(intent);
    }
}