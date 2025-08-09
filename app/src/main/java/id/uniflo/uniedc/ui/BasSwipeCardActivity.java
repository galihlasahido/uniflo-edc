package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import id.uniflo.uniedc.R;

public class BasSwipeCardActivity extends Activity {
    
    private ImageView backButton;
    private TextView titleText;
    private TextView instructionTitle;
    private TextView instructionText1;
    private TextView instructionText2;
    private ImageView cardReaderImage;
    private View cardContainer;
    private TextView statusText;
    private View statusButton;
    
    // Card reader simulation
    private Handler handler = new Handler();
    private boolean isCardDetected = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_swipe_card);
        
        initViews();
        setupListeners();
        startCardDetection();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        instructionTitle = findViewById(R.id.instruction_title);
        instructionText1 = findViewById(R.id.instruction_text1);
        instructionText2 = findViewById(R.id.instruction_text2);
        cardReaderImage = findViewById(R.id.card_reader_image);
        cardContainer = findViewById(R.id.card_container);
        statusText = findViewById(R.id.status_text);
        statusButton = findViewById(R.id.status_button);
        
        // Set title
        titleText.setText("Transfer");
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }
    
    private void startCardDetection() {
        // Simulate checking for card every 2 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isCardDetected) {
                    // Check if card reader is available
                    checkCardReader();
                    handler.postDelayed(this, 2000);
                }
            }
        }, 1000);
    }
    
    private void checkCardReader() {
        // In real implementation, this would check the actual card reader
        // For demo, we'll simulate it
        try {
            // Simulate card detection after 5 seconds
            handler.postDelayed(() -> simulateCardDetected(), 5000);
        } catch (Exception e) {
            updateStatus("Reader error: " + e.getMessage());
        }
    }
    
    private void simulateCardDetected() {
        isCardDetected = true;
        updateStatus("Card detected! Processing...");
        
        // Animate card insertion
        cardContainer.setVisibility(View.VISIBLE);
        cardContainer.animate()
            .translationY(-100)
            .setDuration(1000)
            .withEndAction(() -> {
                // Process card
                processCard();
            });
    }
    
    private void processCard() {
        handler.postDelayed(() -> {
            // Simulate successful card read
            Toast.makeText(this, "Card read successfully", Toast.LENGTH_SHORT).show();
            
            // Return card data to calling activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("card_number", "****-****-****-1234");
            resultIntent.putExtra("card_holder", "JOHN DOE");
            setResult(RESULT_OK, resultIntent);
            
            // Finish after a delay
            handler.postDelayed(() -> finish(), 1500);
        }, 2000);
    }
    
    private void updateStatus(String message) {
        runOnUiThread(() -> {
            statusText.setText("Status : " + message);
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}