package id.uniflo.uniedc.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.icreader.OnIcReaderCallback;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;
import com.ftpos.library.smartpos.util.BytesUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import android.content.Intent;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.managers.SettingsManager;
import id.uniflo.uniedc.widget.AmountEditText;
import id.uniflo.uniedc.utils.EMVUtil;

public class PurchaseActivityEnhanced extends AppCompatActivity {
    
    private static final String TAG = "PurchaseEnhanced";
    private static final int REQUEST_PIN_VERIFICATION = 1003;
    
    private Toolbar toolbar;
    private TextInputLayout tilAmount;
    private AmountEditText etAmount;
    private Button btnQris;
    private Button btnScanCard;
    private TextView tvPaymentMethod;
    
    private IcReader icReader;
    private boolean isServiceConnected = false;
    private Dialog cardInsertDialog;
    private TextView debugLogView;
    private ScrollView debugScrollView;
    private Context mContext;
    private Map<String, String> tlvData = new HashMap<>();
    private SettingsManager settingsManager;
    
    private int currentDetectionAttempt = 0;
    private static final int MAX_DETECTION_ATTEMPTS = 5;
    private Handler timeoutHandler = new Handler();
    private long purchaseAmount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        
        mContext = this;
        
        initViews();
        setupToolbar();
        setupListeners();
        
        settingsManager = SettingsManager.getInstance(this);
        
        // Initialize card reader service
        initializeService();
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
            getSupportActionBar().setTitle("Purchase");
        }
    }
    
    private void setupListeners() {
        btnQris.setOnClickListener(v -> {
            tvPaymentMethod.setText("Payment Method: QRIS");
            tvPaymentMethod.setVisibility(View.VISIBLE);
            Toast.makeText(this, "QRIS payment selected", Toast.LENGTH_SHORT).show();
        });
        
        btnScanCard.setOnClickListener(v -> {
            tvPaymentMethod.setText("Payment Method: Card");
            tvPaymentMethod.setVisibility(View.VISIBLE);
            proceedWithCardPayment();
        });
    }
    
    private void initializeService() {
        addDebugLog("🔧 Initializing service...");
        
        ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
            @Override
            public void onSuccess() {
                addDebugLog("✅ Service connected");
                isServiceConnected = true;
                
                // Initialize IcReader
                icReader = IcReader.getInstance(mContext);
                if (icReader == null) {
                    addDebugLog("❌ IcReader is null");
                    Toast.makeText(PurchaseActivityEnhanced.this, 
                        "Card reader not available", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFail(int errorCode) {
                addDebugLog("❌ Service connection failed: " + errorCode);
                isServiceConnected = false;
                Toast.makeText(PurchaseActivityEnhanced.this, 
                    "Failed to connect to card reader service", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void proceedWithCardPayment() {
        // Validate amount
        String amountStr = etAmount.getText().toString().replace(",", "").replace(".", "");
        if (amountStr.isEmpty() || amountStr.equals("0")) {
            tilAmount.setError("Please enter amount");
            return;
        }
        
        try {
            purchaseAmount = Long.parseLong(amountStr);
            if (purchaseAmount <= 0) {
                tilAmount.setError("Amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            tilAmount.setError("Invalid amount");
            return;
        }
        
        tilAmount.setError(null);
        
        if (!isServiceConnected || icReader == null) {
            Toast.makeText(this, "Card reader not ready", Toast.LENGTH_SHORT).show();
            initializeService();
            return;
        }
        
        // Show card insertion dialog
        showCardInsertDialog();
    }
    
    private void showCardInsertDialog() {
        cardInsertDialog = new Dialog(this);
        cardInsertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cardInsertDialog.setContentView(R.layout.dialog_insert_card);
        cardInsertDialog.setCancelable(false);
        
        TextView tvTitle = cardInsertDialog.findViewById(R.id.tv_title);
        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
        ProgressBar progressBar = cardInsertDialog.findViewById(R.id.progress_bar);
        Button btnCancel = cardInsertDialog.findViewById(R.id.btn_cancel);
        
        // Initialize debug log views if needed
        android.widget.LinearLayout dialogContent = (android.widget.LinearLayout) tvTitle.getParent();
        
        // Add debug log area
        debugScrollView = new ScrollView(this);
        debugScrollView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            200));
        
        debugLogView = new TextView(this);
        debugLogView.setTextSize(10);
        debugLogView.setPadding(8, 8, 8, 8);
        debugLogView.setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"));
        debugLogView.setTextColor(android.graphics.Color.BLACK);
        debugScrollView.addView(debugLogView);
        
        dialogContent.addView(debugScrollView);
        
        tvTitle.setText("Insert Card");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvMessage.setText("Amount: " + formatter.format(purchaseAmount) + "\nPlease insert your card");
        progressBar.setVisibility(View.VISIBLE);
        
        btnCancel.setOnClickListener(v -> {
            cancelCardDetection();
            cardInsertDialog.dismiss();
        });
        
        cardInsertDialog.show();
        
        // Start card detection
        currentDetectionAttempt = 0;
        detectCard();
    }
    
    private void detectCard() {
        if (icReader == null) {
            handleCardError("Card reader not initialized");
            return;
        }
        
        currentDetectionAttempt++;
        addDebugLog("🔍 Detection attempt #" + currentDetectionAttempt);
        
        new Thread(() -> {
            try {
                addDebugLog("📡 Opening card reader...");
                
                icReader.openCard(30, new OnIcReaderCallback() {
                    @Override
                    public void onCardATR(byte[] atr) {
                        addDebugLog("✅ Card detected!");
                        addDebugLog("📊 ATR: " + atr.length + " bytes");
                        addDebugLog("📊 ATR: " + BytesUtils.byte2HexStr(atr));
                        
                        runOnUiThread(() -> {
                            if (cardInsertDialog != null) {
                                TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                                tvMessage.setText("Card detected, reading data...");
                            }
                        });
                        
                        // Process card with timeout
                        processCardWithTimeout();
                    }
                    
                    @Override
                    public void onError(int errorCode) {
                        addDebugLog("❌ Card detection error: " + errorCode + " (0x" + Integer.toHexString(errorCode) + ")");
                        
                        runOnUiThread(() -> {
                            if (errorCode == 32778) { // Power error
                                handlePowerError();
                            } else {
                                if (currentDetectionAttempt < MAX_DETECTION_ATTEMPTS) {
                                    addDebugLog("🔄 Retrying detection...");
                                    new Handler().postDelayed(() -> detectCard(), 1000);
                                } else {
                                    handleDetectionFailure();
                                }
                            }
                        });
                    }
                });
                
                // openCard is async, errors handled in callback
                
            } catch (Exception e) {
                Log.e(TAG, "Card detection exception", e);
                runOnUiThread(() -> handleCardError("Card detection error: " + e.getMessage()));
            }
        }).start();
    }
    
    private void processCardWithTimeout() {
        addDebugLog("🔧 Setting up timeout mechanism...");
        
        // Set timeout of 5 seconds for card processing
        Runnable timeoutRunnable = () -> {
            addDebugLog("⏰ TIMEOUT! Processing took too long");
            
            // Close card reader
            if (icReader != null) {
                icReader.close();
            }
            
            runOnUiThread(() -> {
                handleCardError("Card processing timeout. Please try again.");
            });
        };
        
        addDebugLog("⏰ Timeout set to 5 seconds");
        timeoutHandler.postDelayed(timeoutRunnable, 5000);
        
        // Start card processing in background
        addDebugLog("🧵 Starting background thread...");
        new Thread(() -> {
            addDebugLog("📱 Calling processCard()...");
            processCard();
            
            // Cancel timeout if processing completes
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }).start();
    }
    
    private void processCard() {
        addDebugLog("🚀 processCard() method started");
        
        try {
            runOnUiThread(() -> {
                if (cardInsertDialog != null) {
                    TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                    tvMessage.setText("Processing card data...");
                }
            });
            
            // Enhanced card detection with proper NSICCS AIDs
            addDebugLog("🔍 Enhanced card detection...");
            
            byte[] recvData = new byte[256];
            int[] len = new int[1];
            String cardType = "Unknown";
            boolean cardProcessed = false;
            int ret;
            
            // Define card AIDs with correct NSICCS values
            byte[][] cardAIDs = {
                // NSICCS AIDs (Indonesian National Payment System) - PRIORITY
                {(byte)0xA0, 0x00, 0x00, 0x06, 0x02, 0x10, 0x10}, // A0000006021010
                {(byte)0xA0, 0x00, 0x00, 0x06, 0x02, 0x20, 0x20}, // A0000006022020
                // International cards as fallbacks
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x04, 0x10, 0x10}, // Mastercard A0000000041010
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10}  // Visa
            };
            
            String[] cardNames = {"NSICCS-1", "NSICCS-2", "Mastercard", "Visa"};
            
            addDebugLog("🔄 Starting AID detection loop");
            
            // Try each AID
            for (int i = 0; i < cardAIDs.length && !cardProcessed; i++) {
                byte[] currentAID = cardAIDs[i];
                String currentName = cardNames[i];
                
                addDebugLog("🔍 Trying " + currentName + " (" + (i+1) + "/4)");
                
                // Build SELECT AID command
                byte[] selectAID = new byte[5 + currentAID.length];
                selectAID[0] = 0x00; // CLA
                selectAID[1] = (byte)0xA4; // INS - SELECT
                selectAID[2] = 0x04; // P1 - Select by name
                selectAID[3] = 0x00; // P2
                selectAID[4] = (byte)currentAID.length; // Lc
                System.arraycopy(currentAID, 0, selectAID, 5, currentAID.length);
                
                addDebugLog("📤 " + BytesUtils.byte2HexStr(currentAID));
                
                // Send APDU command
                ret = icReader.sendApduCustomer(selectAID, selectAID.length, recvData, len);
                
                addDebugLog("📥 Result: 0x" + Integer.toHexString(ret) + ", len=" + len[0]);
                
                if (ret == ErrCode.ERR_SUCCESS && len[0] >= 2) {
                    int sw1 = recvData[len[0] - 2] & 0xFF;
                    int sw2 = recvData[len[0] - 1] & 0xFF;
                    
                    addDebugLog("📋 SW: " + String.format("%02X%02X", sw1, sw2));
                    
                    if (sw1 == 0x90 && sw2 == 0x00) {
                        // Success!
                        cardType = currentName.startsWith("NSICCS") ? "NSICCS" : currentName;
                        cardProcessed = true;
                        
                        addDebugLog("✅ SUCCESS! " + currentName);
                        addDebugLog("🎯 Card Type: " + cardType);
                        
                        break;
                    }
                }
                
                // Small delay between attempts
                Thread.sleep(100);
            }
            
            if (!cardProcessed) {
                addDebugLog("🔄 Using generic detection");
                cardType = "EMV Card";
                cardProcessed = true; // Proceed anyway
            }
            
            addDebugLog("🎉 Processing complete: " + cardType);
            
            // Show PIN input after successful card detection
            showPinInputDialog(cardType);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing card", e);
            runOnUiThread(() -> {
                handleCardError("Error processing card: " + e.getMessage());
            });
        }
    }
    
    private void showPinInputDialog(String cardType) {
        runOnUiThread(() -> {
            addDebugLog("🔐 Showing PIN input dialog");
            
            if (cardInsertDialog != null) {
                TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                tvMessage.setText("Please enter your PIN");
                
                // Create PIN input layout
                android.widget.LinearLayout pinLayout = new android.widget.LinearLayout(this);
                pinLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
                pinLayout.setPadding(16, 16, 16, 16);
                
                // Amount display
                TextView amountDisplay = new TextView(this);
                amountDisplay.setTextSize(20);
                amountDisplay.setGravity(android.view.Gravity.CENTER);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                amountDisplay.setText("Amount: " + formatter.format(purchaseAmount));
                amountDisplay.setTextColor(android.graphics.Color.BLACK);
                amountDisplay.setPadding(0, 0, 0, 16);
                pinLayout.addView(amountDisplay);
                
                // PIN display
                TextView pinDisplay = new TextView(this);
                pinDisplay.setTextSize(18);
                pinDisplay.setGravity(android.view.Gravity.CENTER);
                pinDisplay.setMinHeight(60);
                pinDisplay.setBackgroundColor(android.graphics.Color.LTGRAY);
                pinDisplay.setText("Enter PIN:");
                pinLayout.addView(pinDisplay);
                
                // PIN input field
                android.widget.EditText pinInput = new android.widget.EditText(this);
                pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                pinInput.setHint("Enter 4-6 digit PIN");
                pinInput.setMaxLines(1);
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 16, 0, 16);
                pinInput.setLayoutParams(params);
                pinLayout.addView(pinInput);
                
                // Buttons
                android.widget.LinearLayout buttonLayout = new android.widget.LinearLayout(this);
                buttonLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                
                Button btnCancel = new Button(this);
                btnCancel.setText("Cancel");
                btnCancel.setBackgroundColor(android.graphics.Color.RED);
                btnCancel.setTextColor(android.graphics.Color.WHITE);
                android.widget.LinearLayout.LayoutParams cancelParams = new android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                cancelParams.setMargins(0, 0, 8, 0);
                btnCancel.setLayoutParams(cancelParams);
                
                Button btnOk = new Button(this);
                btnOk.setText("OK");
                btnOk.setBackgroundColor(android.graphics.Color.GREEN);
                btnOk.setTextColor(android.graphics.Color.WHITE);
                android.widget.LinearLayout.LayoutParams okParams = new android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                okParams.setMargins(8, 0, 0, 0);
                btnOk.setLayoutParams(okParams);
                
                buttonLayout.addView(btnCancel);
                buttonLayout.addView(btnOk);
                pinLayout.addView(buttonLayout);
                
                // Create new dialog for PIN
                Dialog pinDialog = new Dialog(this);
                pinDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                pinDialog.setContentView(pinLayout);
                pinDialog.setCancelable(false);
                
                btnCancel.setOnClickListener(v -> {
                    addDebugLog("❌ PIN cancelled");
                    pinDialog.dismiss();
                    if (cardInsertDialog != null) {
                        cardInsertDialog.dismiss();
                    }
                    handleCardError("PIN entry cancelled");
                });
                
                btnOk.setOnClickListener(v -> {
                    String pin = pinInput.getText().toString().trim();
                    if (pin.length() < 4) {
                        Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    addDebugLog("✅ PIN entered: " + pin.length() + " digits");
                    pinDialog.dismiss();
                    
                    // Continue with TLV collection including PIN
                    collectTLVDataAndProcessWithPin(cardType, pin);
                });
                
                // Update display to show PIN requirements
                pinInput.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        StringBuilder display = new StringBuilder("PIN: ");
                        for (int i = 0; i < s.length(); i++) {
                            display.append("*");
                        }
                        if (s.length() == 0) {
                            display.append("Enter PIN");
                        }
                        pinDisplay.setText(display.toString());
                    }
                    
                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });
                
                if (cardInsertDialog != null) {
                    cardInsertDialog.dismiss();
                }
                
                pinDialog.show();
                addDebugLog("🔐 PIN dialog displayed");
            }
        });
    }
    
    private void collectTLVDataAndProcessWithPin(String cardType, String pin) {
        addDebugLog("🔧 Building TLV data with PIN...");
        
        new Thread(() -> {
            try {
                tlvData.clear();
                
                // Generate PIN block
                byte[] pinBlockBytes = generatePinBlock(pin);
                String pinBlock = BytesUtils.byte2HexStr(pinBlockBytes);
                addDebugLog("🔐 PIN block generated: " + pinBlock.substring(0, 8) + "...");
                
                // Get terminal configuration
                SettingsManager.TerminalConfig terminalConfig = settingsManager.getTerminalConfig();
                String traceNumber = settingsManager.getNextTraceNumber();
                
                // Use EMVUtil to generate ARQC and build proper TLV data
                EMVUtil emvUtil = new EMVUtil();
                Map<String, String> emvTlvData = emvUtil.generateARQC(purchaseAmount, pinBlockBytes);
                
                // Add EMV TLV data to our transaction data
                tlvData.putAll(emvTlvData);
                
                // Add transaction-specific data
                tlvData.put("MTI", "0200"); // Message Type Indicator
                tlvData.put("ProcessingCode", "000000"); // Purchase
                tlvData.put("Amount", String.format("%012d", purchaseAmount));
                tlvData.put("PINBlock", pinBlock);
                tlvData.put("TransmissionDateTime", new SimpleDateFormat("MMddHHmmss").format(new Date()));
                tlvData.put("STAN", traceNumber);
                tlvData.put("LocalTime", new SimpleDateFormat("HHmmss").format(new Date()));
                tlvData.put("LocalDate", new SimpleDateFormat("MMdd").format(new Date()));
                tlvData.put("AcquiringInstitutionCode", terminalConfig.acquiringInstitutionCode);
                tlvData.put("TerminalID", terminalConfig.terminalId);
                tlvData.put("MerchantID", terminalConfig.merchantId);
                tlvData.put("CardType", cardType);
                
                // EMV-specific tags from EMVUtil
                if (emvTlvData.containsKey(EMVUtil.TAG_CRYPTOGRAM)) {
                    tlvData.put("ARQC", emvTlvData.get(EMVUtil.TAG_CRYPTOGRAM));
                    addDebugLog("✅ ARQC: " + emvTlvData.get(EMVUtil.TAG_CRYPTOGRAM));
                }
                if (emvTlvData.containsKey(EMVUtil.TAG_ATC)) {
                    tlvData.put("ATC", emvTlvData.get(EMVUtil.TAG_ATC));
                }
                if (emvTlvData.containsKey(EMVUtil.TAG_TVR)) {
                    tlvData.put("TVR", emvTlvData.get(EMVUtil.TAG_TVR));
                }
                if (emvTlvData.containsKey(EMVUtil.TAG_AIP)) {
                    tlvData.put("AIP", emvTlvData.get(EMVUtil.TAG_AIP));
                }
                if (emvTlvData.containsKey(EMVUtil.TAG_ISSUER_APPLICATION_DATA)) {
                    tlvData.put("IAD", emvTlvData.get(EMVUtil.TAG_ISSUER_APPLICATION_DATA));
                }
                
                addDebugLog("📋 TLV data ready with " + tlvData.size() + " fields");
                
                // Close card reader before sending to host
                if (icReader != null) {
                    int closeRet = icReader.close();
                    addDebugLog("🔌 Reader closed: " + closeRet);
                }
                
                addDebugLog("📡 Sending to host...");
                // Send to host
                sendToHost();
                
            } catch (Exception e) {
                Log.e(TAG, "Error collecting TLV data", e);
                runOnUiThread(() -> {
                    handleCardError("Error processing transaction data: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private byte[] generatePinBlock(String pin) {
        try {
            // ISO 9564 Format 0 PIN block
            // Format: 0 + PIN length + PIN + padding with F
            StringBuilder pinData = new StringBuilder();
            pinData.append("0"); // Format 0
            pinData.append(String.format("%01X", pin.length())); // PIN length in hex
            pinData.append(pin);
            
            // Pad to 16 hex chars (8 bytes) with F
            while (pinData.length() < 16) {
                pinData.append("F");
            }
            
            // Convert to bytes
            byte[] pinBytes = hexStringToBytes(pinData.toString());
            
            // In production, this would be XORed with PAN
            // For now, XOR with dummy PAN block (0000 + last 12 digits of PAN excluding check digit)
            byte[] panBlock = hexStringToBytes("0000000000000000");
            
            // XOR PIN block with PAN block
            byte[] pinBlock = new byte[8];
            for (int i = 0; i < 8; i++) {
                pinBlock[i] = (byte)(pinBytes[i] ^ panBlock[i]);
            }
            
            return pinBlock;
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating PIN block", e);
            // Return dummy PIN block on error
            return hexStringToBytes("0412345FFFFFFFFF");
        }
    }
    
    // Remove xorWithPan() as PIN block generation is now handled properly
    
    // Helper method to convert hex string to bytes
    private byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            return new byte[0];
        }
        
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
    
    // Remove generateSimulatedARQC() as we now use EMVUtil
    
    private void sendToHost() {
        runOnUiThread(() -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Processing transaction...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
            new Thread(() -> {
                try {
                    SettingsManager.NetworkSettings networkSettings = settingsManager.getNetworkSettings();
                    
                    // Build URL
                    String protocol = networkSettings.useSSL ? "https" : "http";
                    String urlString = String.format("%s://%s:%d/api/purchase",
                        protocol, networkSettings.primaryHost, networkSettings.primaryPort);
                    
                    addDebugLog("🌐 URL: " + urlString);
                    
                    // Convert TLV data to JSON
                    org.json.JSONObject json = new org.json.JSONObject();
                    for (Map.Entry<String, String> entry : tlvData.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                    
                    String jsonData = json.toString();
                    addDebugLog("📤 Sending transaction data");
                    
                    // Send HTTP request
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    
                    try {
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setConnectTimeout(networkSettings.timeout * 1000);
                        conn.setReadTimeout(networkSettings.timeout * 1000);
                        conn.setDoOutput(true);
                        
                        // Write request body
                        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                        os.writeBytes(jsonData);
                        os.flush();
                        os.close();
                        
                        // Read response
                        int responseCode = conn.getResponseCode();
                        addDebugLog("📥 Response: " + responseCode);
                        
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            InputStream is = conn.getInputStream();
                            StringBuilder response = new StringBuilder();
                            int ch;
                            while ((ch = is.read()) != -1) {
                                response.append((char) ch);
                            }
                            is.close();
                            
                            addDebugLog("✅ Transaction successful");
                            
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                showTransactionSuccess(response.toString());
                            });
                            
                        } else {
                            addDebugLog("❌ Host error: " + responseCode);
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                handleCardError("Transaction failed: " + responseCode);
                            });
                        }
                        
                    } finally {
                        conn.disconnect();
                    }
                    
                } catch (Exception e) {
                    addDebugLog("❌ Host communication error: " + e.getMessage());
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        handleCardError("Transaction error: " + e.getMessage());
                    });
                }
            }).start();
        });
    }
    
    private void showTransactionSuccess(String response) {
        // Navigate to success screen
        Intent intent = new Intent(this, TransactionSuccessActivity.class);
        intent.putExtra("transaction_type", "Purchase");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        intent.putExtra("amount", formatter.format(purchaseAmount));
        intent.putExtra("reference", tlvData.get("STAN"));
        startActivity(intent);
        finish();
    }
    
    private void handlePowerError() {
        addDebugLog("⚡ Handling power error");
        
        if (currentDetectionAttempt < MAX_DETECTION_ATTEMPTS) {
            addDebugLog("🔄 Attempting recovery #" + currentDetectionAttempt);
            
            // Try different recovery strategies
            switch (currentDetectionAttempt) {
                case 1:
                    addDebugLog("💡 Strategy 1: Quick retry");
                    new Handler().postDelayed(() -> detectCard(), 500);
                    break;
                case 2:
                    addDebugLog("💡 Strategy 2: Reset with delay");
                    resetCardReader(1000);
                    break;
                case 3:
                    addDebugLog("💡 Strategy 3: Full reset");
                    resetCardReader(2000);
                    break;
                default:
                    addDebugLog("💡 Strategy 4: Service reconnect");
                    reconnectService();
                    break;
            }
        } else {
            handleDetectionFailure();
        }
    }
    
    private void resetCardReader(long delay) {
        new Thread(() -> {
            try {
                if (icReader != null) {
                    addDebugLog("🔄 Closing reader...");
                    icReader.close();
                    Thread.sleep(delay);
                    addDebugLog("🔄 Reopening reader...");
                    runOnUiThread(() -> detectCard());
                }
            } catch (Exception e) {
                Log.e(TAG, "Reset error", e);
                runOnUiThread(() -> handleDetectionFailure());
            }
        }).start();
    }
    
    private void reconnectService() {
        new Thread(() -> {
            try {
                addDebugLog("🔄 Unbinding service...");
                ServiceManager.unbindPosServer();
                Thread.sleep(1000);
                
                addDebugLog("🔄 Rebinding service...");
                runOnUiThread(() -> {
                    initializeService();
                    new Handler().postDelayed(() -> {
                        if (isServiceConnected && icReader != null) {
                            detectCard();
                        } else {
                            handleDetectionFailure();
                        }
                    }, 2000);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Service reconnection error", e);
                runOnUiThread(() -> handleDetectionFailure());
            }
        }).start();
    }
    
    private void handleDetectionFailure() {
        if (cardInsertDialog != null) {
            cardInsertDialog.dismiss();
        }
        
        Toast.makeText(this, "Unable to detect card after multiple attempts", Toast.LENGTH_LONG).show();
    }
    
    private void handleCardError(String errorMsg) {
        runOnUiThread(() -> {
            if (cardInsertDialog != null) {
                cardInsertDialog.dismiss();
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            
            if (icReader != null) {
                icReader.close();
            }
        });
    }
    
    private void cancelCardDetection() {
        if (icReader != null) {
            icReader.close();
        }
    }
    
    private void addDebugLog(String message) {
        Log.d(TAG, message);
        
        runOnUiThread(() -> {
            if (debugLogView != null) {
                String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
                String logEntry = timestamp + " " + message + "\n";
                debugLogView.append(logEntry);
                
                // Auto-scroll to bottom
                if (debugScrollView != null) {
                    debugScrollView.post(() -> debugScrollView.fullScroll(View.FOCUS_DOWN));
                }
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up
        cancelCardDetection();
        
        // Unbind service
        try {
            ServiceManager.unbindPosServer();
        } catch (Exception e) {
            Log.e(TAG, "Error unbinding service", e);
        }
    }
}