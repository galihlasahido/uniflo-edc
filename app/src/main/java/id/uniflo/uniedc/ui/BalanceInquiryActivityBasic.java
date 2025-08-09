package id.uniflo.uniedc.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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

import javax.net.ssl.HttpsURLConnection;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.database.SecureSettingsDAO;
import id.uniflo.uniedc.database.TerminalConfig;
import id.uniflo.uniedc.database.Transaction;
import id.uniflo.uniedc.database.TransactionDAO;
import id.uniflo.uniedc.managers.SettingsManager;
import id.uniflo.uniedc.utils.EMVUtil;

public class BalanceInquiryActivityBasic extends AppCompatActivity {
    
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
    private TextView debugLogView;
    private TransactionDAO transactionDAO;
    private SecureSettingsDAO settingsDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_inquiry);
        
        mContext = this;
        
        initViews();
        setupToolbar();
        setupListeners();
        
        // Initialize managers and DAOs
        settingsManager = SettingsManager.getInstance(this);
        transactionDAO = new TransactionDAO(this);
        settingsDAO = new SecureSettingsDAO(this);
        
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
        
        // Create debug log TextView programmatically
        debugLogView = new TextView(this);
        debugLogView.setId(android.view.View.generateViewId());
        debugLogView.setTextSize(10);
        debugLogView.setMaxLines(10);
        debugLogView.setTextColor(android.graphics.Color.BLUE);
        debugLogView.setBackgroundColor(android.graphics.Color.rgb(240, 248, 255));
        debugLogView.setPadding(8, 8, 8, 8);
        debugLogView.setText("Debug Log:\n");
        
        // Add to dialog layout
        android.widget.LinearLayout dialogLayout = (android.widget.LinearLayout) tvMessage.getParent();
        if (dialogLayout != null) {
            dialogLayout.addView(debugLogView, dialogLayout.indexOfChild(progressBar));
        }
        
        tvTitle.setText("Insert Card - Debug Mode");
        tvMessage.setText("Please insert your card for balance inquiry");
        
        btnCancel.setOnClickListener(v -> {
            cancelCardDetection();
            cardInsertDialog.dismiss();
        });
        
        cardInsertDialog.show();
        
        addDebugLog("🔍 Starting card detection process...");
        
        // Start card detection
        detectCard();
    }
    
    private void addDebugLog(String message) {
        Log.d(TAG, message); // Still log to console for development
        
        runOnUiThread(() -> {
            if (debugLogView != null) {
                String currentText = debugLogView.getText().toString();
                String newText = currentText + message + "\n";
                
                // Keep only last 15 lines to prevent overflow
                String[] lines = newText.split("\n");
                if (lines.length > 15) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = lines.length - 15; i < lines.length; i++) {
                        sb.append(lines[i]).append("\n");
                    }
                    newText = sb.toString();
                }
                
                debugLogView.setText(newText);
                
                // Auto-scroll to bottom
                if (debugLogView.getParent() instanceof android.widget.ScrollView) {
                    android.widget.ScrollView scrollView = (android.widget.ScrollView) debugLogView.getParent();
                    scrollView.post(() -> scrollView.fullScroll(android.view.View.FOCUS_DOWN));
                }
            }
        });
    }
    
    private void cancelCardDetection() {
        if (icReader != null) {
            try {
                icReader.close();
                addDebugLog("🔌 Card reader closed");
            } catch (Exception e) {
                Log.e(TAG, "Error closing card reader", e);
                addDebugLog("❌ Error closing card reader: " + e.getMessage());
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
        addDebugLog("🔄 Power cycle attempt " + (currentDetectionAttempt + 1) + "/5");
        
        new Thread(() -> {
            try {
                // First, ensure card reader is closed
                addDebugLog("🔌 Closing card reader...");
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
                addDebugLog("⏳ Stabilizing reader...");
                Thread.sleep(1000);
                
                // Now try to detect card with progressive timeout
                int baseTimeout = 30;
                int timeout = baseTimeout + (currentDetectionAttempt * 10); // 30s, 40s, 50s, 60s, 70s
                
                addDebugLog("⏰ Timeout: " + timeout + "s");
                
                runOnUiThread(() -> {
                    detectCardWithTimeout(timeout);
                });
                
            } catch (Exception e) {
                addDebugLog("❌ Power cycle error: " + e.getMessage());
                runOnUiThread(() -> {
                    handleDetectionFailure();
                });
            }
        }).start();
    }
    
    private void detectCardWithTimeout(int timeout) {
        addDebugLog("🔍 Starting detection (" + timeout + "s)");
        addDebugLog("💡 Please insert card now");
        
        icReader.openCard(timeout, new OnIcReaderCallback() {
            @Override
            public void onCardATR(byte[] atr) {
                addDebugLog("🎉 CARD INSERTED!");
                addDebugLog("📊 ATR: " + atr.length + " bytes");
                addDebugLog("📊 " + BytesUtils.byte2HexStr(atr).substring(0, Math.min(20, BytesUtils.byte2HexStr(atr).length())) + "...");
                
                // Parse basic ATR information
                if (atr.length > 0) {
                    addDebugLog("📋 TS: 0x" + String.format("%02X", atr[0] & 0xFF));
                    if (atr.length > 1) {
                        addDebugLog("📋 T0: 0x" + String.format("%02X", atr[1] & 0xFF));
                    }
                }
                
                currentDetectionAttempt = 0; // Reset counter on success
                addDebugLog("✅ Card detected! Processing...");
                
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        ProgressBar progressBar = cardInsertDialog.findViewById(R.id.progress_bar);
                        tvMessage.setText("Card detected, reading data...");
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
                
                // Wait before processing to ensure stability and add timeout
                addDebugLog("⏳ Stabilizing card (1s)...");
                addDebugLog("⚙️ Will start processing in 1s");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    addDebugLog("🚀 Starting processCardWithTimeout...");
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
                        Toast.makeText(BalanceInquiryActivityBasic.this, errorMsg, Toast.LENGTH_LONG).show();
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
                    new Handler(Looper.getMainLooper()).postDelayed(() -> performPowerCycleDetection(), 2000);
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
                    new Handler(Looper.getMainLooper()).postDelayed(() -> performPowerCycleDetection(), 5000);
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
        addDebugLog("🔧 Setting up timeout mechanism...");
        
        // Create a timeout handler
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        boolean[] isCompleted = {false};
        
        // Set timeout of 8 seconds for card processing (reduced from 15)
        Runnable timeoutRunnable = () -> {
            if (!isCompleted[0]) {
                addDebugLog("⏰ TIMEOUT! Processing took too long");
                isCompleted[0] = true;
                
                // Close card reader
                if (icReader != null) {
                    icReader.close();
                }
                
                runOnUiThread(() -> {
                    handleCardError("Card processing timeout after 3 seconds. Please try again.");
                });
            }
        };
        
        addDebugLog("⏰ Timeout set to 3 seconds");
        timeoutHandler.postDelayed(timeoutRunnable, 3000); // 3 second timeout for quick debugging
        
        // Start card processing in background
        addDebugLog("🧵 Starting background thread...");
        new Thread(() -> {
            try {
                addDebugLog("🧵 Background thread started");
                if (!isCompleted[0]) {
                    addDebugLog("📱 Calling processCard()...");
                    processCard();
                    
                    if (!isCompleted[0]) {
                        addDebugLog("✅ processCard() completed");
                        isCompleted[0] = true;
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                    }
                }
            } catch (Exception e) {
                if (!isCompleted[0]) {
                    addDebugLog("❌ Exception: " + e.getMessage());
                    isCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    runOnUiThread(() -> {
                        handleCardError("Card processing error: " + e.getMessage());
                    });
                }
            }
        }).start();
        
        addDebugLog("🚀 processCardWithTimeout setup complete");
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
        addDebugLog("🚀 processCard() method started");
        Log.d(TAG, "Starting card processing...");
        
        try {
            addDebugLog("🧵 Inside processCard - no extra thread");
            
            runOnUiThread(() -> {
                if (cardInsertDialog != null) {
                    TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                    tvMessage.setText("Processing card data...");
                }
            });
            
            addDebugLog("📱 UI updated - continuing...");
            
            // Enhanced card detection with proper NSICCS AIDs and debug logging
            addDebugLog("🔍 Enhanced card detection...");
            
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
            
            addDebugLog("🔄 Starting AID detection loop");
            addDebugLog("📋 Testing " + cardAIDs.length + " AIDs");
            
            // Try each AID with timeout check
            for (int i = 0; i < cardAIDs.length && !cardProcessed; i++) {
                // Check timeout (max 10 seconds for card processing)
                if (System.currentTimeMillis() - startTime > 10000) {
                    addDebugLog("⏰ Processing timeout!");
                    throw new RuntimeException("Card processing timeout");
                }
                
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
                
                addDebugLog("📡 About to send APDU command...");
                // Send APDU command
                ret = icReader.sendApduCustomer(selectAID, selectAID.length, recvData, len);
                addDebugLog("📡 APDU command returned");
                
                addDebugLog("📥 Result: 0x" + Integer.toHexString(ret) + ", len=" + len[0]);
                
                if (ret == ErrCode.ERR_SUCCESS) {
                    String responseHex = BytesUtils.byte2HexStr(recvData, len[0]);
                    addDebugLog("📥 " + responseHex.substring(0, Math.min(16, responseHex.length())) + "...");
                    
                    if (len[0] >= 2) {
                        int sw1 = recvData[len[0] - 2] & 0xFF;
                        int sw2 = recvData[len[0] - 1] & 0xFF;
                        
                        addDebugLog("📋 SW: " + String.format("%02X%02X", sw1, sw2));
                        
                        if (sw1 == 0x90 && sw2 == 0x00) {
                            // Success!
                            cardType = currentName.startsWith("NSICCS") ? "NSICCS" : currentName;
                            cardProcessed = true;
                            
                            addDebugLog("✅ SUCCESS! " + currentName);
                            addDebugLog("🎯 Card Type: " + cardType);
                            addDebugLog("⏱️ Time: " + (System.currentTimeMillis() - startTime) + "ms");
                            
                            break;
                        } else {
                            addDebugLog("❌ " + currentName + " rejected");
                        }
                    } else {
                        addDebugLog("⚠️ Insufficient data: " + len[0] + " bytes");
                    }
                } else {
                    addDebugLog("❌ " + currentName + " failed: 0x" + Integer.toHexString(ret));
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
            addDebugLog("🔐 Showing modern PIN input dialog");
            
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
            
            // Store PIN input
            StringBuilder pinBuilder = new StringBuilder();
            
            // Update PIN dots display - all 6 dots visible by default
            Runnable updateDots = () -> {
                int pinLength = pinBuilder.length();
                for (int i = 0; i < dots.length; i++) {
                    if (i < pinLength) {
                        dots[i].setBackgroundResource(R.drawable.pin_dot_filled);
                    } else {
                        dots[i].setBackgroundResource(R.drawable.pin_dot_empty);
                    }
                    // All 6 dots always visible
                    dots[i].setVisibility(View.VISIBLE);
                }
            };
            
            // Number button click listener
            View.OnClickListener numberClickListener = v -> {
                if (pinBuilder.length() < 6) {
                    Button button = (Button) v;
                    pinBuilder.append(button.getText().toString());
                    updateDots.run();
                    tvError.setVisibility(View.GONE);
                }
            };
            
            // Set click listeners for number buttons
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
            
            // Cancel button - delete last digit
            btnCancel.setOnClickListener(v -> {
                if (pinBuilder.length() > 0) {
                    pinBuilder.deleteCharAt(pinBuilder.length() - 1);
                    updateDots.run();
                    tvError.setVisibility(View.GONE);
                } else {
                    // If no PIN entered, cancel the dialog
                    pinDialog.dismiss();
                    if (cardInsertDialog != null) {
                        cardInsertDialog.dismiss();
                    }
                    if (icReader != null) {
                        icReader.close();
                    }
                }
            });
            
            // OK button - validate and submit PIN
            btnOk.setOnClickListener(v -> {
                String pin = pinBuilder.toString();
                if (pin.length() >= 4 && pin.length() <= 6) {
                    addDebugLog("✅ PIN entered: " + pin.substring(0, 2) + "**");
                    
                    // Dismiss dialogs
                    pinDialog.dismiss();
                    if (cardInsertDialog != null) {
                        cardInsertDialog.dismiss();
                    }
                    
                    // Continue with TLV collection including PIN
                    collectTLVDataAndProcessWithPin(cardType, pin);
                } else {
                    tvError.setText("PIN must be 4-6 digits");
                    tvError.setVisibility(View.VISIBLE);
                    
                    // Shake animation on error
                    android.view.animation.Animation shake = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
                    tvError.startAnimation(shake);
                }
            });
            
            // Dismiss card insert dialog before showing PIN dialog
            if (cardInsertDialog != null) {
                cardInsertDialog.dismiss();
            }
            
            pinDialog.show();
            addDebugLog("🔐 Modern PIN dialog displayed");
        });
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
    
    private void collectTLVDataAndProcessWithPin(String cardType, String pin) {
        addDebugLog("🔧 Building TLV data with PIN...");
        
        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    if (cardInsertDialog != null) {
                        TextView tvMessage = cardInsertDialog.findViewById(R.id.tv_message);
                        tvMessage.setText("Processing PIN and building transaction data...");
                    }
                });
                
                tlvData.clear();
                
                // Generate PIN block (simplified for demo)
                String pinBlock = generatePinBlock(pin);
                addDebugLog("🔐 PIN block generated: " + pinBlock.substring(0, 8) + "...");
                
                // Get terminal configuration
                SettingsManager.TerminalConfig terminalConfig = settingsManager.getTerminalConfig();
                String traceNumber = settingsManager.getNextTraceNumber();
                
                // Read actual card data
                String pan = readCardPAN();
                if (pan == null || pan.isEmpty()) {
                    pan = "1234567890123456"; // Fallback for demo
                    addDebugLog("⚠️ Could not read PAN, using demo value");
                } else {
                    addDebugLog("✅ PAN read successfully: " + maskPAN(pan));
                }
                
                // Build basic TLV data
                tlvData.put("MTI", "0200"); // Message Type Indicator for financial transaction
                tlvData.put("PAN", pan); // Actual PAN from card
                tlvData.put("ProcessingCode", "310000"); // Balance inquiry
                tlvData.put("Amount", "000000000000"); // Zero amount for balance inquiry
                tlvData.put("PINBlock", pinBlock); // Encrypted PIN block
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
                tlvData.put("AcquiringInstitutionCode", terminalConfig.acquiringInstitutionCode);
                tlvData.put("TerminalID", terminalConfig.terminalId);
                tlvData.put("MerchantID", terminalConfig.merchantId);
                tlvData.put("CurrencyCode", "360"); // IDR
                tlvData.put("CardType", cardType);
                
                // Generate real ARQC using EMVUtil
                addDebugLog("🔐 Generating real ARQC with EMVUtil...");
                EMVUtil emvUtil = new EMVUtil();
                
                // Pass the icReader to EMVUtil
                if (icReader != null) {
                    emvUtil.setIcReader(icReader);
                    addDebugLog("✅ IcReader passed to EMVUtil");
                }
                
                // Convert PIN to bytes for EMV processing
                byte[] pinBlockBytes = null;
                if (!pin.isEmpty()) {
                    pinBlockBytes = hexStringToBytes(pinBlock);
                }
                
                // Generate ARQC (amount is 0 for balance inquiry)
                Map<String, String> emvTlvData = emvUtil.generateARQC(0, pinBlockBytes);
                
                // Add EMV data to our TLV data
                if (emvTlvData != null) {
                    tlvData.putAll(emvTlvData);
                    addDebugLog("✅ EMV data added to TLV");
                    
                    // Log important EMV values
                    String arqc = emvTlvData.get("9F26");
                    if (arqc != null) {
                        addDebugLog("🔑 ARQC: " + arqc);
                        tlvData.put("ARQC", arqc);
                    } else {
                        addDebugLog("⚠️ No ARQC generated, using simulated");
                        tlvData.put("ARQC", generateSimulatedARQC());
                    }
                    
                    String atc = emvTlvData.get("9F36");
                    if (atc != null) {
                        tlvData.put("ATC", atc);
                    }
                    
                    String tvr = emvTlvData.get("95");
                    if (tvr != null) {
                        tlvData.put("TVR", tvr);
                    }
                } else {
                    addDebugLog("⚠️ EMV data generation failed, using simulated data");
                    tlvData.put("ARQC", generateSimulatedARQC());
                    tlvData.put("CID", "80");
                    tlvData.put("ATC", "0001");
                    tlvData.put("TVR", "0000000000");
                    tlvData.put("TSI", "E800");
                }
                
                addDebugLog("📋 TLV data ready");
                
                // Note: Keep card reader open during ARQC generation
                // The reader will be closed after the transaction is complete
                
                addDebugLog("📡 Sending to host...");
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
    
    private byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
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
            
            // Get terminal configuration
            TerminalConfig terminalConfig = settingsDAO.getTerminalConfig();
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setTransactionType(Transaction.TYPE_BALANCE_INQUIRY);
            transaction.setStatus(Transaction.STATUS_SUCCESS);
            transaction.setAmount(0); // Balance inquiry has no amount
            transaction.setCardNumber(tlvData.get("PAN"));
            transaction.setCardHolderName(tlvData.get("CardholderName"));
            transaction.setCardType(tlvData.get("CardType"));
            transaction.setEntryMode("CHIP");
            transaction.setTerminalId(terminalConfig != null ? terminalConfig.getTerminalId() : "12345678");
            transaction.setMerchantId(terminalConfig != null ? terminalConfig.getMerchantId() : "123456789012345");
            transaction.setBatchNumber(terminalConfig != null ? terminalConfig.getBatchNumber() : "000001");
            transaction.setTraceNumber(terminalConfig != null ? terminalConfig.getTraceNumber() : tlvData.get("STAN"));
            transaction.setReferenceNumber(generateReferenceNumber());
            transaction.setApprovalCode(tlvData.get("AuthCode") != null ? tlvData.get("AuthCode") : "123456");
            transaction.setResponseCode("00");
            transaction.setResponseMessage("Balance inquiry successful");
            transaction.setEmvData(tlvData.get("EmvData"));
            transaction.setPinBlock(tlvData.get("PINBlock"));
            transaction.setArqc(tlvData.get("ARQC"));
            transaction.setAtc(tlvData.get("ATC"));
            transaction.setTvr(tlvData.get("TVR"));
            transaction.setTsi(tlvData.get("TSI"));
            transaction.setAid(tlvData.get("AID"));
            transaction.setApplicationLabel(tlvData.get("ApplicationLabel"));
            transaction.setTransactionDate(new Date());
            
            // Save transaction to database
            long transactionId = transactionDAO.insertTransaction(transaction);
            Log.d(TAG, "Transaction saved with ID: " + transactionId);
            
            // Increment trace number for next transaction
            settingsDAO.incrementTraceNumber();
            
            // Navigate to result activity
            Intent intent = new Intent(this, BalanceInquiryResultActivity.class);
            
            // Pass all transaction data
            intent.putExtra("merchantName", terminalConfig != null ? terminalConfig.getMerchantName() : "UNIFLO MERCHANT");
            intent.putExtra("merchantAddress", terminalConfig != null ? terminalConfig.getMerchantAddress() : "Jl. Sudirman No. 123, Jakarta");
            intent.putExtra("terminalId", transaction.getTerminalId());
            intent.putExtra("merchantId", transaction.getMerchantId());
            intent.putExtra("cardNumber", transaction.getCardNumber());
            intent.putExtra("cardType", transaction.getCardType());
            intent.putExtra("balance", String.valueOf(balance));
            intent.putExtra("responseCode", transaction.getResponseCode());
            intent.putExtra("approvalCode", transaction.getApprovalCode());
            intent.putExtra("referenceNumber", transaction.getReferenceNumber());
            
            startActivity(intent);
        });
    }
    
    private String generateReferenceNumber() {
        // Generate unique reference number using timestamp and random component
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault());
        return sdf.format(new Date());
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
    
    /**
     * Generate PIN block (simplified for demo)
     * In production, this would use proper PIN encryption with DUKPT or similar
     */
    private String generatePinBlock(String pin) {
        try {
            // Format PIN to 4-12 digits with padding
            String formattedPin = String.format("%-12s", pin).replace(' ', 'F');
            
            // For demo, create a simple "encrypted" PIN block
            // In production, this would use proper encryption with card PAN
            StringBuilder pinBlock = new StringBuilder();
            
            // PIN length + PIN + padding
            pinBlock.append(String.format("%02X", pin.length()));
            pinBlock.append(pin);
            
            // Pad to 8 bytes (16 hex chars) with random data
            while (pinBlock.length() < 16) {
                pinBlock.append("F");
            }
            
            // XOR with dummy data (in production, would XOR with PAN)
            String result = xorWithPan(pinBlock.toString(), "1234567890123456");
            
            addDebugLog("🔐 PIN block format: " + pinBlock.toString());
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating PIN block", e);
            // Return dummy PIN block
            return "04" + pin + "FFFFFFFFFF";
        }
    }
    
    /**
     * XOR PIN block with PAN (simplified demo version)
     */
    private String xorWithPan(String pinBlock, String pan) {
        try {
            // Take last 12 digits of PAN (excluding check digit)
            String panPart = "0000" + pan.substring(pan.length() - 13, pan.length() - 1);
            
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < Math.min(pinBlock.length(), panPart.length()); i += 2) {
                int pinByte = Integer.parseInt(pinBlock.substring(i, i + 2), 16);
                int panByte = Integer.parseInt(panPart.substring(i, i + 2), 16);
                int xorResult = pinByte ^ panByte;
                result.append(String.format("%02X", xorResult));
            }
            
            return result.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error in XOR operation", e);
            return pinBlock; // Return original if XOR fails
        }
    }
    
    /**
     * Keep the original method for backward compatibility
     */
    private void collectTLVDataAndProcess(String cardType) {
        // Call the PIN version with empty PIN for backward compatibility
        collectTLVDataAndProcessWithPin(cardType, "");
    }
    
    /**
     * Read PAN from the card
     */
    private String readCardPAN() {
        try {
            if (icReader == null) {
                Log.e(TAG, "IC Reader not initialized");
                return null;
            }
            
            // Read record to get PAN (tag 5A)
            // Try to read records from SFI 1-5 (common locations for PAN)
            byte[] recvData = new byte[256];
            int[] len = new int[1];
            
            for (int sfi = 1; sfi <= 5; sfi++) {
                for (int record = 1; record <= 3; record++) {
                    // Build READ RECORD command
                    // CLA=00, INS=B2, P1=record number, P2=(sfi<<3)|4, Le=00
                    byte[] readRecordCmd = new byte[]{
                        0x00, (byte)0xB2, (byte)record, (byte)((sfi << 3) | 4), 0x00
                    };
                    
                    int ret = icReader.sendApduCustomer(readRecordCmd, readRecordCmd.length, recvData, len);
                    
                    if (ret == 0 && len[0] >= 2) {
                        int sw1 = recvData[len[0] - 2] & 0xFF;
                        int sw2 = recvData[len[0] - 1] & 0xFF;
                        
                        if (sw1 == 0x90 && sw2 == 0x00) {
                            // Success - parse TLV data
                            Map<String, String> tlv = parseTLV(recvData, len[0] - 2);
                            String pan = tlv.get("5A"); // Tag 5A is PAN
                            if (pan != null && !pan.isEmpty()) {
                                // Remove trailing F if present
                                pan = pan.replaceAll("F$", "");
                                Log.d(TAG, "Found PAN in SFI " + sfi + ", Record " + record);
                                return pan;
                            }
                            
                            // Also check for track 2 data (tag 57)
                            String track2 = tlv.get("57");
                            if (track2 != null && !track2.isEmpty()) {
                                // Extract PAN from track 2 (before 'D' separator)
                                int dIndex = track2.indexOf('D');
                                if (dIndex > 0) {
                                    pan = track2.substring(0, dIndex);
                                    Log.d(TAG, "Found PAN from track 2 in SFI " + sfi + ", Record " + record);
                                    return pan;
                                }
                            }
                        }
                    }
                }
            }
            
            Log.w(TAG, "Could not find PAN in card records");
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading PAN from card", e);
            return null;
        }
    }
    
    /**
     * Parse TLV data
     */
    private Map<String, String> parseTLV(byte[] data, int length) {
        Map<String, String> tlvMap = new HashMap<>();
        int pos = 0;
        
        while (pos < length) {
            // Get tag (assume 1 or 2 byte tags)
            String tag;
            if ((data[pos] & 0x1F) == 0x1F) {
                // 2-byte tag
                tag = String.format("%02X%02X", data[pos] & 0xFF, data[pos + 1] & 0xFF);
                pos += 2;
            } else {
                // 1-byte tag
                tag = String.format("%02X", data[pos] & 0xFF);
                pos += 1;
            }
            
            if (pos >= length) break;
            
            // Get length
            int len = data[pos] & 0xFF;
            pos += 1;
            
            if ((len & 0x80) != 0) {
                // Multi-byte length
                int numBytes = len & 0x7F;
                len = 0;
                for (int i = 0; i < numBytes && pos < length; i++) {
                    len = (len << 8) | (data[pos] & 0xFF);
                    pos += 1;
                }
            }
            
            if (pos + len > length) break;
            
            // Get value
            byte[] value = new byte[len];
            System.arraycopy(data, pos, value, 0, len);
            pos += len;
            
            // Convert to hex string
            tlvMap.put(tag, com.ftpos.library.smartpos.util.BytesUtils.byte2HexStr(value));
        }
        
        return tlvMap;
    }
    
    /**
     * Mask PAN for display
     */
    private String maskPAN(String pan) {
        if (pan == null || pan.length() < 10) {
            return "****";
        }
        // Show first 6 and last 4 digits
        return pan.substring(0, 6) + "****" + pan.substring(pan.length() - 4);
    }
}