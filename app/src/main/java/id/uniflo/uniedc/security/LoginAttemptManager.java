package id.uniflo.uniedc.security;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginAttemptManager {
    
    private static final String PREFS_NAME = "login_security_prefs";
    private static final String KEY_FAILED_ATTEMPTS = "failed_attempts";
    private static final String KEY_LOCK_TIMESTAMP = "lock_timestamp";
    private static final String KEY_IS_LOCKED = "is_locked";
    
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 30 * 60 * 1000; // 30 minutes in milliseconds
    
    private final Context context;
    private final SharedPreferences prefs;
    
    public LoginAttemptManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Record a failed login attempt
     */
    public void recordFailedAttempt() {
        int attempts = getFailedAttempts() + 1;
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts).apply();
        
        if (attempts >= MAX_ATTEMPTS) {
            lockAccount();
        }
    }
    
    /**
     * Reset failed attempts on successful login
     */
    public void resetFailedAttempts() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putBoolean(KEY_IS_LOCKED, false)
            .putLong(KEY_LOCK_TIMESTAMP, 0)
            .apply();
    }
    
    /**
     * Get current number of failed attempts
     */
    public int getFailedAttempts() {
        return prefs.getInt(KEY_FAILED_ATTEMPTS, 0);
    }
    
    /**
     * Get remaining attempts before lock
     */
    public int getRemainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - getFailedAttempts());
    }
    
    /**
     * Check if account is locked
     */
    public boolean isAccountLocked() {
        if (!prefs.getBoolean(KEY_IS_LOCKED, false)) {
            return false;
        }
        
        long lockTimestamp = prefs.getLong(KEY_LOCK_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();
        
        // Check if lock duration has expired
        if (currentTime - lockTimestamp >= LOCK_DURATION) {
            // Unlock the account but don't reset attempts
            prefs.edit().putBoolean(KEY_IS_LOCKED, false).apply();
            return false;
        }
        
        return true;
    }
    
    /**
     * Lock the account
     */
    private void lockAccount() {
        prefs.edit()
            .putBoolean(KEY_IS_LOCKED, true)
            .putLong(KEY_LOCK_TIMESTAMP, System.currentTimeMillis())
            .apply();
    }
    
    /**
     * Get remaining lock time in milliseconds
     */
    public long getRemainingLockTime() {
        if (!isAccountLocked()) {
            return 0;
        }
        
        long lockTimestamp = prefs.getLong(KEY_LOCK_TIMESTAMP, 0);
        long elapsedTime = System.currentTimeMillis() - lockTimestamp;
        return Math.max(0, LOCK_DURATION - elapsedTime);
    }
    
    /**
     * Get remaining lock time as formatted string
     */
    public String getRemainingLockTimeFormatted() {
        long remainingMillis = getRemainingLockTime();
        if (remainingMillis == 0) {
            return "";
        }
        
        long minutes = (remainingMillis / 1000) / 60;
        long seconds = (remainingMillis / 1000) % 60;
        
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Force unlock account (admin override)
     */
    public void forceUnlock() {
        resetFailedAttempts();
    }
    
    /**
     * Reset attempts (called after successful login)
     */
    public void resetAttempts() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putBoolean(KEY_IS_LOCKED, false)
            .apply();
    }
}