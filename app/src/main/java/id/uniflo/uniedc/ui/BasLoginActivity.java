package id.uniflo.uniedc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import id.uniflo.uniedc.BuildConfig;
import id.uniflo.uniedc.R;
import id.uniflo.uniedc.database.SettingsDAO;
import id.uniflo.uniedc.security.LoginAttemptManager;
import id.uniflo.uniedc.manager.ProfileManager;
import id.uniflo.uniedc.manager.TokenManager;
import id.uniflo.uniedc.model.DeviceProfile;
import id.uniflo.uniedc.model.OAuthToken;

public class BasLoginActivity extends AppCompatActivity {

    private EditText etUserId;
    private EditText etPassword;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private Button btnLogin;
    private ImageButton btnTogglePassword;
    private TextView tvTime;
    
    // Navigation elements (commented out - not in layout)
    // private FrameLayout navBack;
    // private FrameLayout navHome;
    // private FrameLayout navRecent;
    
    private boolean isPasswordVisible = false;
    private LoginAttemptManager loginAttemptManager;
    private SettingsDAO settingsDAO;
    private ProfileManager profileManager;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bas_activity_login);
        
        // Initialize components
        initializeViews();
        setupListeners();
        // updateTime(); // tvTime not in layout
        
        // Initialize managers
        loginAttemptManager = new LoginAttemptManager(this);
        settingsDAO = new SettingsDAO(this);
        profileManager = ProfileManager.getInstance(this);
        tokenManager = TokenManager.getInstance(this);
        
        // Load remembered credentials if any
        loadRememberedCredentials();
    }
    
    private void initializeViews() {
        etUserId = findViewById(R.id.et_user_id);
        etPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        // tvTime = findViewById(R.id.tv_time); // Not in layout
        
        // Navigation not in layout
        // navBack = findViewById(R.id.nav_back);
        // navHome = findViewById(R.id.nav_home);
        // navRecent = findViewById(R.id.nav_recent);
        
        // Set autofocus to user ID field
        etUserId.requestFocus();
    }
    
    private void setupListeners() {
        // Login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        
        // Toggle password visibility
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
        
        // Forgot password
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
        
        // Handle Enter key on password field to trigger login
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && 
                     event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        
        // Navigation buttons - commented out as elements not in layout
        /*
        navBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle home navigation
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        
        navRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle recent apps - this is system level, just show a message
                Toast.makeText(BasLoginActivity.this, "Recent apps", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }
    
    private void updateTime() {
        // Method commented out - tvTime not in layout
        /*
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvTime.setText(currentTime);
        
        // Update time every minute
        tvTime.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
            }
        }, 60000);
        */
    }
    
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_on);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }
    
    private void attemptLogin() {
        String userId = etUserId.getText().toString().trim();
        String password = etPassword.getText().toString();
        
        // Validate input
        if (userId.isEmpty()) {
            etUserId.setError("Please enter User ID");
            etUserId.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Please enter Password");
            etPassword.requestFocus();
            return;
        }
        
        // Check if account is locked
        if (loginAttemptManager.isAccountLocked()) {
            long remainingTime = loginAttemptManager.getRemainingLockTime();
            String message = String.format("Account locked. Try again in %d minutes", 
                    remainingTime / 60000);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }
        
        // Perform login validation
        if (validateCredentials(userId, password)) {
            // Show loading
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");
            
            // Fetch profile from backend
            fetchProfileAndLogin(userId, password);
        } else {
            // Record failed attempt
            loginAttemptManager.recordFailedAttempt();
            int remainingAttempts = loginAttemptManager.getRemainingAttempts();
            
            if (remainingAttempts > 0) {
                String message = String.format("Invalid credentials. %d attempts remaining", 
                        remainingAttempts);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Account locked due to multiple failed attempts", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private boolean validateCredentials(String userId, String password) {
        // TODO: Implement actual credential validation
        // For now, use default credentials
        return userId.equals("admin") && password.equals("admin123");
    }
    
    private void saveCredentials(String userId) {
        settingsDAO.setSetting("remembered_user_id", userId);
        settingsDAO.setSetting("remember_me", "true");
    }
    
    private void clearSavedCredentials() {
        settingsDAO.setSetting("remembered_user_id", "");
        settingsDAO.setSetting("remember_me", "false");
    }
    
    private void loadRememberedCredentials() {
        String rememberMe = settingsDAO.getSetting("remember_me", "false");
        if ("true".equals(rememberMe)) {
            String userId = settingsDAO.getSetting("remembered_user_id", "");
            etUserId.setText(userId);
            cbRememberMe.setChecked(true);
        }
    }
    
    private void showForgotPasswordDialog() {
        // TODO: Implement forgot password functionality
        Toast.makeText(this, "Please contact your administrator to reset password", 
                Toast.LENGTH_LONG).show();
    }
    
    private void fetchProfileAndLogin(String userId, String password) {
        // Use mock API for development, real API for production
        if (BuildConfig.DEBUG) {
            // Development mode - use mock API
            fetchProfileMock(userId, password);
        } else {
            // Production mode - use real API
            fetchProfileReal(userId, password);
        }
    }
    
    private void fetchProfileMock(String userId, String password) {
        profileManager.fetchProfileFromBackend(userId, password, new ProfileManager.ProfileCallback() {
            @Override
            public void onSuccess(DeviceProfile profile) {
                runOnUiThread(() -> {
                    // Create and save OAuth token
                    OAuthToken token = createMockOAuthToken(userId, profile);
                    tokenManager.saveToken(token);
                    Log.d("BasLoginActivity", "OAuth token saved for user: " + userId);
                    
                    // Save credentials if remember me is checked
                    if (cbRememberMe.isChecked()) {
                        saveCredentials(userId);
                    } else {
                        clearSavedCredentials();
                    }
                    
                    // Reset login attempts
                    loginAttemptManager.resetAttempts();
                    
                    // Navigate to dashboard
                    Intent intent = new Intent(BasLoginActivity.this, BasHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(BasLoginActivity.this, "Failed to fetch profile: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void fetchProfileReal(String userId, String password) {
        profileManager.fetchProfileFromBackend(userId, password, new ProfileManager.ProfileCallback() {
            @Override
            public void onSuccess(DeviceProfile profile) {
                runOnUiThread(() -> {
                    // Create and save OAuth token
                    OAuthToken token = createMockOAuthToken(userId, profile);
                    tokenManager.saveToken(token);
                    Log.d("BasLoginActivity", "OAuth token saved for user: " + userId);
                    
                    // Save credentials if remember me is checked
                    if (cbRememberMe.isChecked()) {
                        saveCredentials(userId);
                    } else {
                        clearSavedCredentials();
                    }
                    
                    // Reset login attempts
                    loginAttemptManager.resetAttempts();
                    
                    // Navigate to dashboard
                    Intent intent = new Intent(BasLoginActivity.this, BasHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(BasLoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back press on login screen
        moveTaskToBack(true);
    }
    
    /**
     * Create a mock OAuth token for development/testing
     * In production, this would come from your authentication server
     */
    private OAuthToken createMockOAuthToken(String userId, DeviceProfile profile) {
        OAuthToken token = new OAuthToken();
        
        // Set basic token properties
        token.setAccessToken(generateMockAccessToken());
        token.setRefreshToken(generateMockRefreshToken());
        token.setTokenType("Bearer");
        
        // Set expiry time (1 hour from now)
        token.setExpiresIn(3600); // 1 hour in seconds
        token.setCreatedAt(new Date());
        
        // Set user information
        token.setUserId(userId);
        
        // Set role based on user type
        if ("admin".equals(userId)) {
            token.setUserRole("ADMIN");
            token.setScope("read write admin");
        } else if ("merchant".equals(userId)) {
            token.setUserRole("MERCHANT");
            token.setScope("read write");
        } else {
            token.setUserRole("USER");
            token.setScope("read");
        }
        
        return token;
    }
    
    /**
     * Generate a mock access token
     * In production, this comes from your OAuth server
     */
    private String generateMockAccessToken() {
        // Generate a random token-like string
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 64; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return token.toString();
    }
    
    /**
     * Generate a mock refresh token
     */
    private String generateMockRefreshToken() {
        // Generate a random token-like string
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 128; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return token.toString();
    }
}