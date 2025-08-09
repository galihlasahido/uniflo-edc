package id.uniflo.uniedc.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
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

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.managers.SettingsManager;

public class BalanceInquiryActivitySimple extends AppCompatActivity {
    
    private static final String TAG = "BalanceInquiry";
    
    private Toolbar toolbar;
    private TextView tvBalance;
    private TextView tvLastUpdate;
    private Button btnCheckBalance;
    
    private Context mContext;
    private IcReader icReader;
    private Dialog cardInsertDialog;
    private boolean isServiceConnected = false;
    private Map<String, String> tlvData = new HashMap<>();
    private SettingsManager settingsManager;
    private int currentDetectionAttempt = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_inquiry);
        
        mContext = this;
        
        initViews();
        setupToolbar();
        setupListeners();
        
        // Initialize managers
        settingsManager = SettingsManager.getInstance(this);
        
        // Initialize SDK
        initializeSdk();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvBalance = findViewById(R.id.tv_balance);
        tvLastUpdate = findViewById(R.id.tv_last_update);
        btnCheckBalance = findViewById(R.id.btn_check_balance);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Balance Inquiry");
        }
    }
    
    private void setupListeners() {
        btnCheckBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBalance();
            }
        });
    }
    
    private void initializeSdk() {
        ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
            @Override
            public void onSuccess() {
                isServiceConnected = true;
                icReader = IcReader.getInstance(mContext);
                
                runOnUiThread(() -> {
                    Toast.makeText(mContext, "Card reader ready", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onFail(int errorCode) {
                isServiceConnected = false;
                runOnUiThread(() -> {
                    Toast.makeText(mContext, "Failed to connect to card service: " + errorCode, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void checkBalance() {
        if (!isServiceConnected) {
            Toast.makeText(this, "Card service not connected. Please wait...", Toast.LENGTH_SHORT).show();
            initializeSdk();
            return;
        }
        
        // Reset attempt counter
        currentDetectionAttempt = 0;
        
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
        
        tvTitle.setText("Insert Card");
        tvMessage.setText("Please insert your card for balance inquiry");
        
        btnCancel.setOnClickListener(v -> {
            cancelCardDetection();
            cardInsertDialog.dismiss();
        });
        
        cardInsertDialog.show();
        
        // Start card detection
        detectCard();
    }
    
    private void cancelCardDetection() {
        if (icReader != null) {
            try {
                icReader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing card reader", e);
            }
        }
    }
    
    private void detectCard() {
        if (icReader == null) {
            Toast.makeText(this, "Card reader not initialized", Toast.LENGTH_SHORT).show();
            if (cardInsertDialog != null) {
                cardInsertDialog.dismiss();
            }
            return;
        }
        
        // Start with power cycle approach
        performPowerCycleDetection();
    }
    
    private void performPowerCycleDetection() {
        Log.d(TAG, "🔄 Performing power cycle detection (attempt " + (currentDetectionAttempt + 1) + "/5)");
        
        new Thread(() -> {
            try {
                // First, ensure card reader is closed
                Log.d(TAG, "🔌 Closing card reader for clean state...");
                icReader.close();
                Thread.sleep(500);
                
                // Update UI
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        tvMessage.setText("Initializing card reader... (attempt " + (currentDetectionAttempt + 1) + "/5)");
                    }
                });
                
                // Wait a bit more for card to settle
                Log.d(TAG, "⏳ Waiting 1 second for card reader stabilization...");
                Thread.sleep(1000);
                
                // Now try to detect card with progressive timeout
                int baseTimeout = 30;
                int timeout = baseTimeout + (currentDetectionAttempt * 10); // 30s, 40s, 50s, 60s, 70s
                
                Log.d(TAG, String.format("⏰ Setting detection timeout to %d seconds for attempt %d", timeout, currentDetectionAttempt + 1));
                
                runOnUiThread(() -> {
                    detectCardWithTimeout(timeout);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error during power cycle", e);
                runOnUiThread(() -> {
                    handleDetectionFailure();
                });
            }
        }).start();
    }
    
    private void detectCardWithTimeout(int timeout) {
        Log.d(TAG, "🔍 Starting card detection with timeout: " + timeout + "s");
        Log.d(TAG, "⏳ Waiting for card insertion...");
        Log.d(TAG, "💡 Please insert your card now");
        
        icReader.openCard(timeout, new OnIcReaderCallback() {
            @Override
            public void onCardATR(byte[] atr) {
                Log.d(TAG, "🎉 CARD INSERTED! ATR received");
                Log.d(TAG, "📊 ATR Length: " + atr.length + " bytes");
                Log.d(TAG, "📊 ATR Data: " + BytesUtils.byte2HexStr(atr));
                
                // Parse basic ATR information
                if (atr.length > 0) {
                    Log.d(TAG, "📋 ATR Analysis:");
                    Log.d(TAG, "   - TS (Initial Character): 0x" + String.format("%02X", atr[0] & 0xFF));
                    if (atr.length > 1) {
                        Log.d(TAG, "   - T0 (Format Character): 0x" + String.format("%02X", atr[1] & 0xFF));
                    }
                    Log.d(TAG, "   - Card appears to be powered and responding");
                }
                
                currentDetectionAttempt = 0; // Reset counter on success
                Log.d(TAG, "✅ Card detection successful, proceeding to card processing...");
                
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        ProgressBar progressBar = cardInsertDialog.findViewById(R.id.progress_bar);
                        tvMessage.setText("Card detected, reading data...");
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
                
                // Wait before processing to ensure stability and add timeout
                Log.d(TAG, "⏳ Waiting 1 second for card stabilization before processing...");
                new Handler().postDelayed(() -> {
                    processCardWithTimeout();
                }, 1000);
            }
            
            @Override
            public void onError(int errorCode) {
                Log.e(TAG, "Card detection error: " + errorCode + " (0x" + Integer.toHexString(errorCode) + ")");
                
                if (errorCode == 32778) { // Power error
                    handlePowerError();
                } else {
                    // Other errors
                    runOnUiThread(() -> {
                        if (cardInsertDialog != null) {
                            cardInsertDialog.dismiss();
                        }
                        
                        String errorMsg = getErrorMessage(errorCode);
                        Toast.makeText(BalanceInquiryActivitySimple.this, errorMsg, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    
    private void handlePowerError() {
        currentDetectionAttempt++;
        
        if (currentDetectionAttempt >= 5) {
            // After 5 attempts, show manual intervention dialog
            runOnUiThread(() -> {
                if (cardInsertDialog != null) {
                    cardInsertDialog.dismiss();
                }
                showPowerErrorRecoveryDialog();
            });
        } else {
            // Try different recovery strategies
            runOnUiThread(() -> {
                if (cardInsertDialog != null) {
                    TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                    tvMessage.setText("Power error detected. Trying recovery method " + (currentDetectionAttempt + 1) + "...");
                }
            });
            
            // Apply different strategies based on attempt number
            switch (currentDetectionAttempt) {
                case 1:
                    // Simple retry with longer delay
                    new Handler().postDelayed(() -> performPowerCycleDetection(), 2000);
                    break;
                case 2:
                    // Try with card reader reset
                    performCardReaderReset();
                    break;
                case 3:
                    // Try with service reconnection
                    performServiceReconnection();
                    break;
                case 4:
                    // Last attempt with maximum delay
                    new Handler().postDelayed(() -> performPowerCycleDetection(), 5000);
                    break;
            }
        }
    }
    
    private void performCardReaderReset() {
        Log.d(TAG, "Performing card reader reset");
        
        new Thread(() -> {
            try {
                // Multiple close attempts
                for (int i = 0; i < 3; i++) {
                    icReader.close();
                    Thread.sleep(300);
                }
                
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        tvMessage.setText("Card reader reset complete. Retrying...");
                    }
                });
                
                Thread.sleep(1500);
                
                runOnUiThread(() -> performPowerCycleDetection());
                
            } catch (Exception e) {
                Log.e(TAG, "Error during reader reset", e);
                runOnUiThread(() -> handleDetectionFailure());
            }
        }).start();
    }
    
    private void performServiceReconnection() {
        Log.d(TAG, "Performing service reconnection");
        
        runOnUiThread(() -> {
            if (cardInsertDialog != null) {
                TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                tvMessage.setText("Reconnecting to card service...");
            }
        });
        
        new Thread(() -> {
            try {
                // Close reader
                if (icReader != null) {
                    icReader.close();
                }
                
                // Unbind service
                ServiceManager.unbindPosServer();
                Thread.sleep(1000);
                
                // Rebind service
                ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
                    @Override
                    public void onSuccess() {
                        isServiceConnected = true;
                        icReader = IcReader.getInstance(mContext);
                        
                        runOnUiThread(() -> {
                            Log.d(TAG, "Service reconnected successfully");
                            performPowerCycleDetection();
                        });
                    }
                    
                    @Override
                    public void onFail(int errorCode) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Service reconnection failed: " + errorCode);
                            handleDetectionFailure();
                        });
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error during service reconnection", e);
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
    
    private void showPowerErrorRecoveryDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Persistent Card Power Error")
            .setMessage("The card reader cannot power your card after multiple attempts.\n\n" +
                       "Troubleshooting steps:\n" +
                       "1. Remove the card completely\n" +
                       "2. Check if the chip is clean and undamaged\n" +
                       "3. Ensure the card is not bent or warped\n" +
                       "4. Try inserting the card more slowly\n" +
                       "5. Make sure chip faces up and card is fully inserted\n\n" +
                       "Would you like to try manual card detection?")
            .setPositiveButton("Try Manual Mode", (dialog, which) -> {
                showManualCardDetectionDialog();
            })
            .setNeutralButton("Clean Card First", (dialog, which) -> {
                showCleaningInstructions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showManualCardDetectionDialog() {
        Dialog manualDialog = new Dialog(this);
        manualDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        manualDialog.setContentView(R.layout.dialog_insert_card);
        manualDialog.setCancelable(false);
        
        TextView tvTitle = manualDialog.findViewById(R.id.tv_title);
        TextView tvMessage = manualDialog.findViewById(R.id.tv_message);
        ProgressBar progressBar = manualDialog.findViewById(R.id.progress_bar);
        Button btnCancel = manualDialog.findViewById(R.id.btn_cancel);
        
        tvTitle.setText("Manual Card Detection");
        tvMessage.setText("1. Remove card if inserted\n2. Wait 5 seconds\n3. Insert card VERY SLOWLY\n4. Push until it clicks");
        progressBar.setVisibility(View.GONE);
        
        // Add a "Card Inserted" button
        Button btnCardInserted = new Button(this);
        btnCardInserted.setText("I've Inserted the Card");
        btnCardInserted.setOnClickListener(v -> {
            manualDialog.dismiss();
            currentDetectionAttempt = 0; // Reset counter
            performPowerCycleDetection();
        });
        
        // Add button to dialog layout programmatically
        ((android.widget.LinearLayout) btnCancel.getParent()).addView(btnCardInserted, 0);
        
        btnCancel.setOnClickListener(v -> {
            cancelCardDetection();
            manualDialog.dismiss();
        });
        
        manualDialog.show();
    }
    
    private void showCleaningInstructions() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Card Cleaning Instructions")
            .setMessage("Please follow these steps:\n\n" +
                       "1. Remove the card from the reader\n" +
                       "2. Use a clean, dry, soft cloth\n" +
                       "3. Gently wipe the gold chip contacts\n" +
                       "4. Remove any visible dirt or residue\n" +
                       "5. Ensure the chip is completely dry\n" +
                       "6. Check for scratches or damage\n\n" +
                       "If the chip appears damaged, try a different card.")
            .setPositiveButton("Done, Try Again", (dialog, which) -> {
                currentDetectionAttempt = 0; // Reset counter
                showCardInsertDialog();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void processCardWithTimeout() {
        // Create a timeout handler
        Handler timeoutHandler = new Handler();
        boolean[] isCompleted = {false};
        
        // Set timeout of 15 seconds for card processing
        Runnable timeoutRunnable = () -> {
            if (!isCompleted[0]) {
                Log.e(TAG, "Card processing timeout after 15 seconds");
                isCompleted[0] = true;
                
                // Close card reader
                if (icReader != null) {
                    icReader.close();
                }
                
                runOnUiThread(() -> {
                    handleCardError("Card processing timeout. Please try again.");
                });
            }
        };
        
        timeoutHandler.postDelayed(timeoutRunnable, 15000); // 15 second timeout
        
        // Start card processing in background
        new Thread(() -> {
            try {
                if (!isCompleted[0]) {
                    processCard();
                    isCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                }
            } catch (Exception e) {
                if (!isCompleted[0]) {
                    Log.e(TAG, "Exception during card processing", e);
                    isCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    runOnUiThread(() -> {
                        handleCardError("Card processing error: " + e.getMessage());
                    });
                }
            }
        }).start();
    }
    
    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case -1:
                return "Card reader error. Please try again.";
            case -2:
                return "No card detected. Please insert card properly.";
            case -3:
                return "Card detection timeout. Please try again.";
            case -4:
                return "Card detection cancelled.";
            case 32778: // 0x800A
                return "Card power error. Please check card condition.";
            case 32779: // 0x800B
                return "Card communication error. Check if card is inserted correctly.";
            case 32768: // 0x8000
                return "Card reader not ready. Please wait and try again.";
            default:
                return "Card detection failed. Error: " + errorCode + " (0x" + Integer.toHexString(errorCode) + ")";
        }
    }
    
    private void processCard() {
        Log.d(TAG, "Starting card processing...");
        
        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        tvMessage.setText("Processing card data...");
                    }
                });
                
                // Enhanced card detection with proper NSICCS AIDs and debug logging
                Log.d(TAG, "🔍 Starting enhanced card detection with timeout protection...");
                
                byte[] recvData = new byte[256];
                int[] len = new int[1];
                String cardType = "Unknown";
                boolean cardProcessed = false;
                int ret;
                long startTime = System.currentTimeMillis();
                
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
                
                // Try each AID with timeout check
                for (int i = 0; i < cardAIDs.length && !cardProcessed; i++) {
                    // Check timeout (max 10 seconds for card processing)
                    if (System.currentTimeMillis() - startTime > 10000) {
                        Log.e(TAG, "⏰ Card processing timeout after 10 seconds");
                        throw new RuntimeException("Card processing timeout");
                    }
                    
                    byte[] currentAID = cardAIDs[i];
                    String currentName = cardNames[i];
                    
                    Log.d(TAG, String.format("🔍 Trying %s AID: %s (attempt %d/%d)", 
                          currentName, BytesUtils.byte2HexStr(currentAID), i+1, cardAIDs.length));
                    
                    // Build SELECT AID command
                    byte[] selectAID = new byte[5 + currentAID.length];
                    selectAID[0] = 0x00; // CLA
                    selectAID[1] = (byte)0xA4; // INS - SELECT
                    selectAID[2] = 0x04; // P1 - Select by name
                    selectAID[3] = 0x00; // P2
                    selectAID[4] = (byte)currentAID.length; // Lc
                    System.arraycopy(currentAID, 0, selectAID, 5, currentAID.length);
                    
                    Log.d(TAG, "📤 Sending SELECT command: " + BytesUtils.byte2HexStr(selectAID));
                    
                    // Send APDU command
                    ret = icReader.sendApduCustomer(selectAID, selectAID.length, recvData, len);
                    
                    Log.d(TAG, String.format("📥 %s APDU result: ret=0x%X, len=%d", currentName, ret, len[0]));
                    
                    if (ret == ErrCode.ERR_SUCCESS) {
                        Log.d(TAG, "📥 " + currentName + " APDU Response: " + BytesUtils.byte2HexStr(recvData, len[0]));
                        
                        if (len[0] >= 2) {
                            int sw1 = recvData[len[0] - 2] & 0xFF;
                            int sw2 = recvData[len[0] - 1] & 0xFF;
                            
                            Log.d(TAG, String.format("📋 %s Status: SW1=0x%02X SW2=0x%02X", currentName, sw1, sw2));
                            
                            if (sw1 == 0x90 && sw2 == 0x00) {
                                // Success!
                                cardType = currentName.startsWith("NSICCS") ? "NSICCS" : currentName;
                                cardProcessed = true;
                                
                                Log.d(TAG, String.format("✅ SUCCESS! %s card detected and selected!", currentName));
                                Log.d(TAG, String.format("🎯 Card Type: %s", cardType));
                                Log.d(TAG, String.format("⏱️ Detection time: %dms", System.currentTimeMillis() - startTime));
                                
                                // Log the response data for analysis
                                if (len[0] > 2) {
                                    byte[] responseData = new byte[len[0] - 2];
                                    System.arraycopy(recvData, 0, responseData, 0, len[0] - 2);
                                    Log.d(TAG, "📊 Card Response Data: " + BytesUtils.byte2HexStr(responseData));
                                }
                                
                                break;
                            } else {
                                Log.d(TAG, String.format("❌ %s rejected: SW1=0x%02X SW2=0x%02X", currentName, sw1, sw2));
                            }
                        } else {
                            Log.w(TAG, String.format("⚠️ %s returned insufficient data: %d bytes", currentName, len[0]));
                        }
                    } else {
                        Log.w(TAG, String.format("❌ %s APDU failed: error=0x%X (%d)", currentName, ret, ret));
                    }
                    
                    // Small delay between attempts
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // If still no success, try simple detection
                if (!cardProcessed) {
                    Log.d(TAG, "Using generic card detection");
                    cardType = "EMV Card";
                    cardProcessed = true; // Proceed anyway
                }
                
                Log.d(TAG, "Card processing complete. Type: " + cardType);
                
                // Proceed with TLV data collection
                collectTLVDataAndProcess(cardType);
                
            } catch (Exception e) {
                Log.e(TAG, "Error processing card", e);
                runOnUiThread(() -> {
                    handleCardError("Error processing card: " + e.getMessage());
                });
            }
        }).start();
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void collectTLVDataAndProcess(String cardType) {
        Log.d(TAG, "Collecting TLV data for card type: " + cardType);
        
        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        tvMessage.setText("Building transaction data...");
                    }
                });
                
                tlvData.clear();
                
                // Get terminal configuration
                SettingsManager.TerminalConfig terminalConfig = settingsManager.getTerminalConfig();
                String traceNumber = settingsManager.getNextTraceNumber();
                
                // Build basic TLV data
                tlvData.put("MTI", "0200"); // Message Type Indicator for financial transaction
                tlvData.put("PAN", "1234567890123456"); // Dummy PAN for demo
                tlvData.put("ProcessingCode", "310000"); // Balance inquiry
                tlvData.put("Amount", "000000000000"); // Zero amount for balance inquiry
                tlvData.put("TransmissionDateTime", new SimpleDateFormat("MMddHHmmss").format(new Date()));
                tlvData.put("STAN", traceNumber); // System Trace Audit Number
                tlvData.put("LocalTime", new SimpleDateFormat("HHmmss").format(new Date()));
                tlvData.put("LocalDate", new SimpleDateFormat("MMdd").format(new Date()));
                tlvData.put("ExpiryDate", "2512"); // Dummy expiry
                tlvData.put("POSEntryMode", "051"); // Chip card
                tlvData.put("CardSequenceNumber", "001");
                tlvData.put("FunctionCode", "831"); // Balance inquiry
                tlvData.put("ReasonCode", "00");
                tlvData.put("MerchantType", "5999"); // General merchant
                tlvData.put("POSConditionCode", "00");
                tlvData.put("AcquiringInstitutionCode", "123456");
                tlvData.put("TerminalID", terminalConfig.terminalId);
                tlvData.put("MerchantID", terminalConfig.merchantId);
                tlvData.put("CurrencyCode", "360"); // IDR
                tlvData.put("CardType", cardType);
                
                // Skip complex EMV commands that might hang
                // Just add simulated data for demo
                String arqc = generateSimulatedARQC();
                tlvData.put("ARQC", arqc);
                tlvData.put("CID", "80"); // Cryptogram Information Data
                tlvData.put("ATC", "0001"); // Application Transaction Counter
                tlvData.put("TVR", "0000000000"); // Terminal Verification Results
                tlvData.put("TSI", "E800"); // Transaction Status Information
                
                Log.d(TAG, "TLV data collection complete");
                
                // Close card reader before sending to host
                if (icReader != null) {
                    int closeRet = icReader.close();
                    Log.d(TAG, "Card reader closed with result: " + closeRet);
                }
                
                // Send to host
                sendToHost();
                
            } catch (Exception e) {
                Log.e(TAG, "Error collecting TLV data", e);
                runOnUiThread(() -> {
                    handleCardError("Error processing card data: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private String generateSimulatedARQC() {
        // This is a simulated ARQC for demonstration
        // In real implementation, this would be generated by the EMV kernel
        return "1234567890ABCDEF";
    }
    
    private void sendToHost() {
        // Show processing dialog
        runOnUiThread(() -> {
            if (cardInsertDialog != null) {
                TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                tvMessage.setText("Sending to host...");
            }
        });
        
        // Send in background thread
        new Thread(() -> {
            try {
                SettingsManager.NetworkSettings networkSettings = settingsManager.getNetworkSettings();
                
                // Build URL
                String protocol = networkSettings.useSSL ? "https" : "http";
                String urlString = String.format("%s://%s:%d/api/balance-inquiry",
                    protocol, networkSettings.primaryHost, networkSettings.primaryPort);
                
                Log.d(TAG, "Sending to: " + urlString);
                
                // Convert TLV data to JSON
                org.json.JSONObject json = new org.json.JSONObject();
                for (Map.Entry<String, String> entry : tlvData.entrySet()) {
                    json.put(entry.getKey(), entry.getValue());
                }
                
                String jsonData = json.toString();
                Log.d(TAG, "Request data: " + jsonData);
                
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
                    Log.d(TAG, "Response code: " + responseCode);
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = conn.getInputStream();
                        StringBuilder response = new StringBuilder();
                        int ch;
                        while ((ch = is.read()) != -1) {
                            response.append((char) ch);
                        }
                        is.close();
                        
                        Log.d(TAG, "Response: " + response.toString());
                        
                        // Parse response
                        handleHostResponse(response.toString());
                    } else {
                        handleHostError("Host returned error: " + responseCode);
                    }
                    
                } finally {
                    conn.disconnect();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending to host", e);
                
                // Try secondary host if primary fails
                if (e instanceof IOException || e instanceof java.net.ConnectException) {
                    trySecondaryHost();
                } else {
                    handleHostError("Communication error: " + e.getMessage());
                }
            }
        }).start();
    }
    
    private void trySecondaryHost() {
        Log.d(TAG, "Trying secondary host...");
        // For demo, just show error
        handleHostError("Primary host unreachable. Secondary host not implemented in demo.");
    }
    
    private void handleHostResponse(String response) {
        try {
            // Parse JSON response
            org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
            
            String responseCode = jsonResponse.optString("responseCode", "96");
            String balance = jsonResponse.optString("balance", "0");
            String message = jsonResponse.optString("message", "Balance inquiry complete");
            
            if ("00".equals(responseCode)) {
                // Success
                long balanceAmount = Long.parseLong(balance);
                showBalanceResultFromHost(balanceAmount, message);
            } else {
                handleHostError("Transaction declined: " + message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
            // For demo, show simulated balance
            showBalanceResultFromHost(25750000, "Demo balance (host unreachable)");
        }
    }
    
    private void handleHostError(String errorMsg) {
        Log.e(TAG, "Host error: " + errorMsg);
        // For demo, show simulated balance
        runOnUiThread(() -> {
            showBalanceResultFromHost(25750000, "Demo balance (" + errorMsg + ")");
        });
    }
    
    private void showBalanceResultFromHost(long balance, String message) {
        runOnUiThread(() -> {
            if (cardInsertDialog != null) {
                cardInsertDialog.dismiss();
            }
            
            // Update balance display
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            tvBalance.setText(formatter.format(balance));
            tvLastUpdate.setText("Last updated: Just now");
            tvBalance.setVisibility(View.VISIBLE);
            tvLastUpdate.setVisibility(View.VISIBLE);
            
            // Show result dialog with TLV data
            StringBuilder details = new StringBuilder();
            details.append("Balance: ").append(formatter.format(balance)).append("\n\n");
            details.append("Message: ").append(message).append("\n\n");
            details.append("=== Transaction Details ===\n");
            details.append("Terminal ID: ").append(tlvData.get("TerminalID")).append("\n");
            details.append("Merchant ID: ").append(tlvData.get("MerchantID")).append("\n");
            details.append("STAN: ").append(tlvData.get("STAN")).append("\n");
            details.append("Card Type: ").append(tlvData.get("CardType")).append("\n");
            
            if (tlvData.containsKey("ARQC")) {
                details.append("\n=== EMV Data ===\n");
                details.append("ARQC: ").append(tlvData.get("ARQC")).append("\n");
                details.append("CID: ").append(tlvData.get("CID")).append("\n");
                details.append("ATC: ").append(tlvData.get("ATC")).append("\n");
            }
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Balance Inquiry Result")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up SDK resources
        cancelCardDetection();
        
        // Unbind service
        try {
            ServiceManager.unbindPosServer();
        } catch (Exception e) {
            Log.e(TAG, "Error unbinding service", e);
        }
    }
}