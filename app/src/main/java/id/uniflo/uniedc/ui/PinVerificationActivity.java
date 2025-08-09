package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import id.uniflo.uniedc.R;

public class PinVerificationActivity extends AppCompatActivity {
    
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PIN_BLOCK = "extra_pin_block";
    public static final int RESULT_PIN_VERIFIED = 100;
    public static final int RESULT_PIN_CANCELLED = 101;
    
    private static final String DEMO_PIN = "123456";
    
    private Toolbar toolbar;
    private TextView tvMessage;
    private TextView tvError;
    private TextView etPin1, etPin2, etPin3, etPin4, etPin5, etPin6;
    private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv0;
    private TextView tvClear, tvBackspace;
    
    private TextView[] pinFields;
    private StringBuilder currentPin = new StringBuilder();
    private int currentFieldIndex = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_verification);
        
        initViews();
        setupToolbar();
        setupNumberPad();
        
        // Set message if provided
        String message = getIntent().getStringExtra(EXTRA_MESSAGE);
        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvMessage = findViewById(R.id.tv_message);
        tvError = findViewById(R.id.tv_error);
        
        etPin1 = findViewById(R.id.et_pin_1);
        etPin2 = findViewById(R.id.et_pin_2);
        etPin3 = findViewById(R.id.et_pin_3);
        etPin4 = findViewById(R.id.et_pin_4);
        etPin5 = findViewById(R.id.et_pin_5);
        etPin6 = findViewById(R.id.et_pin_6);
        
        pinFields = new TextView[]{etPin1, etPin2, etPin3, etPin4, etPin5, etPin6};
        
        // Number pad
        tv1 = findViewById(R.id.tv_1);
        tv2 = findViewById(R.id.tv_2);
        tv3 = findViewById(R.id.tv_3);
        tv4 = findViewById(R.id.tv_4);
        tv5 = findViewById(R.id.tv_5);
        tv6 = findViewById(R.id.tv_6);
        tv7 = findViewById(R.id.tv_7);
        tv8 = findViewById(R.id.tv_8);
        tv9 = findViewById(R.id.tv_9);
        tv0 = findViewById(R.id.tv_0);
        tvClear = findViewById(R.id.tv_clear);
        tvBackspace = findViewById(R.id.tv_backspace);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String title = getIntent().getStringExtra(EXTRA_TITLE);
            getSupportActionBar().setTitle(title != null ? title : "PIN Verification");
        }
    }
    
    private void setupNumberPad() {
        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                addDigit(tv.getText().toString());
            }
        };
        
        tv1.setOnClickListener(numberClickListener);
        tv2.setOnClickListener(numberClickListener);
        tv3.setOnClickListener(numberClickListener);
        tv4.setOnClickListener(numberClickListener);
        tv5.setOnClickListener(numberClickListener);
        tv6.setOnClickListener(numberClickListener);
        tv7.setOnClickListener(numberClickListener);
        tv8.setOnClickListener(numberClickListener);
        tv9.setOnClickListener(numberClickListener);
        tv0.setOnClickListener(numberClickListener);
        
        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPin();
            }
        });
        
        tvBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLastDigit();
            }
        });
    }
    
    private void addDigit(String digit) {
        if (currentFieldIndex < pinFields.length) {
            currentPin.append(digit);
            pinFields[currentFieldIndex].setText("•");
            currentFieldIndex++;
            tvError.setVisibility(View.GONE);
            
            // Check if PIN is complete
            if (currentFieldIndex == pinFields.length) {
                verifyPin();
            }
        }
    }
    
    private void removeLastDigit() {
        if (currentFieldIndex > 0) {
            currentFieldIndex--;
            pinFields[currentFieldIndex].setText("");
            if (currentPin.length() > 0) {
                currentPin.deleteCharAt(currentPin.length() - 1);
            }
            tvError.setVisibility(View.GONE);
        }
    }
    
    private void clearPin() {
        currentPin.setLength(0);
        currentFieldIndex = 0;
        for (TextView field : pinFields) {
            field.setText("");
        }
        tvError.setVisibility(View.GONE);
    }
    
    private void verifyPin() {
        if (currentPin.toString().equals(DEMO_PIN)) {
            // Generate dummy PIN block for demo
            byte[] pinBlock = generateDummyPinBlock();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_PIN_BLOCK, pinBlock);
            setResult(RESULT_PIN_VERIFIED, resultIntent);
            finish();
        } else {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("Incorrect PIN. Please try again.");
            
            // Clear PIN after showing error
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearPin();
                }
            }, 1000);
        }
    }
    
    private void postDelayed(Runnable runnable, long delayMillis) {
        getWindow().getDecorView().postDelayed(runnable, delayMillis);
    }
    
    private byte[] generateDummyPinBlock() {
        // Generate dummy PIN block for demo purposes
        // In real implementation, this would use the SDK's pinpad to encrypt the PIN
        byte[] pinBlock = new byte[8];
        for (int i = 0; i < 8; i++) {
            pinBlock[i] = (byte)(Math.random() * 256);
        }
        return pinBlock;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_PIN_CANCELLED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        setResult(RESULT_PIN_CANCELLED);
        super.onBackPressed();
    }
    
    public static void startForResult(Activity activity, int requestCode, String title, String message) {
        Intent intent = new Intent(activity, PinVerificationActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
//        intent.putExtra(EXTRA_MESSAGE, message);
        activity.startActivityForResult(intent, requestCode);
    }
    
    public static void startForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, PinVerificationActivity.class);
        intent.putExtra(EXTRA_TITLE, "PIN Verification");
        activity.startActivityForResult(intent, requestCode);
    }
}