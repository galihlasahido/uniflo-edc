package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import id.uniflo.uniedc.R;

public class CreatePinCardVerificationActivity extends Activity {
    
    // Request codes
    private static final int REQUEST_CARD_READING = 1001;
    
    // Card data
    private String cardNumber = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Skip showing UI - go directly to card reading
        launchCardReader();
    }
    
    private void launchCardReader() {
        Intent intent = new Intent(this, CardReaderActivity.class);
        startActivityForResult(intent, REQUEST_CARD_READING);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CARD_READING) {
            if (resultCode == RESULT_OK) {
                // Card reading successful - go directly to CreatePinModernActivity
                if (data != null) {
                    cardNumber = data.getStringExtra(CardReaderActivity.EXTRA_CARD_NUMBER);
                    proceedToCreatePin();
                } else {
                    // Card reading failed - go back to home
                    finish();
                }
            } else {
                // Card reading failed or cancelled - go back to home
                finish();
            }
        }
    }
    
    private void proceedToCreatePin() {
        if (cardNumber == null || cardNumber.isEmpty()) {
            finish();
            return;
        }
        
        // Navigate to create PIN form with card data
        Intent intent = new Intent(this, CreatePinModernActivity.class);
        intent.putExtra("card_number", cardNumber);
        intent.putExtra("card_verified", true);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Navigate back to home
        Intent intent = new Intent(this, BasHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}