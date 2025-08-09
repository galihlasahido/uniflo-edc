package id.uniflo.uniedc.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for handling receipt printing using the actual Feitian SDK
 */
public class PrinterUtil {
    
    private static final String TAG = "PrinterUtil";
    private static final int LINE_WIDTH = 32; // Standard receipt width in characters
    
    /**
     * Print a transfer receipt
     */
    public static void printTransferReceipt(Context context, String transactionId, 
                                           String amount, String recipient, 
                                           String bank, String accountNumber, 
                                           String note) {
        
        StringBuilder receipt = new StringBuilder();
        
        // Header
        receipt.append(centerText("UniEDC")).append("\n");
        receipt.append(centerText("Electronic Data Capture")).append("\n");
        receipt.append(repeatChar('=', LINE_WIDTH)).append("\n\n");
        
        // Transaction Type
        receipt.append(centerText("TRANSFER RECEIPT")).append("\n\n");
        
        // Date and Time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        receipt.append("Date: ").append(sdf.format(new Date())).append("\n");
        receipt.append("Transaction ID: ").append(transactionId).append("\n\n");
        
        receipt.append(repeatChar('-', LINE_WIDTH)).append("\n");
        
        // Transfer Details
        receipt.append("Transfer To:\n");
        receipt.append("Bank: ").append(bank).append("\n");
        receipt.append("Account: ").append(accountNumber).append("\n");
        receipt.append("Name: ").append(recipient).append("\n\n");
        
        if (note != null && !note.isEmpty()) {
            receipt.append("Note: ").append(note).append("\n\n");
        }
        
        // Amount
        receipt.append(repeatChar('-', LINE_WIDTH)).append("\n");
        receipt.append("Amount: ").append(amount).append("\n");
        receipt.append(repeatChar('-', LINE_WIDTH)).append("\n\n");
        
        // Footer
        receipt.append(centerText("Transaction Successful")).append("\n");
        receipt.append(centerText("Thank You!")).append("\n\n");
        receipt.append(repeatChar('=', LINE_WIDTH)).append("\n");
        receipt.append("\n\n\n"); // Paper feed
        
        // Send to printer
        printReceipt(context, receipt.toString());
    }
    
    /**
     * Print a withdrawal receipt
     */
    public static void printWithdrawalReceipt(Context context, String transactionId, 
                                            String amount) {
        
        StringBuilder receipt = new StringBuilder();
        
        // Header
        receipt.append(centerText("UniEDC")).append("\n");
        receipt.append(centerText("Electronic Data Capture")).append("\n");
        receipt.append(repeatChar('=', LINE_WIDTH)).append("\n\n");
        
        // Transaction Type
        receipt.append(centerText("CASH WITHDRAWAL")).append("\n\n");
        
        // Date and Time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        receipt.append("Date: ").append(sdf.format(new Date())).append("\n");
        receipt.append("Transaction ID: ").append(transactionId).append("\n\n");
        
        receipt.append(repeatChar('-', LINE_WIDTH)).append("\n");
        
        // Amount
        receipt.append("Withdrawal Amount:\n");
        receipt.append(amount).append("\n");
        receipt.append(repeatChar('-', LINE_WIDTH)).append("\n\n");
        
        // Footer
        receipt.append(centerText("Transaction Successful")).append("\n");
        receipt.append(centerText("Please Take Your Cash")).append("\n");
        receipt.append(centerText("Thank You!")).append("\n\n");
        receipt.append(repeatChar('=', LINE_WIDTH)).append("\n");
        receipt.append("\n\n\n"); // Paper feed
        
        // Send to printer
        printReceipt(context, receipt.toString());
    }
    
    /**
     * Print a sales/payment receipt
     */
    public static void printSalesReceipt(Context context, String transactionId, 
                                       String amount, String paymentMethod, 
                                       String cardNumber) {
        
        StringBuilder receipt = new StringBuilder();
        
        // Header
        receipt.append(centerText("UniEDC")).append("\n");
        receipt.append(centerText("Electronic Data Capture")).append("\n");
        receipt.append(repeatChar('=', LINE_WIDTH)).append("\n\n");
        
        // Transaction Type
        receipt.append(centerText("PAYMENT RECEIPT")).append("\n\n");
        
        // Date and Time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        receipt.append("Date: ").append(sdf.format(new Date())).append("\n");
        receipt.append("Transaction ID: ").append(transactionId).append("\n\n");
        
        receipt.append(repeatChar('-', LINE_WIDTH)).append("\n");
        
        // Payment Details
        receipt.append("Payment Method: ").append(paymentMethod).append("\n");
        if (cardNumber != null && !cardNumber.isEmpty()) {
            receipt.append("Card Number: ").append(cardNumber).append("\n");
        }
        receipt.append("\n");
        
        // Amount
        receipt.append("Amount: ").append(amount).append("\n");
        receipt.append(repeatChar('-', LINE_WIDTH)).append("\n\n");
        
        // Footer
        receipt.append(centerText("Transaction Successful")).append("\n");
        receipt.append(centerText("Thank You For Your Payment!")).append("\n\n");
        receipt.append(repeatChar('=', LINE_WIDTH)).append("\n");
        receipt.append("\n\n\n"); // Paper feed
        
        // Send to printer
        printReceipt(context, receipt.toString());
    }
    
    /**
     * Print raw receipt content directly (from JSON or other sources)
     */
    public static void printRawReceipt(Context context, String receiptContent) {
        if (receiptContent == null || receiptContent.trim().isEmpty()) {
            Toast.makeText(context, "No receipt content to print", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Starting raw receipt print");
        
        // Connect to service and print
        ServiceManager.bindPosServer(context, new OnServiceConnectCallback() {
            @Override
            public void onSuccess() {
                // Get printer instance
                Printer printer = Printer.getInstance(context);
                
                if (printer == null) {
                    Toast.makeText(context, "Failed to get printer instance", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    // Open printer
                    int ret = printer.open();
                    if (ret != ErrCode.ERR_SUCCESS) {
                        Toast.makeText(context, "Failed to open printer: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Start caching
                    ret = printer.startCaching();
                    if (ret != ErrCode.ERR_SUCCESS) {
                        Toast.makeText(context, "Failed to start caching: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Set gray level
                    ret = printer.setGray(3);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        Toast.makeText(context, "Failed to set gray: " + String.format("0x%x", ret), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Check paper status
                    PrintStatus printStatus = new PrintStatus();
                    ret = printer.getStatus(printStatus);
                    if (ret == ErrCode.ERR_SUCCESS && !printStatus.getmIsHavePaper()) {
                        Toast.makeText(context, "Printer out of paper!", Toast.LENGTH_SHORT).show();
                        printer.close();
                        return;
                    }
                    
                    // Print header centered
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
                    printer.printStr("UniEDC");
                    printer.printStr("\n");
                    printer.printStr("================================");
                    printer.printStr("\n\n");
                    
                    // Print receipt content left-aligned
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_LEFT);
                    
                    // Split content by lines and print each line
                    String[] lines = receiptContent.split("\n");
                    for (String line : lines) {
                        printer.printStr(line);
                        printer.printStr("\n");
                    }
                    
                    // Print footer
                    printer.printStr("\n");
                    printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
                    printer.printStr("================================");
                    printer.printStr("\n");
                    printer.printStr("Thank You!");
                    printer.printStr("\n\n\n"); // Paper feed
                    
                    // Print cached data
                    printer.print(new OnPrinterCallback() {
                        @Override
                        public void onSuccess() {
                            printer.feed(32); // Feed paper
                            Toast.makeText(context, "Receipt printed successfully!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Receipt printed successfully");
                            
                            // Close printer and unbind service
                            try {
                                printer.close();
                                ServiceManager.unbindPosServer();
                            } catch (Exception e) {
                                Log.e(TAG, "Error closing printer", e);
                            }
                        }
                        
                        @Override
                        public void onError(int errorCode) {
                            Toast.makeText(context, "Print failed: " + String.format("0x%x", errorCode), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Print failed with error: " + String.format("0x%x", errorCode));
                            
                            // Close printer and unbind service
                            try {
                                printer.close();
                                ServiceManager.unbindPosServer();
                            } catch (Exception e) {
                                Log.e(TAG, "Error closing printer", e);
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Print exception: " + e.getMessage());
                    Toast.makeText(context, "Print error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    printer.close();
                }
            }
            
            @Override
            public void onFail(int errorCode) {
                Toast.makeText(context, "Failed to bind printer service: " + errorCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to bind printer service with error: " + errorCode);
            }
        });
    }
    
    /**
     * Core print function using actual Feitian SDK
     */
    private static void printReceipt(Context context, String receiptData) {
        // This method now delegates to printRawReceipt for consistency
        printRawReceipt(context, receiptData);
    }
    
    /**
     * Center text within the line width
     */
    private static String centerText(String text) {
        if (text.length() >= LINE_WIDTH) {
            return text;
        }
        
        int padding = (LINE_WIDTH - text.length()) / 2;
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < padding; i++) {
            result.append(" ");
        }
        result.append(text);
        
        return result.toString();
    }
    
    /**
     * Repeat a character n times
     */
    private static String repeatChar(char c, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(c);
        }
        return result.toString();
    }
}