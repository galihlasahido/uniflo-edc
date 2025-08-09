package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import android.content.Intent;

import id.uniflo.uniedc.R;

public class TransferModernActivity extends Activity {
    
    private ImageView backButton, historyButton;
    private TextView tvAmount, tvAccountName;
    private Spinner spinnerDestinationBank;
    private EditText etAccountNumber, etAmount, etNotes;
    private Button btnVerifyAccount, btn100k, btn500k, btn1m, btnTransfer;
    private EditText pin1, pin2, pin3, pin4, pin5, pin6;
    
    private boolean accountVerified = false;
    private String selectedBank = "";
    private Handler handler = new Handler();
    
    // Request codes
    private static final int REQUEST_CARD_READING = 1001;
    private static final int REQUEST_PIN_ENTRY = 1002;
    
    // Card data
    private String cardNumber = "";
    private String enteredPin = "";
    private int transferAmount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_modern);
        
        initViews();
        setupListeners();
        
        // Launch card reader immediately
        launchCardReader();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        historyButton = findViewById(R.id.history_button);
        tvAmount = findViewById(R.id.tv_amount);
        tvAccountName = findViewById(R.id.tv_account_name);
        
        spinnerDestinationBank = findViewById(R.id.spinner_destination_bank);
        etAccountNumber = findViewById(R.id.et_account_number);
        etAmount = findViewById(R.id.et_amount);
        etNotes = findViewById(R.id.et_notes);
        
        // Setup bank spinner
        setupBankSpinner();
        
        btnVerifyAccount = findViewById(R.id.btn_verify_account);
        btn100k = findViewById(R.id.btn_100k);
        btn500k = findViewById(R.id.btn_500k);
        btn1m = findViewById(R.id.btn_1m);
        btnTransfer = findViewById(R.id.btn_transfer);
        
        pin1 = findViewById(R.id.pin_1);
        pin2 = findViewById(R.id.pin_2);
        pin3 = findViewById(R.id.pin_3);
        pin4 = findViewById(R.id.pin_4);
        pin5 = findViewById(R.id.pin_5);
        pin6 = findViewById(R.id.pin_6);
    }
    
    private void setupBankSpinner() {
        // List of Indonesian banks
        String[] banks = {
            "Pilih Bank Tujuan",
            "Bank Central Asia (BCA)",
            "Bank Mandiri",
            "Bank Negara Indonesia (BNI)",
            "Bank Rakyat Indonesia (BRI)",
            "Bank CIMB Niaga",
            "Bank Danamon",
            "Bank Permata",
            "Bank BTN",
            "Bank OCBC NISP",
            "Bank Maybank Indonesia",
            "Bank Panin",
            "Bank BTPN",
            "Bank Mega",
            "Bank Sinarmas",
            "Bank Commonwealth",
            "Bank BCA Syariah",
            "Bank Syariah Indonesia (BSI)",
            "Bank Muamalat",
            "Bank Jago",
            "Bank Neo Commerce",
            "SeaBank"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, banks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestinationBank.setAdapter(adapter);
        
        spinnerDestinationBank.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position > 0) {
                    selectedBank = banks[position];
                } else {
                    selectedBank = "";
                }
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedBank = "";
            }
        });
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        historyButton.setOnClickListener(v -> {
            Toast.makeText(this, "Riwayat transfer antar bank", Toast.LENGTH_SHORT).show();
        });
        
        // Account verification
        btnVerifyAccount.setOnClickListener(v -> verifyAccount());
        
        etAccountNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                accountVerified = false;
                tvAccountName.setText("-");
                btnVerifyAccount.setVisibility(View.VISIBLE);
            }
        });
        
        // Amount input
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateAmountDisplay();
            }
        });
        
        // Quick amount buttons
        btn100k.setOnClickListener(v -> setAmount(100000));
        btn500k.setOnClickListener(v -> setAmount(500000));
        btn1m.setOnClickListener(v -> setAmount(1000000));
        
        // PIN input
        setupPinInput();
        
        // Transfer button
        btnTransfer.setOnClickListener(v -> processTransfer());
    }
    
    private void setupPinInput() {
        // Hide PIN inputs since we'll use SalesPinActivity
        pin1.setVisibility(View.GONE);
        pin2.setVisibility(View.GONE);
        pin3.setVisibility(View.GONE);
        pin4.setVisibility(View.GONE);
        pin5.setVisibility(View.GONE);
        pin6.setVisibility(View.GONE);
        
        View pinSection = findViewById(R.id.pin_section);
        if (pinSection != null) {
            pinSection.setVisibility(View.GONE);
        }
    }
    
    private void verifyAccount() {
        // Check if bank is selected
        if (selectedBank.isEmpty()) {
            Toast.makeText(this, "Pilih bank tujuan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String accountNumber = etAccountNumber.getText().toString().trim();
        
        if (accountNumber.isEmpty()) {
            Toast.makeText(this, "Masukkan nomor rekening", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (accountNumber.length() < 10) {
            Toast.makeText(this, "Nomor rekening tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        btnVerifyAccount.setEnabled(false);
        btnVerifyAccount.setText("Memverifikasi...");
        
        // Simulate account verification
        handler.postDelayed(() -> {
            accountVerified = true;
            // Generate dummy name based on account number
            String name = generateAccountName(accountNumber);
            tvAccountName.setText(name);
            
            btnVerifyAccount.setEnabled(true);
            btnVerifyAccount.setText("Terverifikasi ✓");
            btnVerifyAccount.setVisibility(View.GONE);
            
            Toast.makeText(this, "Rekening " + selectedBank + " terverifikasi", Toast.LENGTH_SHORT).show();
        }, 1500);
    }
    
    private String generateAccountName(String accountNumber) {
        String[] names = {
            "Ahmad Fauzi",
            "Siti Nurhaliza",
            "Budi Santoso",
            "Dewi Kartika",
            "Rudi Hermawan",
            "Maya Putri",
            "Andi Wijaya",
            "Linda Susanti"
        };
        
        // Use account number to select a name
        int index = Math.abs(accountNumber.hashCode()) % names.length;
        return names[index];
    }
    
    private void setAmount(int amount) {
        etAmount.setText(String.valueOf(amount));
    }
    
    private void updateAmountDisplay() {
        try {
            String text = etAmount.getText().toString().replaceAll("[^0-9]", "");
            if (!text.isEmpty()) {
                int amount = Integer.parseInt(text);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                String formattedAmount = formatter.format(amount).replace("Rp", "Rp ");
                tvAmount.setText(formattedAmount);
            } else {
                tvAmount.setText("Rp 0");
            }
        } catch (NumberFormatException e) {
            tvAmount.setText("Rp 0");
        }
    }
    
    private void launchCardReader() {
        // Check if bank and account are ready first
        if (selectedBank.isEmpty()) {
            Toast.makeText(this, "Pilih bank tujuan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!accountVerified) {
            Toast.makeText(this, "Verifikasi rekening tujuan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, CardReaderActivity.class);
        startActivityForResult(intent, REQUEST_CARD_READING);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CARD_READING) {
            if (resultCode == RESULT_OK) {
                // Card reading successful
                if (data != null) {
                    cardNumber = data.getStringExtra(CardReaderActivity.EXTRA_CARD_NUMBER);
                    // Card read successfully, now user can proceed with transfer
                    Toast.makeText(this, "Kartu berhasil dibaca. Silakan lanjutkan transfer antar bank.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Gagal membaca data kartu", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Card reading failed or cancelled
                Toast.makeText(this, "Pembacaan kartu dibatalkan", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == REQUEST_PIN_ENTRY) {
            if (resultCode == RESULT_OK) {
                // PIN entered successfully
                if (data != null) {
                    enteredPin = data.getStringExtra("pin_entered");
                    // Execute inter-bank transfer with PIN
                    executeTransfer();
                } else {
                    Toast.makeText(this, "PIN tidak valid", Toast.LENGTH_SHORT).show();
                }
            } else {
                // PIN entry cancelled
                Toast.makeText(this, "PIN dibatalkan", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void processTransfer() {
        // Validate bank selection
        if (selectedBank.isEmpty()) {
            Toast.makeText(this, "Pilih bank tujuan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate account
        if (!accountVerified) {
            Toast.makeText(this, "Verifikasi rekening terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate amount
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Masukkan jumlah transfer", Toast.LENGTH_SHORT).show();
            return;
        }
        
        transferAmount = Integer.parseInt(amountStr);
        if (transferAmount < 10000) {
            Toast.makeText(this, "Jumlah minimum Rp 10.000", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (transferAmount > 50000000) {
            Toast.makeText(this, "Jumlah maksimum Rp 50.000.000", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate card
        if (cardNumber.isEmpty()) {
            Toast.makeText(this, "Silakan masukkan kartu terlebih dahulu", Toast.LENGTH_SHORT).show();
            launchCardReader();
            return;
        }
        
        // Launch PIN entry using SalesPinActivity
        Intent intent = new Intent(this, SalesPinActivity.class);
        intent.putExtra(SalesPinActivity.EXTRA_CARD_NUMBER, cardNumber);
        intent.putExtra(SalesPinActivity.EXTRA_AMOUNT, (long)transferAmount);
        intent.putExtra(SalesPinActivity.EXTRA_TRANSACTION_TYPE, "transfer");
        startActivityForResult(intent, REQUEST_PIN_ENTRY);
    }
    
    private void executeTransfer() {
        // Show confirmation dialog first
        showConfirmationDialog(transferAmount);
    }
    
    private void showConfirmationDialog(int amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(amount).replace("Rp", "Rp ");
        
        String message = "Transfer Antar Bank\n\n" +
                        "Bank Tujuan: " + selectedBank + "\n" +
                        "Nama: " + tvAccountName.getText().toString() + "\n" +
                        "Rekening: " + etAccountNumber.getText().toString() + "\n\n" +
                        "Jumlah: " + formattedAmount;
        
        if (!etNotes.getText().toString().trim().isEmpty()) {
            message += "\n\nCatatan: " + etNotes.getText().toString();
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Konfirmasi Transfer")
            .setMessage(message)
            .setPositiveButton("Kirim", (dialog, which) -> performTransfer())
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void performTransfer() {
        // Show processing dialog
        AlertDialog processingDialog = new AlertDialog.Builder(this)
            .setTitle("Memproses")
            .setMessage("Sedang memproses transfer...")
            .setCancelable(false)
            .create();
        processingDialog.show();
        
        // Simulate transfer processing
        handler.postDelayed(() -> {
            processingDialog.dismiss();
            showSuccessDialog();
        }, 3000);
    }
    
    private void showSuccessDialog() {
        String amountStr = etAmount.getText().toString();
        int amount = Integer.parseInt(amountStr);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedAmount = formatter.format(amount).replace("Rp", "Rp ");
        
        new AlertDialog.Builder(this)
            .setTitle("Transfer Antar Bank Berhasil")
            .setMessage("Transfer antar bank " + formattedAmount + " berhasil diproses.\n\n" +
                       "Bank: " + selectedBank + "\n" +
                       "Penerima: " + tvAccountName.getText().toString() + "\n" +
                       "No. Rekening: " + etAccountNumber.getText().toString() + "\n\n" +
                       "No. Referensi: TRF" + System.currentTimeMillis() + "\n" +
                       "Estimasi: 2-3 menit (real-time) atau 1 hari kerja (SKN)")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    private class PinTextWatcher implements TextWatcher {
        private EditText currentBox;
        private EditText nextBox;
        
        public PinTextWatcher(EditText currentBox, EditText nextBox) {
            this.currentBox = currentBox;
            this.nextBox = nextBox;
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextBox != null) {
                nextBox.requestFocus();
            }
        }
    }
}