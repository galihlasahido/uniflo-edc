package id.uniflo.uniedc.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * OAuth Token model for secure authentication
 */
public class OAuthToken {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn; // seconds until expiry
    private Date createdAt;
    private String scope;
    private String userId;
    private String userRole;
    
    public OAuthToken() {
        this.createdAt = new Date();
        this.tokenType = "Bearer";
    }
    
    // Check if token is expired
    public boolean isExpired() {
        if (accessToken == null || createdAt == null) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();
        long tokenCreatedTime = createdAt.getTime();
        long expiryTime = tokenCreatedTime + (expiresIn * 1000); // Convert seconds to milliseconds
        
        // Add 5 minute buffer before actual expiry
        return currentTime >= (expiryTime - 300000);
    }
    
    // Get remaining time in seconds
    public long getRemainingTime() {
        if (isExpired()) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long tokenCreatedTime = createdAt.getTime();
        long expiryTime = tokenCreatedTime + (expiresIn * 1000);
        
        return (expiryTime - currentTime) / 1000; // Return in seconds
    }
    
    // Convert to JSON for storage
    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("access_token", accessToken);
        json.put("refresh_token", refreshToken);
        json.put("token_type", tokenType);
        json.put("expires_in", expiresIn);
        json.put("created_at", createdAt != null ? createdAt.getTime() : 0);
        json.put("scope", scope);
        json.put("user_id", userId);
        json.put("user_role", userRole);
        return json.toString();
    }
    
    // Create from JSON
    public static OAuthToken fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        OAuthToken token = new OAuthToken();
        
        token.accessToken = json.optString("access_token");
        token.refreshToken = json.optString("refresh_token");
        token.tokenType = json.optString("token_type", "Bearer");
        token.expiresIn = json.optLong("expires_in", 3600); // Default 1 hour
        
        long createdAtTime = json.optLong("created_at", 0);
        if (createdAtTime > 0) {
            token.createdAt = new Date(createdAtTime);
        }
        
        token.scope = json.optString("scope");
        token.userId = json.optString("user_id");
        token.userRole = json.optString("user_role");
        
        return token;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }
}