package id.uniflo.uniedc.ui;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.sdk.SDKManager;
import id.uniflo.uniedc.sdk.interfaces.IDevice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DeviceInfoActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextView tvDeviceModel;
    private TextView tvSerialNumber;
    private TextView tvAndroidVersion;
    private TextView tvSdkVersion;
    private TextView tvScreenResolution;
    private TextView tvScreenSize;
    private TextView tvScreenDensity;
    private TextView tvMemoryInfo;
    private TextView tvStorageInfo;
    private TextView tvBatteryLevel;
    private TextView tvUptime;
    private TextView tvKernelVersion;
    
    private SDKManager sdkManager;
    private IDevice device;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        
        initViews();
        setupToolbar();
        loadDeviceInfo();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDeviceModel = findViewById(R.id.tv_device_model);
        tvSerialNumber = findViewById(R.id.tv_serial_number);
        tvAndroidVersion = findViewById(R.id.tv_android_version);
        tvSdkVersion = findViewById(R.id.tv_sdk_version);
        tvScreenResolution = findViewById(R.id.tv_screen_resolution);
        tvScreenSize = findViewById(R.id.tv_screen_size);
        tvScreenDensity = findViewById(R.id.tv_screen_density);
        tvMemoryInfo = findViewById(R.id.tv_memory_info);
        tvStorageInfo = findViewById(R.id.tv_storage_info);
        tvBatteryLevel = findViewById(R.id.tv_battery_level);
        tvUptime = findViewById(R.id.tv_uptime);
        tvKernelVersion = findViewById(R.id.tv_kernel_version);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Device Information");
        }
    }
    
    private void loadDeviceInfo() {
        // Get SDK manager instance
        sdkManager = SDKManager.getInstance();
        device = sdkManager.getDevice();
        
        // Device Model and Serial
        if (device != null) {
            String model = device.getModel();
            String serial = device.getSerialNumber();
            tvDeviceModel.setText(model != null ? model : "Unknown");
            tvSerialNumber.setText(serial != null ? serial : "Unknown");
            
            // SDK Version (Firmware Version)
            String sdkVersion = device.getFirmwareVersion();
            tvSdkVersion.setText(sdkVersion != null ? sdkVersion : "Unknown");
            
            // Battery Level
            int batteryLevel = device.getBatteryLevel();
            tvBatteryLevel.setText(batteryLevel >= 0 ? batteryLevel + "%" : "Unknown");
        } else {
            tvDeviceModel.setText(Build.MODEL);
            tvSerialNumber.setText(Build.SERIAL);
            tvSdkVersion.setText("N/A");
            tvBatteryLevel.setText("N/A");
        }
        
        // Android Version
        tvAndroidVersion.setText("Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        
        // Screen Information
        loadScreenInfo();
        
        // Memory Information
        loadMemoryInfo();
        
        // Storage Information
        loadStorageInfo();
        
        // System Uptime
        long uptimeMillis = android.os.SystemClock.uptimeMillis();
        String uptime = formatUptime(uptimeMillis);
        tvUptime.setText(uptime);
        
        // Kernel Version
        String kernelVersion = System.getProperty("os.version");
        tvKernelVersion.setText(kernelVersion != null ? kernelVersion : "Unknown");
    }
    
    private void loadScreenInfo() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        
        // Resolution
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        tvScreenResolution.setText(width + " x " + height + " pixels");
        
        // Physical size
        float widthInches = width / metrics.xdpi;
        float heightInches = height / metrics.ydpi;
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        tvScreenSize.setText(String.format(Locale.US, "%.1f inches", diagonalInches));
        
        // Density
        float density = metrics.density;
        int dpi = metrics.densityDpi;
        String densityStr = String.format(Locale.US, "%.1fx (%d dpi)", density, dpi);
        
        // Add density bucket info
        String bucket = "";
        if (dpi <= 120) bucket = " (ldpi)";
        else if (dpi <= 160) bucket = " (mdpi)";
        else if (dpi <= 240) bucket = " (hdpi)";
        else if (dpi <= 320) bucket = " (xhdpi)";
        else if (dpi <= 480) bucket = " (xxhdpi)";
        else if (dpi <= 640) bucket = " (xxxhdpi)";
        
        tvScreenDensity.setText(densityStr + bucket);
    }
    
    private void loadMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        String memInfo = String.format(Locale.US, 
            "Used: %s / Total: %s / Max: %s",
            formatBytes(usedMemory),
            formatBytes(totalMemory),
            formatBytes(maxMemory));
        
        tvMemoryInfo.setText(memInfo);
    }
    
    private void loadStorageInfo() {
        java.io.File dataDir = getFilesDir();
        if (dataDir != null) {
            long totalSpace = dataDir.getTotalSpace();
            long freeSpace = dataDir.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            String storageInfo = String.format(Locale.US,
                "Used: %s / Total: %s",
                formatBytes(usedSpace),
                formatBytes(totalSpace));
            
            tvStorageInfo.setText(storageInfo);
        } else {
            tvStorageInfo.setText("Unknown");
        }
    }
    
    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format(Locale.US, "%d days, %d hours, %d minutes",
                days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format(Locale.US, "%d hours, %d minutes",
                hours, minutes % 60);
        } else {
            return String.format(Locale.US, "%d minutes", minutes);
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}