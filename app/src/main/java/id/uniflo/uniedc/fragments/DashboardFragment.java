package id.uniflo.uniedc.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.database.SecureSettingsDAO;
import id.uniflo.uniedc.database.TerminalConfig;
import id.uniflo.uniedc.database.Transaction;
import id.uniflo.uniedc.database.TransactionDAO;
import id.uniflo.uniedc.ui.DashboardModernActivity;
import id.uniflo.uniedc.ui.PrinterTestActivity;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.IPrinter;

public class DashboardFragment extends Fragment {
    
    private TextView tvGreeting;
    private TextView tvMerchantName;
    private TextView tvTotalAmount;
    private TextView tvTransactionCount;
    private ImageView ivProfile;
    
    private LinearLayout actionToko;
    private LinearLayout actionSaldoPending;
    private LinearLayout actionMutasi;
    private LinearLayout actionDisbursment;
    
    private SecureSettingsDAO settingsDAO;
    private TransactionDAO transactionDAO;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        settingsDAO = new SecureSettingsDAO(getContext());
        transactionDAO = new TransactionDAO(getContext());
        
        initViews(view);
        loadMerchantInfo();
        setupClickListeners();
        loadDashboardData();
        
        return view;
    }
    
    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvMerchantName = view.findViewById(R.id.tv_merchant_name);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        // Transaction count view doesn't exist in layout, comment it out
        // tvTransactionCount = view.findViewById(R.id.tv_transaction_count);
        ivProfile = view.findViewById(R.id.iv_profile);
        
        actionToko = view.findViewById(R.id.action_toko);
        actionSaldoPending = view.findViewById(R.id.action_saldo_pending);
        actionMutasi = view.findViewById(R.id.action_mutasi);
        actionDisbursment = view.findViewById(R.id.action_disbursment);
        
    }
    
    private void loadMerchantInfo() {
        // Load merchant name from settings
        TerminalConfig config = settingsDAO.getTerminalConfig();
        if (config != null && config.getMerchantName() != null) {
            tvMerchantName.setText(config.getMerchantName());
        }
        
        // Set greeting based on time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(timeFormat.format(new Date()));
        
        String greeting;
        if (hour < 10) {
            greeting = "Selamat pagi!";
        } else if (hour < 15) {
            greeting = "Selamat siang!";
        } else if (hour < 18) {
            greeting = "Selamat sore!";
        } else {
            greeting = "Selamat malam!";
        }
        
        tvGreeting.setText(greeting);
    }
    
    private void setupClickListeners() {
        actionToko.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Toko clicked", Toast.LENGTH_SHORT).show();
        });
        
        actionSaldoPending.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Saldo Pending clicked", Toast.LENGTH_SHORT).show();
        });
        
        actionMutasi.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mutasi clicked", Toast.LENGTH_SHORT).show();
        });
        
        actionDisbursment.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DashboardModernActivity.class);
            startActivity(intent);
        });
        
        ivProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Profile clicked", Toast.LENGTH_SHORT).show();
        });

    }
    
    private void loadDashboardData() {
        // Load real transaction data from database
        TransactionDAO.TransactionSummary summary = transactionDAO.getTodayTransactionSummary();
        
        // Format currency
        DecimalFormat formatter = new DecimalFormat("#,###");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        
        // Set total amount from actual transactions
        long totalAmount = summary.getTotalAmount();
        tvTotalAmount.setText("Rp" + formatter.format(totalAmount));
        
        // Update other UI elements if they exist
        // Transaction count is commented out as the view doesn't exist in layout
        // int transactionCount = summary.getTotalCount();
        // tvTransactionCount.setText("Anda melakukan " + transactionCount + " transaksi hari ini");
    }
    
    private void printLastTransactionReceipt() {
        // Get last transaction from database
        java.util.List<Transaction> transactions = transactionDAO.getAllTransactions();
        Transaction lastTransaction = null;
        if (!transactions.isEmpty()) {
            lastTransaction = transactions.get(0); // Already sorted by date DESC
        }
        
        final Transaction transactionToPrint = lastTransaction;
        
        // Connect to service and get printer instance
        com.ftpos.library.smartpos.servicemanager.ServiceManager.bindPosServer(getContext(), new com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback() {
            @Override
            public void onSuccess() {
                // Get printer instance
                com.ftpos.library.smartpos.printer.Printer printer = com.ftpos.library.smartpos.printer.Printer.getInstance(getContext());
                
                if (printer == null) {
                    Toast.makeText(getContext(), "Failed to get printer instance", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    // Open printer
                    int ret = printer.open();
                    if (ret != com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS) {
                        Toast.makeText(getContext(), "Failed to open printer: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Start caching
                    ret = printer.startCaching();
                    if (ret != com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS) {
                        Toast.makeText(getContext(), "Failed to start caching: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Set gray level
                    ret = printer.setGray(3);
                    if (ret != com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS) {
                        Toast.makeText(getContext(), "Failed to set gray: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Get terminal config
                    TerminalConfig config = settingsDAO.getTerminalConfig();
                    
                    // Print header centered
                    printer.setAlignStyle(com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_CENTER);
                    printer.printStr(config != null ? config.getMerchantName() : "UNIFLO EDC");
                    printer.printStr("\n");
                    printer.printStr("LAST TRANSACTION");
                    printer.printStr("\n");
                    printer.printStr("================================");
                    printer.printStr("\n\n");
                    
                    // Print details left aligned
                    printer.setAlignStyle(com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_LEFT);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    
                    if (transactionToPrint != null) {
                        printer.printStr("Date/Time: " + sdf.format(transactionToPrint.getTransactionDate()));
                        printer.printStr("\n");
                        printer.printStr("Type: " + transactionToPrint.getTransactionType());
                        printer.printStr("\n");
                        printer.printStr("Card: " + transactionToPrint.getMaskedCardNumber());
                        printer.printStr("\n");
                        if (transactionToPrint.getAmount() > 0) {
                            DecimalFormat formatter = new DecimalFormat("#,###");
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
                            symbols.setGroupingSeparator('.');
                            formatter.setDecimalFormatSymbols(symbols);
                            printer.printStr("Amount: Rp" + formatter.format(transactionToPrint.getAmount()));
                            printer.printStr("\n");
                        }
                        printer.printStr("Status: " + transactionToPrint.getStatus());
                        printer.printStr("\n");
                        printer.printStr("Ref No: " + transactionToPrint.getReferenceNumber());
                        printer.printStr("\n");
                        if (transactionToPrint.getApprovalCode() != null) {
                            printer.printStr("Approval: " + transactionToPrint.getApprovalCode());
                            printer.printStr("\n");
                        }
                    } else {
                        printer.printStr("Date/Time: " + sdf.format(new Date()));
                        printer.printStr("\n\n");
                        printer.setAlignStyle(com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_CENTER);
                        printer.printStr("*** NO TRANSACTION DATA ***");
                        printer.setAlignStyle(com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_LEFT);
                    }
                    
                    printer.printStr("--------------------------------");
                    printer.printStr("\n\n");
                    
                    // Print footer centered
                    printer.setAlignStyle(com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_CENTER);
                    printer.printStr("*** REPRINT ***");
                    printer.printStr("\n\n");
                    
                    // Execute print with callback
                    printer.print(new com.ftpos.library.smartpos.printer.OnPrinterCallback() {
                        @Override
                        public void onSuccess() {
                            printer.feed(32); // Feed paper
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Receipt printed successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            
                            // Close printer and unbind service
                            try {
                                printer.close();
                                com.ftpos.library.smartpos.servicemanager.ServiceManager.unbindPosServer();
                            } catch (Exception e) {
                                android.util.Log.e("DashboardFragment", "Error closing printer", e);
                            }
                        }
                        
                        @Override
                        public void onError(int errorCode) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Print error: " + String.format("0x%x", errorCode), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            
                            // Close printer and unbind service
                            try {
                                printer.close();
                                com.ftpos.library.smartpos.servicemanager.ServiceManager.unbindPosServer();
                            } catch (Exception e) {
                                android.util.Log.e("DashboardFragment", "Error closing printer", e);
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Print failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("DashboardFragment", "Print exception", e);
                }
            }
            
            @Override
            public void onFail(int errorCode) {
                Toast.makeText(getContext(), "Service connection failed: " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Inner class for QuickAction data model
    public static class QuickAction {
        private int iconResource;
        private String title;
        private String description;
        
        public QuickAction(int iconResource, String title, String description) {
            this.iconResource = iconResource;
            this.title = title;
            this.description = description;
        }
        
        public int getIconResource() {
            return iconResource;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
    }
}