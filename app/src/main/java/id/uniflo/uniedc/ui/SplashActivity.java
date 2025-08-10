package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import id.uniflo.uniedc.BuildConfig;
import id.uniflo.uniedc.R;
import id.uniflo.uniedc.manager.ProfileManager;
import id.uniflo.uniedc.manager.TokenManager;
import id.uniflo.uniedc.model.DeviceProfile;
import id.uniflo.uniedc.model.OAuthToken;

public class SplashActivity extends Activity {
    
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    
    private TextView tvStatus;
    private ProgressBar progressBar;
    private ProfileManager profileManager;
    private TokenManager tokenManager;
    private Handler handler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        initViews();
        initManagers();
        startInitialization();
    }
    
    private void initViews() {
        ImageView logo = findViewById(R.id.iv_logo);
        tvStatus = findViewById(R.id.tv_status);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set initial status
        tvStatus.setText("Initializing UniEDC...");
    }
    
    private void initManagers() {
        profileManager = ProfileManager.getInstance(this);
        tokenManager = TokenManager.getInstance(this);
        handler = new Handler();
    }
    
    private void startInitialization() {
        Log.d(TAG, "Starting application initialization");
        
        // Show splash screen for minimum time
        handler.postDelayed(() -> {
            checkDeviceProfile();
        }, SPLASH_DELAY);
    }
    
    private void checkDeviceProfile() {
        updateStatus("Checking authentication...");
        
        Log.d(TAG, "Checking OAuth token");
        
        // Check if we have a valid OAuth token
        if (tokenManager.hasValidToken()) {
            OAuthToken token = tokenManager.getToken();
            Log.d(TAG, "Valid token found for user: " + token.getUserId());
            updateStatus("Authentication valid");
            
            // Check if token needs refresh (less than 10 minutes remaining)
            if (tokenManager.shouldRefreshToken()) {
                Log.d(TAG, "Token needs refresh, remaining time: " + token.getRemainingTime() + " seconds");
                updateStatus("Refreshing authentication...");
                // In production, you would refresh the token here
                // For now, we'll continue with the existing token
            }
            
            // Token is valid, check for profile
            if (profileManager.hasValidProfile()) {
                DeviceProfile profile = profileManager.getCurrentProfile();
                Log.d(TAG, "Profile found: " + profile.getProfileName());
                updateStatus("Loading profile: " + profile.getProfileName());
                
                // Everything valid, go to home
                handler.postDelayed(() -> {
                    navigateToDashboard();
                }, 1000);
            } else {
                // Token valid but no profile, fetch profile
                Log.d(TAG, "Token valid but no profile, fetching profile");
                updateStatus("Loading user profile...");
                
                // In production, fetch profile using the token
                // For now, create a default profile
                if (isDebugMode()) {
                    createDefaultProfile();
                } else {
                    handler.postDelayed(() -> {
                        navigateToDashboard();
                    }, 1000);
                }
            }
        } else {
            // No valid token or token expired
            OAuthToken token = tokenManager.getToken();
            if (token != null) {
                Log.d(TAG, "Token expired, was created at: " + token.getCreatedAt());
                updateStatus("Session expired, please login");
            } else {
                Log.d(TAG, "No token found, first time login required");
                updateStatus("Please login to continue");
            }
            
            // Clear any expired token and profile
            tokenManager.clearToken();
            profileManager.clearProfile();
            
            // Navigate to login
            handler.postDelayed(() -> {
                navigateToLogin();
            }, 1000);
        }
    }
    
    private void createDefaultProfile() {
        updateStatus("Creating default profile...");
        Log.d(TAG, "Creating default development profile");
        
        handler.postDelayed(() -> {
            DeviceProfile defaultProfile = profileManager.createDefaultProfile();
            profileManager.setProfile(defaultProfile);
            
            updateStatus("Default profile created");
            Log.d(TAG, "Default profile created and saved");
            
            handler.postDelayed(() -> {
                navigateToDashboard();
            }, 1000);
        }, 1000);
    }
    
    private void updateStatus(String status) {
        runOnUiThread(() -> {
            tvStatus.setText(status);
            Log.d(TAG, "Status: " + status);
        });
    }
    
    private void navigateToDashboard() {
        Log.d(TAG, "Navigating to dashboard");
        Intent intent = new Intent(this, BasHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void navigateToLogin() {
        Log.d(TAG, "Navigating to login");
        Intent intent = new Intent(this, BasLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private boolean isDebugMode() {
        // Check if app is in debug mode or development environment
        return BuildConfig.DEBUG || 
               android.os.Build.TAGS.contains("test-keys");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}