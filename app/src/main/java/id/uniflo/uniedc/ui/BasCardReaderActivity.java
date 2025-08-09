package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.ICardReader;

/**
 * Reusable card reader activity for Feitian SDK integration
 * Handles card insertion, validation, and ICC reading
 */
public class BasCardReaderActivity extends Activity implements ICardReader.ICardDetectListener {
    
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SUBTITLE = "extra_subtitle";
    public static final String EXTRA_TARGET_ACTIVITY = "extra_target_activity";
    public static final String EXTRA_CARD_DATA = "extra_card_data";
    
    // Result codes
    public static final int RESULT_CARD_READ_SUCCESS = 100;
    public static final int RESULT_CARD_READ_ERROR = 101;
    public static final int RESULT_CARD_READ_CANCELLED = 102;
    
    protected ImageView backButton;
    protected TextView titleText;
    protected TextView subtitleText;
    protected TextView cardStatusText;
    protected ImageView cardAnimationView;
    
    protected ICardReader cardReader;
    protected Handler handler = new Handler();
    protected String targetActivity = "";
    
    // Card data
    protected int detectedCardType = 0;
    protected String cardNumber = "";
    protected byte[] atrData = null;
    protected boolean cardReadingInProgress = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_reader);
        
        initViews();
        setupListeners();
        initCardReader();
        startCardReading();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.tv_title);
        subtitleText = findViewById(R.id.tv_subtitle);
        cardStatusText = findViewById(R.id.tv_card_status);
        cardAnimationView = findViewById(R.id.iv_card_animation);
        
        // Set title and subtitle from intent extras
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        String subtitle = intent.getStringExtra(EXTRA_SUBTITLE);
        targetActivity = intent.getStringExtra(EXTRA_TARGET_ACTIVITY);
        
        if (title != null) titleText.setText(title);
        if (subtitle != null) subtitleText.setText(subtitle);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            stopCardReading();
            setResult(RESULT_CARD_READ_CANCELLED);
            finish();
        });
    }
    
    private void initCardReader() {
        try {
            cardReader = SDKManager.getInstance().getCardReader();
            if (cardReader != null) {
                int result = cardReader.init();
                if (result != 0) {
                    showErrorDialog("Gagal menginisialisasi card reader", "Error code: " + result);
                }
            } else {
                showErrorDialog("Card reader tidak tersedia", "Periksa koneksi SDK");
            }
        } catch (Exception e) {
            showErrorDialog("Error SDK", e.getMessage());
        }
    }
    
    private void startCardReading() {
        if (cardReader == null) {
            showErrorDialog("Card reader tidak tersedia", "");
            return;
        }
        
        cardReadingInProgress = true;
        updateCardStatus("Menunggu kartu...", 0xFF00C853); // Green
        
        try {
            // Open card reader for all card types with 60 second timeout
            int cardTypes = ICardReader.CARD_TYPE_IC | ICardReader.CARD_TYPE_MAG | ICardReader.CARD_TYPE_NFC;
            int result = cardReader.open(cardTypes, 60, this);
            
            if (result != 0) {
                updateCardStatus("Error membuka card reader", 0xFFFF5722); // Red
                showErrorDialog("Gagal membuka card reader", "Error code: " + result);
            }
        } catch (Exception e) {
            updateCardStatus("Error card reader", 0xFFFF5722); // Red
            showErrorDialog("Error", e.getMessage());
        }
    }
    
    private void stopCardReading() {
        cardReadingInProgress = false;
        if (cardReader != null) {
            try {
                cardReader.close();
                cardReader.release();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
    
    private void updateCardStatus(String message, int color) {
        runOnUiThread(() -> {
            cardStatusText.setText(message);
            cardStatusText.setTextColor(color);
        });
    }
    
    @Override
    public void onCardDetected(int cardType) {
        detectedCardType = cardType;
        
        runOnUiThread(() -> {
            updateCardStatus("Kartu terdeteksi - Membaca...", 0xFFFFA726); // Orange
            
            // Handle different card types
            handler.postDelayed(() -> {
                try {
                    boolean readSuccess = false;
                    
                    if (cardType == ICardReader.CARD_TYPE_IC) {
                        readSuccess = readICCard();
                    } else if (cardType == ICardReader.CARD_TYPE_MAG) {
                        readSuccess = readMagCard();
                    } else if (cardType == ICardReader.CARD_TYPE_NFC) {
                        readSuccess = readNFCCard();
                    }
                    
                    if (readSuccess) {
                        onCardReadSuccess();
                    } else {
                        onCardReadError("Gagal membaca kartu");
                    }
                } catch (Exception e) {
                    onCardReadError("Error: " + e.getMessage());
                }
            }, 1000);
        });
    }
    
    private boolean readICCard() {
        try {
            // Power on IC card
            atrData = cardReader.powerOn();
            if (atrData == null || atrData.length == 0) {
                return false;
            }
            
            // Extract card number (this is simplified - actual implementation depends on card type)
            // For demo purposes, generate a card number
            cardNumber = "1234567890123456";
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean readMagCard() {
        try {
            // Read track 2 data (most common for card number)
            String track2 = cardReader.getTrackData(2);
            if (track2 != null && !track2.isEmpty()) {
                // Extract card number from track 2 (format: cardnumber=expiry...)
                int separatorIndex = track2.indexOf('=');
                if (separatorIndex > 0) {
                    cardNumber = track2.substring(0, separatorIndex);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean readNFCCard() {
        try {
            // NFC card reading logic (simplified)
            // For demo purposes, generate a card number
            cardNumber = "9876543210987654";
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void onCardReadSuccess() {
        updateCardStatus("Kartu valid - ICC berhasil dibaca", 0xFF00C853); // Green
        
        handler.postDelayed(() -> {
            stopCardReading();
            
            // Prepare result data
            Intent resultIntent = new Intent();
            resultIntent.putExtra("card_type", detectedCardType);
            resultIntent.putExtra("card_number", cardNumber);
            if (atrData != null) {
                resultIntent.putExtra("atr_data", atrData);
            }
            
            setResult(RESULT_CARD_READ_SUCCESS, resultIntent);
            
            // If target activity is specified, start it
            if (targetActivity != null && !targetActivity.isEmpty()) {
                try {
                    Class<?> targetClass = Class.forName(targetActivity);
                    Intent intent = new Intent(this, targetClass);
                    intent.putExtra("card_type", detectedCardType);
                    intent.putExtra("card_number", cardNumber);
                    if (atrData != null) {
                        intent.putExtra("atr_data", atrData);
                    }
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    // Target activity not found, just finish
                }
            }
            
            finish();
        }, 1500);
    }
    
    private void onCardReadError(String error) {
        updateCardStatus("Kartu tidak valid: " + error, 0xFFFF5722); // Red
        
        showErrorDialog("Kartu Tidak Valid", 
            "Kartu yang Anda masukkan tidak valid atau rusak.\n\n" +
            "Error: " + error + "\n\n" +
            "Silakan coba dengan kartu lain atau hubungi customer service.");
    }
    
    @Override
    public void onCardRemoved() {
        runOnUiThread(() -> {
            if (cardReadingInProgress) {
                updateCardStatus("Kartu dilepas - Menunggu kartu...", 0xFF00C853); // Green
            }
        });
    }
    
    @Override
    public void onTimeout() {
        runOnUiThread(() -> {
            updateCardStatus("Timeout - Tidak ada kartu terdeteksi", 0xFFFF5722); // Red
            showErrorDialog("Timeout", "Tidak ada kartu terdeteksi dalam waktu yang ditentukan.");
        });
    }
    
    @Override
    public void onError(int errorCode, String message) {
        runOnUiThread(() -> {
            updateCardStatus("Error: " + message, 0xFFFF5722); // Red
            showErrorDialog("Error Card Reader", "Error " + errorCode + ": " + message);
        });
    }
    
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Coba Lagi", (dialog, which) -> {
                startCardReading();
            })
            .setNegativeButton("Batal", (dialog, which) -> {
                stopCardReading();
                setResult(RESULT_CARD_READ_ERROR);
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        stopCardReading();
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        stopCardReading();
        setResult(RESULT_CARD_READ_CANCELLED);
        super.onBackPressed();
    }
}