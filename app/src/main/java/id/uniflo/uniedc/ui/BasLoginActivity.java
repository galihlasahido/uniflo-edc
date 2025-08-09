package id.uniflo.uniedc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
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

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.database.SettingsDAO;
import id.uniflo.uniedc.security.LoginAttemptManager;

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
            // Save credentials if remember me is checked
            if (cbRememberMe.isChecked()) {
                saveCredentials(userId);
            } else {
                clearSavedCredentials();
            }
            
            // Reset login attempts
            loginAttemptManager.resetAttempts();
            
            // Navigate to main activity
            Intent intent = new Intent(this, DashboardModernActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
    
    @Override
    public void onBackPressed() {
        // Prevent back press on login screen
        moveTaskToBack(true);
    }
}