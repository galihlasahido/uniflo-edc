package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;

import id.uniflo.uniedc.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaleResultActivity extends Activity {
    
    private static final String TAG = "SaleResult";
    
    private TextView tvMerchantName;
    private TextView tvMerchantAddress;
    private TextView tvDateTime;
    private TextView tvTerminalId;
    private TextView tvMerchantId;
    private TextView tvCardNumber;
    private TextView tvCardType;
    private TextView tvAmount;
    private TextView tvResponseCode;
    private TextView tvApprovalCode;
    private TextView tvReferenceNumber;
    private Button btnPrint;
    private Button btnReprint;
    private Button btnClose;
    
    // Data from intent
    private String merchantName;
    private String merchantAddress;
    private String terminalId;
    private String merchantId;
    private String cardNumber;
    private String cardType;
    private long amount;
    private String responseCode;
    private String approvalCode;
    private String referenceNumber;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_result);
        
        // Initialize views
        initViews();
        
        // Get data from intent
        getIntentData();
        
        // Display data
        displayData();
        
        // Set button listeners
        setButtonListeners();
        
        // Auto print on first load
        printReceipt();
    }
    
    private void initViews() {
        tvMerchantName = findViewById(R.id.tv_merchant_name);
        tvMerchantAddress = findViewById(R.id.tv_merchant_address);
        tvDateTime = findViewById(R.id.tv_date_time);
        tvTerminalId = findViewById(R.id.tv_terminal_id);
        tvMerchantId = findViewById(R.id.tv_merchant_id);
        tvCardNumber = findViewById(R.id.tv_card_number);
        tvCardType = findViewById(R.id.tv_card_type);
        tvAmount = findViewById(R.id.tv_amount);
        tvResponseCode = findViewById(R.id.tv_response_code);
        tvApprovalCode = findViewById(R.id.tv_approval_code);
        tvReferenceNumber = findViewById(R.id.tv_reference_number);
        btnPrint = findViewById(R.id.btn_print);
        btnReprint = findViewById(R.id.btn_reprint);
        btnClose = findViewById(R.id.btn_close);
    }
    
    private void getIntentData() {
        merchantName = getIntent().getStringExtra("merchantName");
        merchantAddress = getIntent().getStringExtra("merchantAddress");
        terminalId = getIntent().getStringExtra("terminalId");
        merchantId = getIntent().getStringExtra("merchantId");
        cardNumber = getIntent().getStringExtra("cardNumber");
        cardType = getIntent().getStringExtra("cardType");
        amount = getIntent().getLongExtra("amount", 0);
        responseCode = getIntent().getStringExtra("responseCode");
        approvalCode = getIntent().getStringExtra("approvalCode");
        referenceNumber = getIntent().getStringExtra("referenceNumber");
    }
    
    private void displayData() {
        // Display merchant info
        tvMerchantName.setText(merchantName != null ? merchantName : "MERCHANT NAME");
        tvMerchantAddress.setText(merchantAddress != null ? merchantAddress : "Merchant Address");
        
        // Display date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        tvDateTime.setText(sdf.format(new Date()));
        
        // Display terminal and merchant ID
        tvTerminalId.setText("Terminal ID: " + (terminalId != null ? terminalId : "12345678"));
        tvMerchantId.setText("Merchant ID: " + (merchantId != null ? merchantId : "123456789012345"));
        
        // Display card info
        tvCardNumber.setText("Card Number: " + (cardNumber != null ? maskCardNumber(cardNumber) : "****"));
        tvCardType.setText("Card Type: " + (cardType != null ? cardType : "UNKNOWN"));
        
        // Display amount
        DecimalFormat formatter = new DecimalFormat("#,###");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        tvAmount.setText("Amount: Rp " + formatter.format(amount));
        
        // Display response info
        tvResponseCode.setText("Response Code: " + (responseCode != null ? responseCode : "00"));
        tvApprovalCode.setText("Approval Code: " + (approvalCode != null ? approvalCode : "123456"));
        tvReferenceNumber.setText("Reference Number: " + (referenceNumber != null ? referenceNumber : "000000000001"));
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        int length = cardNumber.length();
        String first6 = cardNumber.substring(0, 6);
        String last4 = cardNumber.substring(length - 4);
        String masked = first6;
        for (int i = 6; i < length - 4; i++) {
            masked += "*";
        }
        masked += last4;
        return masked;
    }
    
    private void setButtonListeners() {
        btnPrint.setOnClickListener(v -> printReceipt());
        btnReprint.setOnClickListener(v -> printReceipt());
        btnClose.setOnClickListener(v -> finish());
    }
    
    private void printReceipt() {
        // Connect to service and get printer instance
        ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
            @Override
            public void onSuccess() {
                // Get printer instance
                Printer printer = Printer.getInstance(SaleResultActivity.this);
                
                if (printer == null) {
                    Toast.makeText(SaleResultActivity.this, "Failed to get printer instance", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    // Open printer
                    int ret = printer.open();
                    if (ret != ErrCode.ERR_SUCCESS) {
                        Toast.makeText(SaleResultActivity.this, "Failed to open printer: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Start caching
                    ret = printer.startCaching();
                    if (ret != ErrCode.ERR_SUCCESS) {
                        Toast.makeText(SaleResultActivity.this, "Failed to start caching: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Set gray level
                    ret = printer.setGray(3);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        Toast.makeText(SaleResultActivity.this, "Failed to set gray: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Check paper status
                    PrintStatus printStatus = new PrintStatus();
                    ret = printer.getStatus(printStatus);
                    if (ret == ErrCode.ERR_SUCCESS && !printStatus.getmIsHavePaper()) {
                        Toast.makeText(SaleResultActivity.this, "Printer out of paper!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Print header centered
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
                    printer.printStr(merchantName != null ? merchantName : "MERCHANT NAME");
                    printer.printStr("\n");
                    printer.printStr(merchantAddress != null ? merchantAddress : "Merchant Address");
                    printer.printStr("\n\n");
                    printer.printStr("SALE");
                    printer.printStr("\n");
                    printer.printStr("================================");
                    printer.printStr("\n\n");
                    
                    // Print details left aligned
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_LEFT);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    printer.printStr("Date/Time: " + sdf.format(new Date()));
                    printer.printStr("\n");
                    printer.printStr("Terminal ID: " + (terminalId != null ? terminalId : "12345678"));
                    printer.printStr("\n");
                    printer.printStr("Merchant ID: " + (merchantId != null ? merchantId : "123456789012345"));
                    printer.printStr("\n\n");
                    
                    printer.printStr("Card Number: " + (cardNumber != null ? maskCardNumber(cardNumber) : "****"));
                    printer.printStr("\n");
                    printer.printStr("Card Type: " + (cardType != null ? cardType : "UNKNOWN"));
                    printer.printStr("\n");
                    printer.printStr("--------------------------------");
                    printer.printStr("\n\n");
                    
                    // Print amount centered and larger
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
                    printer.printStr("AMOUNT");
                    printer.printStr("\n");
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
                    symbols.setGroupingSeparator('.');
                    formatter.setDecimalFormatSymbols(symbols);
                    printer.printStr("Rp " + formatter.format(amount));
                    printer.printStr("\n\n");
                    
                    // Print transaction details left aligned
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_LEFT);
                    printer.printStr("--------------------------------");
                    printer.printStr("\n");
                    printer.printStr("Response Code: " + (responseCode != null ? responseCode : "00"));
                    printer.printStr("\n");
                    printer.printStr("Approval Code: " + (approvalCode != null ? approvalCode : "123456"));
                    printer.printStr("\n");
                    printer.printStr("Ref Number: " + (referenceNumber != null ? referenceNumber : "000000000001"));
                    printer.printStr("\n\n");
                    
                    // Print footer centered
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
                    printer.printStr("TRANSACTION SUCCESSFUL");
                    printer.printStr("\n");
                    printer.printStr("*** CUSTOMER COPY ***");
                    printer.printStr("\n\n");
                    
                    // Execute print with callback
                    printer.print(new OnPrinterCallback() {
                        @Override
                        public void onSuccess() {
                            printer.feed(32); // Feed paper
                            runOnUiThread(() -> {
                                Toast.makeText(SaleResultActivity.this, "Receipt printed successfully", Toast.LENGTH_SHORT).show();
                            });
                            
                            // Close printer and unbind service
                            try {
                                printer.close();
                                ServiceManager.unbindPosServer();
                            } catch (Exception e) {
                                android.util.Log.e(TAG, "Error closing printer", e);
                            }
                        }
                        
                        @Override
                        public void onError(int errorCode) {
                            runOnUiThread(() -> {
                                Toast.makeText(SaleResultActivity.this, "Print error: " + String.format("0x%x", errorCode), Toast.LENGTH_SHORT).show();
                            });
                            
                            // Close printer and unbind service
                            try {
                                printer.close();
                                ServiceManager.unbindPosServer();
                            } catch (Exception e) {
                                android.util.Log.e(TAG, "Error closing printer", e);
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Toast.makeText(SaleResultActivity.this, "Print failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e(TAG, "Print exception", e);
                }
            }
            
            @Override
            public void onFail(int errorCode) {
                Toast.makeText(SaleResultActivity.this, "Service connection failed: " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }
}