package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.SDKType;
import id.uniflo.uniedc.sdk.interfaces.*;

/**
 * Example activity demonstrating how to use the SDK wrapper architecture
 * This shows initialization and basic usage of all SDK components
 */
public class SDKTestActivity extends Activity {
    
    private static final String TAG = "SDKTestActivity";
    
    private TextView tvStatus;
    private Button btnInitSDK;
    private Button btnTestPrinter;
    private Button btnTestCardReader;
    private Button btnTestDevice;
    private Button btnSwitchSDK;
    
    private SDKManager sdkManager;
    private ProgressDialog progressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_test);
        
        // Initialize SDK Manager
        sdkManager = SDKManager.getInstance();
        sdkManager.init(this);
        
        // Find views
        tvStatus = findViewById(R.id.tvStatus);
        btnInitSDK = findViewById(R.id.btnInitSDK);
        btnTestPrinter = findViewById(R.id.btnTestPrinter);
        btnTestCardReader = findViewById(R.id.btnTestCardReader);
        btnTestDevice = findViewById(R.id.btnTestDevice);
        btnSwitchSDK = findViewById(R.id.btnSwitchSDK);
        
        // Set click listeners
        btnInitSDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeSDK();
            }
        });
        
        btnTestPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testPrinter();
            }
        });
        
        btnTestCardReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testCardReader();
            }
        });
        
        btnTestDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDevice();
            }
        });
        
        btnSwitchSDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSDKType();
            }
        });
        
        updateStatus();
    }
    
    private void updateStatus() {
        SDKType currentType = sdkManager.getCurrentSDKType();
        boolean initialized = sdkManager.isInitialized();
        
        String status = "SDK Type: " + currentType.getDisplayName() + "\n";
        status += "Initialized: " + (initialized ? "Yes" : "No");
        
        if (initialized && sdkManager.getCurrentProvider() != null) {
            ISDKProvider provider = sdkManager.getCurrentProvider();
            status += "\nSDK Name: " + provider.getSDKName();
            status += "\nSDK Version: " + provider.getSDKVersion();
        }
        
        tvStatus.setText(status);
        
        // Enable/disable buttons
        btnTestPrinter.setEnabled(initialized);
        btnTestCardReader.setEnabled(initialized);
        btnTestDevice.setEnabled(initialized);
    }
    
    private void initializeSDK() {
        showProgress("Initializing SDK...");
        
        sdkManager.initializeSDK(new ISDKProvider.IInitCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        Toast.makeText(SDKTestActivity.this, "SDK initialized successfully", Toast.LENGTH_SHORT).show();
                        updateStatus();
                    }
                });
            }
            
            @Override
            public void onError(int errorCode, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        Toast.makeText(SDKTestActivity.this, "SDK init failed: " + message, Toast.LENGTH_LONG).show();
                        updateStatus();
                    }
                });
            }
        });
    }
    
    private void testPrinter() {
        IPrinter printer = sdkManager.getPrinter();
        if (printer == null) {
            Toast.makeText(this, "Printer not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Initialize printer
        int ret = printer.init();
        if (ret != 0) {
            Toast.makeText(this, "Printer init failed: " + ret, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Print test receipt
        printer.setAlignment(1); // Center
        printer.setTextSize(1); // Large
        printer.printText("SDK TEST RECEIPT\n\n");
        
        printer.setAlignment(0); // Left
        printer.setTextSize(0); // Normal
        printer.printText("SDK Type: " + sdkManager.getCurrentSDKType().getDisplayName() + "\n");
        printer.printText("Time: " + new java.util.Date() + "\n");
        printer.printText("================================\n");
        printer.printText("This is a test print\n");
        printer.printText("to verify SDK wrapper\n");
        printer.printText("================================\n\n");
        
        // Print QR code
        printer.printQRCode("https://uniflo.id", 4);
        
        printer.feedPaper(3);
        
        Toast.makeText(this, "Test print sent", Toast.LENGTH_SHORT).show();
    }
    
    private void testCardReader() {
        ICardReader cardReader = sdkManager.getCardReader();
        if (cardReader == null) {
            Toast.makeText(this, "Card reader not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress("Waiting for card...");
        
        // Initialize and open card reader
        int ret = cardReader.init();
        if (ret != 0) {
            hideProgress();
            Toast.makeText(this, "Card reader init failed: " + ret, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Wait for any type of card
        cardReader.open(ICardReader.CARD_TYPE_MAG | ICardReader.CARD_TYPE_IC | ICardReader.CARD_TYPE_NFC, 
                       30, // 30 second timeout
                       new ICardReader.ICardDetectListener() {
            @Override
            public void onCardDetected(int cardType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        String cardTypeName = "";
                        switch (cardType) {
                            case ICardReader.CARD_TYPE_MAG:
                                cardTypeName = "Magnetic";
                                break;
                            case ICardReader.CARD_TYPE_IC:
                                cardTypeName = "IC/Chip";
                                break;
                            case ICardReader.CARD_TYPE_NFC:
                                cardTypeName = "NFC/Contactless";
                                break;
                        }
                        Toast.makeText(SDKTestActivity.this, "Card detected: " + cardTypeName, Toast.LENGTH_LONG).show();
                        
                        // Close reader
                        cardReader.close();
                    }
                });
            }
            
            @Override
            public void onTimeout() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        Toast.makeText(SDKTestActivity.this, "Card detection timeout", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(int errorCode, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        Toast.makeText(SDKTestActivity.this, "Card reader error: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onCardRemoved() {
                // Card removed during waiting
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SDKTestActivity.this, "Card removed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void testDevice() {
        IDevice device = sdkManager.getDevice();
        if (device == null) {
            Toast.makeText(this, "Device not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Initialize device
        int ret = device.init();
        if (ret != 0) {
            Toast.makeText(this, "Device init failed: " + ret, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show device info
        String info = "Device Information:\n";
        info += "Serial: " + device.getSerialNumber() + "\n";
        info += "Model: " + device.getModel() + "\n";
        info += "Firmware: " + device.getFirmwareVersion() + "\n";
        info += "Battery: " + device.getBatteryLevel() + "%\n";
        info += "Charging: " + (device.isCharging() ? "Yes" : "No") + "\n";
        
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
        
        // Test beep
        device.beep(200);
        
        // Test LED
        device.setLed(0, true);
        
        // Turn off LED after 1 second
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                device.setLed(0, false);
            }
        }, 1000);
    }
    
    private void switchSDKType() {
        // Cycle through SDK types
        SDKType currentType = sdkManager.getCurrentSDKType();
        SDKType newType;
        
        switch (currentType) {
            case FEITIAN:
                newType = SDKType.EMULATOR;
                break;
            case EMULATOR:
                newType = SDKType.FEITIAN;
                break;
            default:
                newType = SDKType.EMULATOR;
                break;
        }
        
        sdkManager.setSDKType(newType);
        updateStatus();
        
        Toast.makeText(this, "Switched to " + newType.getDisplayName(), Toast.LENGTH_SHORT).show();
    }
    
    private void showProgress(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    
    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Don't release SDK Manager here as it's a singleton
        // It should be released when the app terminates
    }
}