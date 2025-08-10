# OAuth Token Authentication Flow

## Overview
The application now uses OAuth 2.0 token-based authentication with secure storage and automatic expiry checking. Tokens are encrypted using Android Keystore for maximum security.

## Authentication Flow

### First Time Login
```
1. App Launch → SplashActivity
2. Check for valid OAuth token → Not found
3. Navigate to → BasLoginActivity
4. User enters credentials
5. Validate credentials
6. Generate OAuth token (1 hour expiry)
7. Save token securely using Android Keystore
8. Save user profile
9. Navigate to → BasHomeActivity
```

### Subsequent App Launches
```
1. App Launch → SplashActivity
2. Check for valid OAuth token → Found
3. Validate token expiry
   - If expired → Navigate to Login
   - If valid → Navigate to BasHomeActivity
4. Check if refresh needed (< 10 min remaining)
   - Refresh token in background if needed
```

## Components

### 1. OAuthToken Model
**Location**: `/app/src/main/java/id/uniflo/uniedc/model/OAuthToken.java`

**Key Features**:
- Access token and refresh token storage
- Automatic expiry checking
- Token remaining time calculation
- JSON serialization/deserialization
- User role and scope management

**Token Structure**:
```java
{
  "access_token": "64-character-random-string",
  "refresh_token": "128-character-random-string",
  "token_type": "Bearer",
  "expires_in": 3600,  // seconds
  "created_at": 1703123456789,  // timestamp
  "user_id": "admin",
  "user_role": "ADMIN",
  "scope": "read write admin"
}
```

### 2. TokenManager
**Location**: `/app/src/main/java/id/uniflo/uniedc/manager/TokenManager.java`

**Security Features**:
- Uses Android Keystore for encryption
- AES-256-GCM encryption
- Secure key generation and storage
- Encrypted SharedPreferences storage
- Automatic IV (Initialization Vector) management

**Key Methods**:
```java
saveToken(OAuthToken token) - Encrypt and save token
getToken() - Retrieve and decrypt token
hasValidToken() - Check if valid token exists
shouldRefreshToken() - Check if refresh needed
clearToken() - Logout and clear token
```

### 3. SplashActivity Updates
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/SplashActivity.java`

**Token Validation Logic**:
```java
if (tokenManager.hasValidToken()) {
    // Check remaining time
    if (tokenManager.shouldRefreshToken()) {
        // Refresh if < 10 minutes remaining
    }
    // Navigate to BasHomeActivity
} else {
    // Clear expired tokens
    tokenManager.clearToken();
    profileManager.clearProfile();
    // Navigate to Login
}
```

### 4. BasLoginActivity Updates
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/BasLoginActivity.java`

**Token Generation on Login**:
```java
// After successful authentication
OAuthToken token = createOAuthToken(userId);
tokenManager.saveToken(token);
// Navigate to BasHomeActivity
```

## Security Implementation

### Encryption Details
- **Algorithm**: AES-256-GCM
- **Key Storage**: Android Keystore (hardware-backed when available)
- **Key Alias**: "UniEDC_TokenKey"
- **Tag Length**: 128 bits
- **IV Storage**: Separate encrypted storage

### Token Expiry Management
- **Default Expiry**: 1 hour (3600 seconds)
- **Buffer Time**: 5 minutes before actual expiry
- **Refresh Threshold**: 10 minutes remaining
- **Validation**: Checked on every app launch

### Secure Storage Layers
1. **Token Data**: Encrypted with AES-256-GCM
2. **Encryption Key**: Stored in Android Keystore
3. **Storage Medium**: SharedPreferences (encrypted)
4. **Access Control**: App-specific, not accessible to other apps

## User Roles and Scopes

### Admin User
- **Role**: ADMIN
- **Scope**: read write admin
- **Access**: All features

### Merchant User
- **Role**: MERCHANT
- **Scope**: read write
- **Access**: Limited admin features

### Regular User
- **Role**: USER
- **Scope**: read
- **Access**: Basic features only

## Token Lifecycle

### Creation
1. User logs in with credentials
2. Server validates credentials (mocked in development)
3. Generate access token (64 chars)
4. Generate refresh token (128 chars)
5. Set expiry time (1 hour)
6. Encrypt and store securely

### Validation
1. Retrieve encrypted token
2. Decrypt using Keystore
3. Check expiry time
4. Verify token structure
5. Return validation result

### Refresh (Future Implementation)
1. Check remaining time
2. If < 10 minutes, use refresh token
3. Call refresh endpoint
4. Get new access token
5. Update stored token
6. Continue session

### Expiry
1. Token expires after 1 hour
2. App detects expiry on next launch
3. Clear expired token
4. Clear user profile
5. Redirect to login

## API Integration (Production)

### Login Endpoint
```
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123",
  "device_id": "UNIEDC_F360_12345"
}

Response:
{
  "access_token": "...",
  "refresh_token": "...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Refresh Endpoint
```
POST /api/auth/refresh
Headers: Authorization: Bearer [refresh_token]

Response:
{
  "access_token": "...",
  "expires_in": 3600
}
```

### Using Token in API Calls
```
GET /api/transactions
Headers: Authorization: Bearer [access_token]
```

## Error Handling

### Token Expired
- Clear token and profile
- Show "Session expired" message
- Redirect to login
- Require re-authentication

### Token Invalid
- Clear corrupted data
- Log error for debugging
- Redirect to login
- Show generic error message

### Keystore Issues
- Fallback to standard encryption
- Log warning
- Continue with degraded security
- Alert in development mode

## Testing

### Test Scenarios
1. **First Login**: No token → Login → Save token → Navigate
2. **Valid Token**: Has token → Validate → Navigate directly
3. **Expired Token**: Has expired token → Clear → Login required
4. **Refresh Needed**: Token valid but < 10 min → Refresh
5. **Logout**: Clear token → Redirect to login
6. **App Reinstall**: Keystore cleared → Fresh login required

### Debug Information
```java
TokenManager.getTokenInfo() provides:
- Token existence
- User ID
- User Role
- Expiry status
- Remaining time
- Creation time
```

## Future Enhancements

1. **Real OAuth Server Integration**
   - Replace mock token generation
   - Implement proper refresh flow
   - Add token revocation

2. **Biometric Authentication**
   - Use fingerprint/face to unlock token
   - Additional security layer

3. **Multi-Device Support**
   - Device registration
   - Token per device
   - Remote logout capability

4. **Token Rotation**
   - Automatic token rotation
   - Sliding expiry window
   - Background refresh

5. **Offline Token Validation**
   - Local token verification
   - Offline grace period
   - Sync when online

## Migration Notes

### From Profile-Based to Token-Based
- Old: Check profile existence
- New: Check token validity
- Profile still used for permissions
- Token required for all API calls

### Backward Compatibility
- Existing users need to re-login once
- Credentials can be remembered
- Profile data preserved
- Settings maintained