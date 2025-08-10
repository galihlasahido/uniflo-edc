# Settings Flow Documentation

## Overview
Settings module provides comprehensive configuration management for the EDC terminal, including network settings, terminal parameters, security configurations, and system maintenance.

## Entry Points
- **Dashboard**: Settings button (if user has permission)
- **Admin Menu**: Advanced settings access
- **Direct Access**: `SettingsActivity.java`

## Main Components

### 1. SettingsActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/SettingsActivity.java`

#### Main Categories:
```java
Settings Menu:
├── Terminal Configuration
├── Network Settings
├── Printer Settings
├── Security Settings
├── Transaction Limits
├── Device Information
├── AID Settings
└── System Maintenance
```

### 2. TerminalConfigActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/TerminalConfigActivity.java`

#### Terminal Parameters:
```java
Configuration Items:
├── Terminal ID (8 digits)
├── Merchant ID (15 digits)
├── Merchant Name
├── Merchant Address
├── Batch Number
├── Trace Number
├── Currency Code
└── Time Zone
```

### 3. NetworkSettingsActivity.java
**Location**: `/app/src/main/java/id/uniflo/uniedc/ui/NetworkSettingsActivity.java`

#### Network Configuration:
```java
Network Parameters:
├── Primary Host
│   ├── IP Address/URL
│   ├── Port Number
│   └── SSL/TLS Enable
├── Secondary Host
│   ├── IP Address/URL
│   ├── Port Number
│   └── SSL/TLS Enable
├── Connection Timeout
├── Read Timeout
└── Retry Count
```

## Settings Structure

### Terminal Configuration

#### Basic Settings
```java
TerminalConfig {
    terminalId: "12345678",
    merchantId: "123456789012345",
    merchantName: "UNIFLO MERCHANT",
    merchantAddress: "Jl. Sudirman No. 123\nJakarta",
    merchantCity: "Jakarta",
    merchantPhone: "021-5551234",
    currencyCode: "360",  // IDR
    currencySymbol: "Rp",
    timeZone: "Asia/Jakarta"
}
```

#### Sequence Numbers
```java
SequenceConfig {
    batchNumber: "000001",
    traceNumber: "000123",
    invoiceNumber: "INV00001",
    autoIncrementTrace: true,
    resetTraceDaily: false,
    maxTraceNumber: 999999
}
```

### Network Settings

#### Host Configuration
```java
HostConfig {
    primary: {
        type: "IP",  // IP or URL
        address: "10.0.0.1",
        port: 8583,
        useSSL: true,
        certificatePinning: true,
        certificate: "[Base64 Cert]"
    },
    secondary: {
        type: "URL",
        address: "backup.host.com",
        port: 443,
        useSSL: true
    },
    failoverMode: "AUTO",  // AUTO or MANUAL
    maxRetries: 3,
    retryDelay: 5000  // milliseconds
}
```

#### Timeout Settings
```java
TimeoutConfig {
    connectionTimeout: 10000,  // 10 seconds
    readTimeout: 30000,        // 30 seconds
    writeTimeout: 10000,       // 10 seconds
    keepAlive: true,
    keepAliveInterval: 60000   // 1 minute
}
```

### Printer Settings

#### Print Configuration
```java
PrinterConfig {
    enabled: true,
    paperWidth: 58,  // mm
    fontSize: {
        header: LARGE,
        body: NORMAL,
        footer: SMALL
    },
    printSpeed: NORMAL,
    density: MEDIUM,
    logoEnabled: true,
    logoPath: "/storage/logo.bmp",
    footerText: "Thank you for your business",
    copies: {
        sale: 2,      // Customer + Merchant
        void: 2,
        settlement: 3
    }
}
```

### Security Settings

#### PIN Security
```java
PINSecurity {
    minLength: 4,
    maxLength: 6,
    maxAttempts: 3,
    blockDuration: 86400,  // 24 hours in seconds
    requireChange: 90,      // Days
    preventReuse: 5,        // Last 5 PINs
    complexityCheck: true
}
```

#### Session Management
```java
SessionConfig {
    timeout: 300000,        // 5 minutes
    extendOnActivity: true,
    requireReauth: true,
    maxSessions: 1,
    logoutOnCardRemoval: true
}
```

#### Encryption Settings
```java
EncryptionConfig {
    algorithm: "AES-256",
    keyManagement: "DUKPT",
    keyRotation: 30,  // Days
    secureDelete: true,
    tamperDetection: true
}
```

### Transaction Limits

#### Amount Limits
```java
TransactionLimits {
    sale: {
        min: 10000,
        max: 50000000,
        daily: 500000000
    },
    withdrawal: {
        min: 50000,
        max: 5000000,
        daily: 10000000
    },
    transfer: {
        min: 10000,
        max: 50000000,
        daily: 100000000
    },
    void: {
        enabled: true,
        timeLimit: 86400  // 24 hours
    }
}
```

### AID Settings
**Location**: `AidSettingsActivity.java`

#### Application Identifiers
```java
AIDConfig {
    aids: [
        {
            name: "NSICCS Debit",
            aid: "A0000006021010",
            enabled: true,
            priority: 1
        },
        {
            name: "Mastercard",
            aid: "A0000000041010",
            enabled: true,
            priority: 2
        },
        {
            name: "Visa",
            aid: "A0000000031010",
            enabled: true,
            priority: 3
        }
    ]
}
```

## Settings Management

### Load Settings
```java
loadSettings() {
    // Load from SharedPreferences
    SharedPreferences prefs = getSharedPreferences("terminal_settings", MODE_PRIVATE);
    
    // Load from encrypted database
    SecureSettingsDAO dao = new SecureSettingsDAO(context);
    TerminalConfig config = dao.getTerminalConfig();
    
    // Apply defaults if missing
    if (config == null) {
        config = createDefaultConfig();
    }
    
    return config;
}
```

### Save Settings
```java
saveSettings(config) {
    // Validate settings
    if (!validateConfig(config)) {
        throw new InvalidConfigException();
    }
    
    // Save to encrypted storage
    dao.saveTerminalConfig(config);
    
    // Backup to SharedPreferences
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("config_json", toJson(config));
    editor.apply();
    
    // Notify listeners
    notifyConfigurationChanged();
}
```

### Settings Validation
```java
validateSettings(config) {
    // Terminal ID validation
    if (!config.terminalId.matches("\\d{8}")) {
        return "Invalid Terminal ID";
    }
    
    // Merchant ID validation
    if (!config.merchantId.matches("\\d{15}")) {
        return "Invalid Merchant ID";
    }
    
    // Network validation
    if (!isValidIP(config.primaryHost)) {
        return "Invalid Primary Host";
    }
    
    // Port range validation
    if (config.port < 1 || config.port > 65535) {
        return "Invalid Port Number";
    }
    
    return null;  // Valid
}
```

## UI Components

### Settings Screens
```xml
Layouts:
├── activity_settings.xml - Main settings menu
├── activity_terminal_config.xml - Terminal setup
├── activity_network_settings.xml - Network config
├── activity_printer_settings.xml - Printer options
├── activity_security_settings.xml - Security params
├── activity_transaction_limits.xml - Limit config
├── activity_device_info.xml - Device details
├── activity_aid_settings.xml - AID management
└── activity_system_maintenance.xml - Maintenance
```

### Settings Navigation
```java
Main Settings
├── Terminal Configuration
│   ├── Basic Info
│   ├── Sequence Numbers
│   └── Regional Settings
├── Network Settings
│   ├── Host Configuration
│   ├── Timeout Settings
│   └── Proxy Settings
├── Printer Settings
│   ├── Print Options
│   ├── Receipt Format
│   └── Logo Management
└── Security Settings
    ├── PIN Configuration
    ├── Session Management
    └── Encryption Options
```

## Database Schema

### Settings Table
```sql
CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT,
    encrypted BOOLEAN DEFAULT 0,
    last_modified TIMESTAMP,
    modified_by TEXT
);
```

### Configuration History
```sql
CREATE TABLE config_history (
    id INTEGER PRIMARY KEY,
    config_type TEXT,
    old_value TEXT,
    new_value TEXT,
    changed_by TEXT,
    changed_at TIMESTAMP,
    reason TEXT
);
```

## System Maintenance

### Maintenance Functions
```java
MaintenanceOptions {
    clearTransactionLog: "Remove old transactions",
    resetSequenceNumbers: "Reset trace/batch numbers",
    exportConfiguration: "Backup settings to file",
    importConfiguration: "Restore settings from file",
    factoryReset: "Reset to factory defaults",
    updateFirmware: "Install firmware updates",
    diagnosticMode: "Run system diagnostics"
}
```

### Diagnostic Tests
```java
runDiagnostics() {
    tests = [
        testCardReader(),
        testPrinter(),
        testNetwork(),
        testStorage(),
        testSecurity(),
        testMemory()
    ];
    
    return generateReport(tests);
}
```

## Access Control

### Permission Levels
```java
SettingsPermissions {
    ADMIN: [
        "ALL_SETTINGS"
    ],
    SUPERVISOR: [
        "TERMINAL_CONFIG",
        "NETWORK_SETTINGS",
        "PRINTER_SETTINGS",
        "TRANSACTION_LIMITS"
    ],
    OPERATOR: [
        "PRINTER_SETTINGS",
        "VIEW_ONLY"
    ]
}
```

### Password Protection
```java
protectedSettings = [
    "Terminal Configuration",
    "Network Settings",
    "Security Settings",
    "Factory Reset"
];

accessProtectedSetting(setting) {
    if (protectedSettings.contains(setting)) {
        return requirePassword();
    }
    return true;
}
```

## Configuration Export/Import

### Export Format
```json
{
  "version": "1.0",
  "exported": "2024-12-20T10:30:00Z",
  "device": "F360",
  "terminal": {
    "id": "12345678",
    "merchant": "123456789012345"
  },
  "network": {
    "primary": "10.0.0.1:8583",
    "secondary": "10.0.0.2:8583"
  },
  "settings": {
    // All configuration items
  }
}
```

### Import Process
```java
importConfiguration(file) {
    // Read and parse file
    config = parseConfigFile(file);
    
    // Validate version compatibility
    if (!isCompatibleVersion(config.version)) {
        throw new IncompatibleVersionException();
    }
    
    // Backup current settings
    backupCurrentSettings();
    
    // Apply new settings
    applyConfiguration(config);
    
    // Restart services
    restartServices();
}
```

## Remote Configuration

### Download Settings
```java
downloadRemoteConfig() {
    // Connect to management server
    Response response = apiClient.get("/config/" + terminalId);
    
    // Validate signature
    if (!verifySignature(response)) {
        throw new SecurityException();
    }
    
    // Apply configuration
    applyRemoteConfig(response.config);
    
    // Send acknowledgment
    apiClient.post("/config/ack", {
        terminalId: terminalId,
        timestamp: now(),
        status: "SUCCESS"
    });
}
```

## Settings Backup

### Automatic Backup
```java
BackupSchedule {
    enabled: true,
    frequency: "DAILY",
    time: "02:00",
    retention: 30,  // Days
    location: "/storage/backups/",
    encrypt: true
}
```

### Manual Backup
```java
createBackup() {
    // Generate backup filename
    filename = "backup_" + terminalId + "_" + timestamp + ".enc";
    
    // Collect all settings
    data = collectAllSettings();
    
    // Encrypt backup
    encrypted = encrypt(data, backupKey);
    
    // Save to file
    saveToFile(filename, encrypted);
    
    // Upload to cloud (optional)
    uploadToCloud(filename);
}
```

## Troubleshooting

### Common Issues
1. **Settings not saving**: Check storage permissions
2. **Network unreachable**: Verify host and port
3. **Printer not working**: Check printer settings
4. **AID not detected**: Enable in AID settings
5. **Login timeout**: Adjust session timeout

### Reset Procedures
```java
// Soft reset - Keep terminal ID
softReset() {
    clearTransactions();
    resetSequenceNumbers();
    keepTerminalId();
}

// Hard reset - Factory defaults
hardReset() {
    if (confirmFactoryReset()) {
        clearAllData();
        restoreFactoryDefaults();
        requireSetup();
    }
}
```

## Testing Settings

### Test Configurations
```java
TestMode {
    enabled: false,
    testHost: "test.server.com",
    testPort: 8583,
    testCards: ["4111111111111111"],
    alwaysApprove: false,
    printTestReceipts: true
}
```

## Notes
- Settings changes may require restart
- Some settings need supervisor password
- Network changes affect all transactions
- Backup before major changes
- Regular settings audit recommended
- Remote configuration overrides local