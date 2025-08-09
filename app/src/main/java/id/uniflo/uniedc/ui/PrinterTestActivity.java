package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;

import id.uniflo.uniedc.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrinterTestActivity extends Activity {
    
    private static final String TAG = "PrinterTestActivity";
    
    private TextView tvLog;
    private Button btnTestPrint;
    private Button btnGetPrinterStatus;
    private Button btnInitPrinter;
    
    private Printer printer = null;
    private boolean isServiceConnected = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_test);
        
        initViews();
        
        // Try to connect to service first
        logMsg("Attempting to connect to POS service...");
        connectToService();
    }
    
    private void initViews() {
        tvLog = findViewById(R.id.tv_log);
        btnTestPrint = findViewById(R.id.btn_test_print);
        btnGetPrinterStatus = findViewById(R.id.btn_get_status);
        btnInitPrinter = findViewById(R.id.btn_init_printer);
        
        btnInitPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializePrinter();
            }
        });
        
        btnGetPrinterStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPrinterStatus();
            }
        });
        
        btnTestPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testPrint();
            }
        });
    }
    
    private void connectToService() {
        try {
            ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
                @Override
                public void onSuccess() {
                    logMsg("✓ Service connected successfully!");
                    isServiceConnected = true;
                    
                    // Try to get printer instance
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initializePrinter();
                        }
                    });
                }
                
                @Override
                public void onFail(int errorCode) {
                    logMsg("✗ Service connection failed: " + errorCode);
                    isServiceConnected = false;
                }
            });
        } catch (Exception e) {
            logMsg("✗ Exception connecting to service: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializePrinter() {
        try {
            logMsg("\n--- Initializing Printer ---");
            
            if (!isServiceConnected) {
                logMsg("✗ Service not connected, cannot initialize printer");
                return;
            }
            
            // Get printer instance
            printer = Printer.getInstance(this);
            
            if (printer == null) {
                logMsg("✗ Failed to get Printer instance - null");
                return;
            }
            
            logMsg("✓ Got Printer instance");
            
            // Open printer
            int ret = printer.open();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("✗ Failed to open printer: " + String.format("0x%x", ret));
                return;
            }
            
            logMsg("✓ Printer opened successfully");
            
        } catch (Exception e) {
            logMsg("✗ Exception initializing printer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void getPrinterStatus() {
        if (printer == null) {
            logMsg("✗ Printer not initialized");
            return;
        }
        
        try {
            logMsg("\n--- Getting Printer Status ---");
            
            PrintStatus printStatus = new PrintStatus();
            int ret = printer.getStatus(printStatus);
            
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("✗ Failed to get status: " + String.format("0x%x", ret));
                return;
            }
            
            logMsg("✓ Status retrieved:");
            logMsg("  Temperature: " + printStatus.getmTemperature());
            logMsg("  Gray: " + printStatus.getmGray());
            logMsg("  Has Paper: " + printStatus.getmIsHavePaper());
            
            if (!printStatus.getmIsHavePaper()) {
                logMsg("⚠ WARNING: Printer out of paper!");
            }
            
        } catch (Exception e) {
            logMsg("✗ Exception getting status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testPrint() {
        if (printer == null) {
            logMsg("✗ Printer not initialized");
            return;
        }
        
        try {
            logMsg("\n--- Starting Print Test ---");
            
            // Start caching
            int ret = printer.startCaching();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("✗ Failed to start caching: " + String.format("0x%x", ret));
                return;
            }
            logMsg("✓ Caching started");
            
            // Set gray level
            ret = printer.setGray(3);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("✗ Failed to set gray: " + String.format("0x%x", ret));
                return;
            }
            logMsg("✓ Gray level set to 3");
            
            // Check paper status
            PrintStatus printStatus = new PrintStatus();
            ret = printer.getStatus(printStatus);
            if (ret == ErrCode.ERR_SUCCESS && !printStatus.getmIsHavePaper()) {
                logMsg("✗ Printer out of paper!");
                return;
            }
            
            // Print header centered
            printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
            printer.printStr("=== PRINTER TEST ===\n");
            printer.printStr("UniEDC Test Receipt\n");
            printer.printStr("========================\n\n");
            
            // Print details left aligned
            printer.setAlignStyle(AlignStyle.PRINT_STYLE_LEFT);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            printer.printStr("Date: " + sdf.format(new Date()) + "\n");
            printer.printStr("Test Type: Basic Print\n");
            printer.printStr("Status: Testing...\n\n");
            
            // Print some test lines
            printer.printStr("Line 1: ABCDEFGHIJKLMNOPQRSTUVWXYZ\n");
            printer.printStr("Line 2: abcdefghijklmnopqrstuvwxyz\n");
            printer.printStr("Line 3: 0123456789\n");
            printer.printStr("Line 4: !@#$%^&*()_+-=[]{}\\|;:'\",.<>?/\n\n");
            
            // Print footer centered
            printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
            printer.printStr("--- END OF TEST ---\n\n");
            
            logMsg("✓ Print data prepared");
            
            // Execute print with callback
            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    printer.feed(32); // Feed paper
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMsg("✓ Print SUCCESS!");
                        }
                    });
                }
                
                @Override
                public void onError(int errorCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMsg("✗ Print ERROR: " + String.format("0x%x", errorCode));
                        }
                    });
                }
            });
            
            logMsg("✓ Print command sent, waiting for callback...");
            
        } catch (Exception e) {
            logMsg("✗ Exception during print: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void logMsg(final String msg) {
        Log.d(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String current = tvLog.getText().toString();
                tvLog.setText(current + "\n" + msg);
                
                // Auto scroll to bottom
                tvLog.post(new Runnable() {
                    @Override
                    public void run() {
                        final int scrollAmount = tvLog.getLayout().getLineTop(tvLog.getLineCount()) - tvLog.getHeight();
                        if (scrollAmount > 0) {
                            tvLog.scrollTo(0, scrollAmount);
                        } else {
                            tvLog.scrollTo(0, 0);
                        }
                    }
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up
        if (printer != null) {
            try {
                printer.close();
                logMsg("Printer closed");
            } catch (Exception e) {
                Log.e(TAG, "Error closing printer", e);
            }
        }
        
        // Unbind service
        try {
            ServiceManager.unbindPosServer();
            logMsg("Service unbound");
        } catch (Exception e) {
            Log.e(TAG, "Error unbinding service", e);
        }
    }
}