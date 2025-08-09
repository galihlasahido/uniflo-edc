package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.ICardReader;

public class SalesModernActivity extends Activity {
    
    private ImageView backButton;
    private ImageView btnHistory;
    private TextView tvDisplayAmount;
    private EditText etAmount;
    
    // Quick amount buttons
    private Button btn10k, btn20k, btn50k, btn100k, btn200k, btn500k;
    
    // Payment method buttons
    private LinearLayout btnCardPayment;
    private LinearLayout btnQrisPayment;
    
    private long saleAmount = 0;
    private String selectedPaymentMethod = "";
    private ProgressDialog progressDialog;
    
    // Request codes
    private static final int REQUEST_CARD_READING = 1001;
    private static final int REQUEST_PIN_ENTRY = 1002;
    
    // Card data from reader
    private String cardNumber = "";
    private int cardTypeDetected = 0;
    
    private SDKManager sdkManager;
    
    // Payment method constants
    private static final String PAYMENT_CARD = "CARD";
    private static final String PAYMENT_QRIS = "QRIS";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_modern);
        
        // Initialize SDK Manager
        sdkManager = SDKManager.getInstance();
        
        initViews();
        setupListeners();
        updateAmountDisplay();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        btnHistory = findViewById(R.id.btn_history);
        tvDisplayAmount = findViewById(R.id.tv_display_amount);
        etAmount = findViewById(R.id.et_amount);
        
        // Quick amount buttons
        btn10k = findViewById(R.id.btn_10k);
        btn20k = findViewById(R.id.btn_20k);
        btn50k = findViewById(R.id.btn_50k);
        btn100k = findViewById(R.id.btn_100k);
        btn200k = findViewById(R.id.btn_200k);
        btn500k = findViewById(R.id.btn_500k);
        
        // Payment method buttons
        btnCardPayment = findViewById(R.id.btn_card_payment);
        btnQrisPayment = findViewById(R.id.btn_qris_payment);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        btnHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Transaction History", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to transaction history
        });
        
        // Amount input listener
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);
                    
                    String cleanString = s.toString().replaceAll("[Rp,.]", "").trim();
                    
                    if (!cleanString.isEmpty()) {
                        try {
                            saleAmount = Long.parseLong(cleanString);
                            String formatted = NumberFormat.getNumberInstance(new Locale("id", "ID"))
                                .format(saleAmount);
                            current = formatted;
                            etAmount.setText(formatted);
                            etAmount.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            saleAmount = 0;
                        }
                    } else {
                        saleAmount = 0;
                        current = "";
                    }
                    
                    etAmount.addTextChangedListener(this);
                    updateAmountDisplay();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Quick amount buttons
        btn10k.setOnClickListener(v -> setAmount(10000));
        btn20k.setOnClickListener(v -> setAmount(20000));
        btn50k.setOnClickListener(v -> setAmount(50000));
        btn100k.setOnClickListener(v -> setAmount(100000));
        btn200k.setOnClickListener(v -> setAmount(200000));
        btn500k.setOnClickListener(v -> setAmount(500000));
        
        // Payment method selection - directly process payment
        btnCardPayment.setOnClickListener(v -> {
            if (saleAmount > 0) {
                selectAndProcessPayment(PAYMENT_CARD);
            } else {
                Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
            }
        });
        
        btnQrisPayment.setOnClickListener(v -> {
            if (saleAmount > 0) {
                selectAndProcessPayment(PAYMENT_QRIS);
            } else {
                Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setAmount(long amount) {
        saleAmount = amount;
        String formatted = NumberFormat.getNumberInstance(new Locale("id", "ID"))
            .format(amount);
        etAmount.setText(formatted);
        updateAmountDisplay();
    }
    
    private void updateAmountDisplay() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(saleAmount).replace("Rp", "Rp ");
        tvDisplayAmount.setText(formattedAmount);
    }
    
    private void selectAndProcessPayment(String method) {
        selectedPaymentMethod = method;
        
        // Update UI to show selected method temporarily
        if (method.equals(PAYMENT_CARD)) {
            btnCardPayment.setBackgroundResource(R.drawable.selected_card_background);
            btnQrisPayment.setBackgroundResource(R.drawable.white_card_background);
            processCardPayment();
        } else if (method.equals(PAYMENT_QRIS)) {
            btnQrisPayment.setBackgroundResource(R.drawable.selected_card_background);
            btnCardPayment.setBackgroundResource(R.drawable.white_card_background);
            processQrisPayment();
        }
    }
    
    private void processCardPayment() {
        Intent intent = new Intent(this, CardReaderActivity.class);
        startActivityForResult(intent, REQUEST_CARD_READING);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CARD_READING) {
            if (resultCode == RESULT_OK) {
                // Card reading successful, get card data
                if (data != null) {
                    cardNumber = data.getStringExtra(CardReaderActivity.EXTRA_CARD_NUMBER);
                    String cardAtr = data.getStringExtra(CardReaderActivity.EXTRA_CARD_ATR);
                    String fullPan = data.getStringExtra(CardReaderActivity.EXTRA_FULL_PAN);
                    String track2Data = data.getStringExtra(CardReaderActivity.EXTRA_TRACK2_DATA);
                    String emvTlv = data.getStringExtra(CardReaderActivity.EXTRA_EMV_TLV);
                    boolean cardValidated = data.getBooleanExtra(CardReaderActivity.EXTRA_CARD_VALIDATED, false);
                    
                    // Show PIN entry activity for payment
                    showPinEntry();
                } else {
                    Toast.makeText(this, "Gagal membaca data kartu", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Card reading failed or cancelled
                Toast.makeText(this, "Pembacaan kartu dibatalkan", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PIN_ENTRY) {
            if (resultCode == RESULT_OK) {
                // PIN entered successfully, complete transaction
                if (data != null) {
                    String pin = data.getStringExtra("pin_entered");
                    // Process the transaction with the entered PIN
                    completeTransaction();
                } else {
                    Toast.makeText(this, "PIN tidak valid", Toast.LENGTH_SHORT).show();
                }
            } else {
                // PIN entry cancelled
                Toast.makeText(this, "PIN dibatalkan", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void processQrisPayment() {
        showProgressDialog("Generating QR Code...");
        
        // Simulate QR code generation
        new Handler().postDelayed(() -> {
            hideProgressDialog();
            showQrisDialog();
        }, 1500);
    }
    
    private void showPinEntry() {
        Intent intent = new Intent(this, SalesPinActivity.class);
        intent.putExtra(SalesPinActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(SalesPinActivity.EXTRA_AMOUNT, saleAmount);
        startActivityForResult(intent, REQUEST_PIN_ENTRY);
    }
    
    private void showQrisDialog() {
        // Create a dialog for QRIS display
        Dialog qrisDialog = new Dialog(this);
        qrisDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        qrisDialog.setContentView(R.layout.dialog_qris_payment);
        qrisDialog.setCancelable(true);
        
        if (qrisDialog.getWindow() != null) {
            qrisDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            qrisDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
        
        TextView tvAmount = qrisDialog.findViewById(R.id.tv_qris_amount);
        if (tvAmount != null) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            tvAmount.setText(formatter.format(saleAmount).replace("Rp", "Rp "));
        }
        
        // Simulate payment completion after 5 seconds
        new Handler().postDelayed(() -> {
            if (qrisDialog.isShowing()) {
                qrisDialog.dismiss();
                completeTransaction();
            }
        }, 5000);
        
        qrisDialog.show();
    }
    
    private void completeTransaction() {
        showProgressDialog(getString(R.string.transaction_processing));
        
        // Simulate transaction processing
        new Handler().postDelayed(() -> {
            hideProgressDialog();
            
            // Save transaction to SharedPreferences
            String transactionId = "SALE" + System.currentTimeMillis();
            getSharedPreferences("transactions", MODE_PRIVATE)
                .edit()
                .putString(transactionId, selectedPaymentMethod + "|" + saleAmount + "|" + new Date().toString())
                .apply();
            
            // Navigate to success screen
            TransactionSuccessActivity.start(
                this,
                "sales",
                null,  // Amount will come from JSON
                null,  // No recipient for sales
                transactionId,
                null   // Details will come from JSON
            );
            finish();
        }, 2000);
    }
    
    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }
    
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
}