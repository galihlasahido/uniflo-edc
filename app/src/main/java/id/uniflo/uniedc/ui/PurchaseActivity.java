package id.uniflo.uniedc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Locale;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.widget.AmountEditText;

public class PurchaseActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextInputLayout tilAmount;
    private AmountEditText etAmount;
    private Button btnQris;
    private Button btnScanCard;
    private TextView tvPaymentMethod;
    
    private String selectedPaymentMethod = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        
        initViews();
        setupToolbar();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilAmount = findViewById(R.id.til_amount);
        etAmount = findViewById(R.id.et_amount);
        btnQris = findViewById(R.id.btn_qris);
        btnScanCard = findViewById(R.id.btn_scan_card);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sales");
        }
    }
    
    private void setupListeners() {
        btnQris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSalesWithQris();
            }
        });
        
        btnScanCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSalesWithCard();
            }
        });
    }
    
    private void processSalesWithQris() {
        // Clear errors
        tilAmount.setError(null);
        
        // Validate using AmountEditText method
        if (!etAmount.hasValidAmount()) {
            tilAmount.setError("Amount is required");
            return;
        }
        
        long amount = etAmount.getAmount();
        
        // Process QRIS payment
        selectedPaymentMethod = "QRIS";
        tvPaymentMethod.setText("Payment Method: " + selectedPaymentMethod);
        showQrisPaymentDialog(amount);
    }
    
    private void processSalesWithCard() {
        // Clear errors
        tilAmount.setError(null);
        
        // Validate using AmountEditText method
        if (!etAmount.hasValidAmount()) {
            tilAmount.setError("Amount is required");
            return;
        }
        
        long amount = etAmount.getAmount();
        
        // Process card payment
        selectedPaymentMethod = "Card";
        tvPaymentMethod.setText("Payment Method: " + selectedPaymentMethod);
        simulateCardScan(amount);
    }
    
    private void showQrisPaymentDialog(long amount) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qris_payment, null);
        TextView tvAmount = dialogView.findViewById(R.id.tv_amount);
        ImageView ivQrCode = dialogView.findViewById(R.id.iv_qr_code);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirmPayment = dialogView.findViewById(R.id.btn_confirm_payment);
        
        // Format amount
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvAmount.setText(formatter.format(amount));
        
        // Generate QR code
        String qrData = "QRIS|MERCHANT|UniEDC|" + System.currentTimeMillis() + "|" + amount;
        Bitmap qrBitmap = generateQRCode(qrData);
        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirmPayment.setOnClickListener(v -> {
            dialog.dismiss();
            processPayment(amount, "QRIS");
        });
        
        dialog.show();
    }
    
    private Bitmap generateQRCode(String data) {
        // Simple QR code placeholder implementation without ZXing
        // In production, you would use a proper QR code library or service
        
        int size = 512;
        int moduleSize = 8; // Size of each QR module
        int modules = size / moduleSize;
        
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        
        // Fill background white
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, size, size, paint);
        
        // Draw black border
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(moduleSize);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        canvas.drawRect(0, 0, size, size, paint);
        
        // Draw finder patterns (corners)
        paint.setStyle(android.graphics.Paint.Style.FILL);
        drawFinderPattern(canvas, paint, 0, 0, moduleSize);
        drawFinderPattern(canvas, paint, (modules - 7) * moduleSize, 0, moduleSize);
        drawFinderPattern(canvas, paint, 0, (modules - 7) * moduleSize, moduleSize);
        
        // Draw simple data pattern in center
        paint.setTextSize(24);
        paint.setTextAlign(android.graphics.Paint.Align.CENTER);
        String displayText = "QRIS Payment";
        canvas.drawText(displayText, size / 2, size / 2 - 40, paint);
        
        // Draw amount from data
        if (data.contains("|")) {
            String[] parts = data.split("\\|");
            if (parts.length > 3) {
                String amountStr = parts[parts.length - 1];
                try {
                    long amount = Long.parseLong(amountStr);
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    canvas.drawText(formatter.format(amount), size / 2, size / 2, paint);
                } catch (NumberFormatException e) {
                    // Ignore parsing error
                }
            }
        }
        
        // Add some random pattern for visual effect
        java.util.Random random = new java.util.Random(data.hashCode());
        for (int i = 8; i < modules - 8; i++) {
            for (int j = 8; j < modules - 8; j++) {
                if (random.nextBoolean() && random.nextBoolean()) {
                    canvas.drawRect(i * moduleSize, j * moduleSize, 
                                  (i + 1) * moduleSize, (j + 1) * moduleSize, paint);
                }
            }
        }
        
        return bitmap;
    }
    
    private void drawFinderPattern(android.graphics.Canvas canvas, android.graphics.Paint paint, 
                                  int x, int y, int moduleSize) {
        // Draw outer square
        paint.setStyle(android.graphics.Paint.Style.FILL);
        canvas.drawRect(x, y, x + 7 * moduleSize, y + 7 * moduleSize, paint);
        
        // Draw white square
        paint.setColor(Color.WHITE);
        canvas.drawRect(x + moduleSize, y + moduleSize, 
                       x + 6 * moduleSize, y + 6 * moduleSize, paint);
        
        // Draw inner square
        paint.setColor(Color.BLACK);
        canvas.drawRect(x + 2 * moduleSize, y + 2 * moduleSize, 
                       x + 5 * moduleSize, y + 5 * moduleSize, paint);
    }
    
    private void simulateCardScan(long amount) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please insert/tap/swipe your card...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        
        // Simulate card reading
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                
                // Simulate card data
                String cardNumber = "1234567890123456";
                String maskedCard = "****" + cardNumber.substring(cardNumber.length() - 4);
                
                // Store payment details for later use
                getIntent().putExtra("payment_amount", amount);
                getIntent().putExtra("payment_info", maskedCard);
                
                // Show modern PIN dialog instead of PIN verification activity
                showModernPinDialog(amount, cardNumber);
            }
        }, 3000);
    }
    
    private void processPayment(long amount, String paymentInfo) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing payment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Simulate payment processing
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                
                // Format amount
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                String formattedAmount = formatter.format(amount);
                String transactionId = "TRX" + System.currentTimeMillis();
                
                // Print receipt
                printReceipt(amount, transactionId, paymentInfo);
                
                // Prepare payment details
                String paymentDetails = "Payment Method: " + selectedPaymentMethod;
                if (selectedPaymentMethod.equals("Card")) {
                    paymentDetails += "\nCard Number: " + paymentInfo;
                }
                
                // Navigate to success page
                TransactionSuccessActivity.start(PurchaseActivity.this,
                    "sales",
                    formattedAmount,
                    "Merchant: UniEDC Store",
                    transactionId,
                    paymentDetails);
                
                // Close this activity
                finish();
            }
        }, 2000);
    }
    
    private void printReceipt(long amount, String transactionId, String paymentInfo) {
        // Simulate receipt printing
        String receipt = "\n" +
                "================================\n" +
                "          UniEDC\n" +
                "   Electronic Data Capture\n" +
                "================================\n" +
                "\n" +
                "Date: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + "\n" +
                "Transaction ID: " + transactionId + "\n" +
                "\n" +
                "--------------------------------\n" +
                "Payment Method: " + selectedPaymentMethod + "\n";
        
        if (selectedPaymentMethod.equals("Card")) {
            receipt += "Card Number: " + paymentInfo + "\n";
        }
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        receipt += "\n" +
                "Amount: " + formatter.format(amount) + "\n" +
                "--------------------------------\n" +
                "\n" +
                "      Transaction Successful\n" +
                "         Thank You!\n" +
                "\n" +
                "================================\n" +
                "\n\n\n";
        
        // In a real app, this would send to the printer
        android.util.Log.d("Receipt", receipt);
    }
    
    private void showModernPinDialog(long amount, String cardNumber) {
        // Create modern PIN dialog
        Dialog pinDialog = new Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pinDialog.setContentView(R.layout.dialog_pin_input);
        pinDialog.setCancelable(false);
        
        // Make dialog width responsive
        if (pinDialog.getWindow() != null) {
            pinDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            pinDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        
        // Get views
        TextView tvMessage = pinDialog.findViewById(R.id.tv_pin_message);
        EditText etPinInput = pinDialog.findViewById(R.id.et_pin_input);
        TextView tvError = pinDialog.findViewById(R.id.tv_error);
        
        // PIN dots
        View dot1 = pinDialog.findViewById(R.id.dot1);
        View dot2 = pinDialog.findViewById(R.id.dot2);
        View dot3 = pinDialog.findViewById(R.id.dot3);
        View dot4 = pinDialog.findViewById(R.id.dot4);
        View dot5 = pinDialog.findViewById(R.id.dot5);
        View dot6 = pinDialog.findViewById(R.id.dot6);
        View[] dots = {dot1, dot2, dot3, dot4, dot5, dot6};
        
        // Number pad buttons
        Button btn0 = pinDialog.findViewById(R.id.btn_0);
        Button btn1 = pinDialog.findViewById(R.id.btn_1);
        Button btn2 = pinDialog.findViewById(R.id.btn_2);
        Button btn3 = pinDialog.findViewById(R.id.btn_3);
        Button btn4 = pinDialog.findViewById(R.id.btn_4);
        Button btn5 = pinDialog.findViewById(R.id.btn_5);
        Button btn6 = pinDialog.findViewById(R.id.btn_6);
        Button btn7 = pinDialog.findViewById(R.id.btn_7);
        Button btn8 = pinDialog.findViewById(R.id.btn_8);
        Button btn9 = pinDialog.findViewById(R.id.btn_9);
        Button btnCancel = pinDialog.findViewById(R.id.btn_cancel);
        Button btnOk = pinDialog.findViewById(R.id.btn_ok);
        
        // Set message
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvMessage.setText("Enter PIN for payment of " + formatter.format(amount));
        
        // Track PIN input
        StringBuilder pinBuilder = new StringBuilder();
        
        // Update PIN dots display
        Runnable updatePinDisplay = () -> {
            int pinLength = pinBuilder.length();
            for (int i = 0; i < dots.length; i++) {
                if (i < pinLength) {
                    dots[i].setBackgroundResource(R.drawable.pin_dot_filled);
                } else {
                    dots[i].setBackgroundResource(R.drawable.pin_dot_empty);
                }
            }
        };
        
        // Number button click listener
        View.OnClickListener numberClickListener = v -> {
            if (pinBuilder.length() < 6) {
                Button btn = (Button) v;
                pinBuilder.append(btn.getText());
                updatePinDisplay.run();
                
                // Clear any previous error
                tvError.setVisibility(View.GONE);
            }
        };
        
        // Set number button listeners
        btn0.setOnClickListener(numberClickListener);
        btn1.setOnClickListener(numberClickListener);
        btn2.setOnClickListener(numberClickListener);
        btn3.setOnClickListener(numberClickListener);
        btn4.setOnClickListener(numberClickListener);
        btn5.setOnClickListener(numberClickListener);
        btn6.setOnClickListener(numberClickListener);
        btn7.setOnClickListener(numberClickListener);
        btn8.setOnClickListener(numberClickListener);
        btn9.setOnClickListener(numberClickListener);
        
        // Cancel/Delete button
        btnCancel.setOnClickListener(v -> {
            if (pinBuilder.length() > 0) {
                pinBuilder.deleteCharAt(pinBuilder.length() - 1);
                updatePinDisplay.run();
            }
        });
        
        // OK button
        btnOk.setOnClickListener(v -> {
            if (pinBuilder.length() >= 4) {
                String pin = pinBuilder.toString();
                pinDialog.dismiss();
                
                // Process payment with PIN
                String paymentInfo = getIntent().getStringExtra("payment_info");
                processPayment(amount, paymentInfo);
            } else {
                tvError.setText("PIN must be at least 4 digits");
                tvError.setVisibility(View.VISIBLE);
            }
        });
        
        pinDialog.show();
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