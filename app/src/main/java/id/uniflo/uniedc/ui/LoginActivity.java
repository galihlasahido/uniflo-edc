package id.uniflo.uniedc.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.ISDKProvider;
import id.uniflo.uniedc.security.LoginAttemptManager;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvAppName;
    private TextView tvAppDesc;
    private TextView tvAttemptsWarning;
    
    private LoginAttemptManager loginAttemptManager;
    private CountDownTimer lockCountdownTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_login);
        
        loginAttemptManager = new LoginAttemptManager(this);
        
        initViews();
        setupListeners();
        checkAccountLockStatus();
        
        // Auto-focus on username field when activity opens
        etUsername.requestFocus();
        
        // Initialize SDK Manager
        initializeSDK();
    }
    
    private void initViews() {
        // Input fields (TextInputEditText instead of TextInputLayout)
        etUsername = findViewById(R.id.et_user_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        
        // Additional UI elements
        CheckBox cbRememberMe = findViewById(R.id.cb_remember_me);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        ImageButton btnTogglePassword = findViewById(R.id.btn_toggle_password);
        
        // Create TextInputLayouts for error handling (not in new layout)
        tilUsername = null;
        tilPassword = null;
        tvAppName = null;
        tvAppDesc = null;
        tvAttemptsWarning = null;
        
        // Handle password visibility toggle
        final boolean[] isPasswordVisible = {false};
        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
                isPasswordVisible[0] = false;
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
                isPasswordVisible[0] = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });
        
        // Handle forgot password
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Please contact administrator", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
        
        // Enable Enter key on password field to submit login
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || 
                    actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && 
                     event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    performLogin();
                    return true;
                }
                return false;
            }
        });
    }
    
    private void checkAccountLockStatus() {
        if (loginAttemptManager.isAccountLocked()) {
            showAccountLockedUI();
        } else {
            updateAttemptsWarning();
        }
    }
    
    private void updateAttemptsWarning() {
        int remainingAttempts = loginAttemptManager.getRemainingAttempts();
        int failedAttempts = loginAttemptManager.getFailedAttempts();
        
        if (failedAttempts > 0 && remainingAttempts > 0) {
            // Show warning in toast since we don't have tvAttemptsWarning in new layout
            Toast.makeText(this, String.format("Warning: %d attempts remaining", remainingAttempts), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showAccountLockedUI() {
        // Disable login inputs
        etUsername.setEnabled(false);
        etPassword.setEnabled(false);
        btnLogin.setEnabled(false);
        btnLogin.setText("Account Locked");
        
        // Start countdown timer
        if (lockCountdownTimer != null) {
            lockCountdownTimer.cancel();
        }
        
        lockCountdownTimer = new CountDownTimer(loginAttemptManager.getRemainingLockTime(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeRemaining = loginAttemptManager.getRemainingLockTimeFormatted();
                btnLogin.setText("Locked - " + timeRemaining);
            }
            
            @Override
            public void onFinish() {
                // Re-enable login
                etUsername.setEnabled(true);
                etPassword.setEnabled(true);
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");
                updateAttemptsWarning();
            }
        }.start();
    }
    
    private void performLogin() {
        // Check if account is locked
        if (loginAttemptManager.isAccountLocked()) {
            showAccountLockedDialog();
            return;
        }
        
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Clear previous errors (no TextInputLayout in new design)
        etUsername.setError(null);
        etPassword.setError(null);
        
        // Validate inputs
        if (username.isEmpty()) {
            etUsername.setError("User ID is required");
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }
        
        // Show loading dialog
        ProgressDialog loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Signing in...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        
        // Simulate authentication delay
        new Handler().postDelayed(() -> {
            // Simple demo login (username: admin, password: 1234)
            if ("admin".equals(username) && "1234".equals(password)) {
                // Success - reset failed attempts and navigate to dashboard
                loginAttemptManager.resetFailedAttempts();
                
                loadingDialog.setMessage("Login successful! Redirecting...");
                
                // Small delay before navigation for better UX
                new Handler().postDelayed(() -> {
                    loadingDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, BasHomeActivity.class);
                    startActivity(intent);
                    finish();
                }, 500);
            } else {
                // Failed login
                loadingDialog.dismiss();
                loginAttemptManager.recordFailedAttempt();
                
                if (loginAttemptManager.isAccountLocked()) {
                    showAccountLockedDialog();
                    showAccountLockedUI();
                } else {
                    int remainingAttempts = loginAttemptManager.getRemainingAttempts();
                    String message = "Invalid username or password";
                    
                    if (remainingAttempts <= 2) {
                        message += String.format("\n%d attempts remaining before account lock", remainingAttempts);
                    }
                    
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    updateAttemptsWarning();
                }
            }
        }, 1500); // 1.5 second delay to simulate authentication
    }
    
    private void showAccountLockedDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Account Locked")
            .setMessage("Too many failed login attempts. Your account has been locked for 30 minutes for security reasons.")
            .setIcon(R.drawable.ic_lock)
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .show();
    }
    
    private void initializeSDK() {
        // Initialize SDK Manager
        SDKManager sdkManager = SDKManager.getInstance();
        sdkManager.init(this);
        
        // Initialize SDK in background
        sdkManager.initializeSDK(new ISDKProvider.IInitCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // SDK initialized successfully
                    }
                });
            }
            
            @Override
            public void onError(int errorCode, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // SDK init failed, will use emulator mode
                    }
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lockCountdownTimer != null) {
            lockCountdownTimer.cancel();
        }
    }
}