package id.uniflo.uniedc.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import id.uniflo.uniedc.model.DeviceProfile;

public class ProfileManager {
    
    private static final String TAG = "ProfileManager";
    private static final String PREFS_NAME = "device_profile";
    private static final String KEY_PROFILE_JSON = "profile_json";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_LAST_SYNC = "last_sync";
    
    private static ProfileManager instance;
    private Context context;
    private SharedPreferences prefs;
    private DeviceProfile currentProfile;
    
    private ProfileManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadProfile();
    }
    
    public static synchronized ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context);
        }
        return instance;
    }
    
    // Get unique device identifier
    public String getDeviceId() {
        String deviceId = prefs.getString(KEY_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = generateDeviceId();
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
        return deviceId;
    }
    
    private String generateDeviceId() {
        try {
            // Try to get Android ID first
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId != null && !androidId.equals("9774d56d682e549c")) {
                return "UNIEDC_" + androidId;
            }
            
            // Fallback to device info combination
            return "UNIEDC_" + Build.MODEL.replaceAll("[^A-Za-z0-9]", "") + 
                   "_" + Build.SERIAL.replaceAll("[^A-Za-z0-9]", "");
        } catch (Exception e) {
            Log.w(TAG, "Error generating device ID", e);
            return "UNIEDC_" + System.currentTimeMillis();
        }
    }
    
    // Load profile from SharedPreferences
    private void loadProfile() {
        try {
            String profileJson = prefs.getString(KEY_PROFILE_JSON, null);
            if (profileJson != null) {
                currentProfile = profileFromJson(profileJson);
                Log.d(TAG, "Profile loaded: " + (currentProfile != null ? currentProfile.getProfileName() : "null"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile", e);
            currentProfile = null;
        }
    }
    
    // Save profile to SharedPreferences
    private void saveProfile() {
        if (currentProfile != null) {
            try {
                String profileJson = profileToJson(currentProfile);
                prefs.edit()
                    .putString(KEY_PROFILE_JSON, profileJson)
                    .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                    .apply();
                Log.d(TAG, "Profile saved: " + currentProfile.getProfileName());
            } catch (Exception e) {
                Log.e(TAG, "Error saving profile", e);
            }
        }
    }
    
    // Check if device has valid profile
    public boolean hasValidProfile() {
        return currentProfile != null && currentProfile.isValidProfile();
    }
    
    // Get current profile
    public DeviceProfile getCurrentProfile() {
        return currentProfile;
    }
    
    // Set profile (called after fetching from backend)
    public void setProfile(DeviceProfile profile) {
        this.currentProfile = profile;
        if (profile != null) {
            profile.setLastUpdated(new Date());
            profile.setDeviceId(getDeviceId());
        }
        saveProfile();
    }
    
    // Clear profile (logout)
    public void clearProfile() {
        currentProfile = null;
        prefs.edit()
            .remove(KEY_PROFILE_JSON)
            .remove(KEY_LAST_SYNC)
            .apply();
        Log.d(TAG, "Profile cleared");
    }
    
    // Feature access methods
    public boolean canAccessFeature(String featureName) {
        if (!hasValidProfile()) {
            return false;
        }
        
        switch (featureName.toLowerCase()) {
            case "sales":
                return currentProfile.canAccessSales();
            case "balance_inquiry":
                return currentProfile.canAccessBalanceInquiry();
            case "cash_withdrawal":
                return currentProfile.canAccessCashWithdrawal();
            case "transfer":
                return currentProfile.canAccessTransfer();
            case "pin_management":
                return currentProfile.canAccessPinManagement();
            case "settings":
                return currentProfile.canAccessSettings();
            case "reports":
                return currentProfile.canAccessReports();
            default:
                return false;
        }
    }
    
    // Check transaction limits
    public boolean isTransactionAllowed(long amount) {
        if (!hasValidProfile()) {
            return false;
        }
        
        DeviceProfile.FeaturePermissions features = currentProfile.getFeatures();
        return amount <= features.getMaxTransactionAmount();
    }
    
    // Get device info for backend
    public DeviceInfo getDeviceInfo() {
        DeviceInfo info = new DeviceInfo();
        info.deviceId = getDeviceId();
        info.model = Build.MODEL;
        info.manufacturer = Build.MANUFACTURER;
        info.osVersion = Build.VERSION.RELEASE;
        info.appVersion = getAppVersion();
        info.serialNumber = Build.SERIAL;
        
        return info;
    }
    
    private String getAppVersion() {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }
    
    // Simplified profile fetching for development
    public interface ProfileCallback {
        void onSuccess(DeviceProfile profile);
        void onError(String error);
    }
    
    public void fetchProfileFromBackend(String username, String password, ProfileCallback callback) {
        Log.d(TAG, "Mock fetching profile from backend...");
        
        // Mock profile creation based on username
        try {
            DeviceProfile profile = createMockProfile(username);
            setProfile(profile);
            callback.onSuccess(profile);
        } catch (Exception e) {
            callback.onError("Failed to create profile: " + e.getMessage());
        }
    }
    
    // Create mock profile based on username
    private DeviceProfile createMockProfile(String username) {
        DeviceProfile profile = createDefaultProfile();
        
        // Customize profile based on username
        if ("admin".equals(username)) {
            profile.setProfileName("Administrator Profile");
            // Admin has all features (already enabled in default)
        } else if ("merchant".equals(username)) {
            profile.setProfileName("Merchant Profile");
            // Merchant has limited features
            profile.getFeatures().setSettingsEnabled(false);
            profile.getFeatures().setReportsEnabled(false);
            profile.getFeatures().setMaxTransactionAmount(5000000); // 5 million IDR
        } else if ("cashier".equals(username)) {
            profile.setProfileName("Cashier Profile");
            // Cashier has very limited features
            profile.getFeatures().setTransferEnabled(false);
            profile.getFeatures().setPinManagementEnabled(false);
            profile.getFeatures().setSettingsEnabled(false);
            profile.getFeatures().setReportsEnabled(false);
            profile.getFeatures().setMaxTransactionAmount(2000000); // 2 million IDR
        }
        
        return profile;
    }
    
    // Create default profile for development/testing
    public DeviceProfile createDefaultProfile() {
        DeviceProfile profile = new DeviceProfile();
        profile.setDeviceId(getDeviceId());
        profile.setProfileId("DEFAULT_PROFILE");
        profile.setProfileName("Default Development Profile");
        profile.setProfileVersion("1.0");
        profile.setActive(true);
        profile.setLastUpdated(new Date());
        profile.setDeviceModel(Build.MODEL);
        
        // Enable all features for development
        DeviceProfile.FeaturePermissions features = profile.getFeatures();
        features.setSalesEnabled(true);
        features.setBalanceInquiryEnabled(true);
        features.setCashWithdrawalEnabled(true);
        features.setTransferEnabled(true);
        features.setPinManagementEnabled(true);
        features.setSettingsEnabled(true);
        features.setReportsEnabled(true);
        features.setOfflineMode(true);
        features.setMaxTransactionAmount(10000000); // 10 million IDR
        features.setMaxDailyAmount(100000000); // 100 million IDR
        features.setMaxTransactionsPerDay(100);
        
        // Set default settings
        DeviceProfile.DeviceSettings settings = profile.getSettings();
        settings.setServerUrl("https://api.uniflo.id");
        settings.setPrintReceipt(true);
        settings.setSessionTimeout(600);
        
        return profile;
    }
    
    // Simple JSON serialization methods
    private String profileToJson(DeviceProfile profile) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("deviceId", profile.getDeviceId());
        json.put("profileId", profile.getProfileId());
        json.put("profileName", profile.getProfileName());
        json.put("profileVersion", profile.getProfileVersion());
        json.put("isActive", profile.isActive());
        json.put("lastUpdated", profile.getLastUpdated() != null ? profile.getLastUpdated().getTime() : 0);
        json.put("deviceModel", profile.getDeviceModel());
        
        // Serialize features
        JSONObject featuresJson = new JSONObject();
        DeviceProfile.FeaturePermissions features = profile.getFeatures();
        featuresJson.put("salesEnabled", features.isSalesEnabled());
        featuresJson.put("balanceInquiryEnabled", features.isBalanceInquiryEnabled());
        featuresJson.put("cashWithdrawalEnabled", features.isCashWithdrawalEnabled());
        featuresJson.put("transferEnabled", features.isTransferEnabled());
        featuresJson.put("pinManagementEnabled", features.isPinManagementEnabled());
        featuresJson.put("settingsEnabled", features.isSettingsEnabled());
        featuresJson.put("reportsEnabled", features.isReportsEnabled());
        featuresJson.put("offlineMode", features.isOfflineMode());
        featuresJson.put("maxTransactionAmount", features.getMaxTransactionAmount());
        featuresJson.put("maxDailyAmount", features.getMaxDailyAmount());
        featuresJson.put("maxTransactionsPerDay", features.getMaxTransactionsPerDay());
        json.put("features", featuresJson);
        
        // Serialize settings
        JSONObject settingsJson = new JSONObject();
        DeviceProfile.DeviceSettings settings = profile.getSettings();
        settingsJson.put("serverUrl", settings.getServerUrl());
        settingsJson.put("printReceipt", settings.isPrintReceipt());
        settingsJson.put("sessionTimeout", settings.getSessionTimeout());
        json.put("settings", settingsJson);
        
        return json.toString();
    }
    
    private DeviceProfile profileFromJson(String json) throws JSONException {
        JSONObject jsonObj = new JSONObject(json);
        
        DeviceProfile profile = new DeviceProfile();
        profile.setDeviceId(jsonObj.optString("deviceId"));
        profile.setProfileId(jsonObj.optString("profileId"));
        profile.setProfileName(jsonObj.optString("profileName"));
        profile.setProfileVersion(jsonObj.optString("profileVersion"));
        profile.setActive(jsonObj.optBoolean("isActive"));
        
        long lastUpdated = jsonObj.optLong("lastUpdated", 0);
        if (lastUpdated > 0) {
            profile.setLastUpdated(new Date(lastUpdated));
        }
        
        profile.setDeviceModel(jsonObj.optString("deviceModel"));
        
        // Deserialize features
        JSONObject featuresJson = jsonObj.optJSONObject("features");
        if (featuresJson != null) {
            DeviceProfile.FeaturePermissions features = profile.getFeatures();
            features.setSalesEnabled(featuresJson.optBoolean("salesEnabled", true));
            features.setBalanceInquiryEnabled(featuresJson.optBoolean("balanceInquiryEnabled", true));
            features.setCashWithdrawalEnabled(featuresJson.optBoolean("cashWithdrawalEnabled", true));
            features.setTransferEnabled(featuresJson.optBoolean("transferEnabled", true));
            features.setPinManagementEnabled(featuresJson.optBoolean("pinManagementEnabled", true));
            features.setSettingsEnabled(featuresJson.optBoolean("settingsEnabled", true));
            features.setReportsEnabled(featuresJson.optBoolean("reportsEnabled", true));
            features.setOfflineMode(featuresJson.optBoolean("offlineMode", true));
            features.setMaxTransactionAmount(featuresJson.optLong("maxTransactionAmount", 10000000));
            features.setMaxDailyAmount(featuresJson.optLong("maxDailyAmount", 100000000));
            features.setMaxTransactionsPerDay(featuresJson.optInt("maxTransactionsPerDay", 100));
        }
        
        // Deserialize settings
        JSONObject settingsJson = jsonObj.optJSONObject("settings");
        if (settingsJson != null) {
            DeviceProfile.DeviceSettings settings = profile.getSettings();
            settings.setServerUrl(settingsJson.optString("serverUrl", "https://api.uniflo.id"));
            settings.setPrintReceipt(settingsJson.optBoolean("printReceipt", true));
            settings.setSessionTimeout(settingsJson.optInt("sessionTimeout", 600));
        }
        
        return profile;
    }
    
    // Data classes for API communication
    public static class DeviceInfo {
        public String deviceId;
        public String model;
        public String manufacturer;
        public String osVersion;
        public String appVersion;
        public String serialNumber;
    }
    
    public static class ProfileRequest {
        public String username;
        public String password;
        public DeviceInfo deviceInfo;
    }
    
    public static class ProfileResponse {
        public boolean success;
        public String message;
        public DeviceProfile profile;
    }
}