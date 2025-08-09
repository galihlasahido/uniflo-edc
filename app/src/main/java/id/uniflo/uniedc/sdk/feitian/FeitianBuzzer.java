package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import id.uniflo.uniedc.sdk.interfaces.IBuzzer;

/**
 * Feitian Buzzer Wrapper using Reflection
 */
public class FeitianBuzzer implements IBuzzer {
    
    private static final String TAG = "FeitianBuzzer";
    private final Context context;
    private Object buzzer;
    private boolean initialized = false;
    private boolean isPlaying = false;
    private Handler handler;
    
    public FeitianBuzzer(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public int init() {
        try {
            // Check if Feitian SDK is available
            if (!FeitianReflectionHelper.isFeitianSDKAvailable()) {
                Log.e(TAG, "Feitian SDK not available");
                return -1;
            }
            
            // Load buzzer class
            Class<?> buzzerClass = FeitianReflectionHelper.loadClass(FeitianReflectionHelper.BUZZER);
            
            if (buzzerClass == null) {
                Log.e(TAG, "Buzzer class not found");
                return -1;
            }
            
            // Get buzzer instance
            buzzer = FeitianReflectionHelper.invokeStaticMethod(
                buzzerClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            if (buzzer == null) {
                Log.e(TAG, "Failed to get Buzzer instance");
                return -1;
            }
            
            initialized = true;
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize buzzer", e);
            return -1;
        }
    }
    
    @Override
    public int open() {
        if (!initialized) {
            return -1;
        }
        
        try {
            Object ret = FeitianReflectionHelper.invokeMethod(
                buzzer, "open", new Class<?>[0], new Object[0]
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to open buzzer: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open buzzer", e);
            return -1;
        }
    }
    
    @Override
    public int beep(int frequency, int duration) {
        if (!initialized || buzzer == null) {
            return -1;
        }
        
        try {
            isPlaying = true;
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                buzzer, "beep",
                new Class<?>[] { int.class, int.class },
                new Object[] { frequency, duration }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to beep: " + ret);
                isPlaying = false;
                return ret != null ? (Integer)ret : -1;
            }
            
            // Set playing flag to false after duration
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isPlaying = false;
                }
            }, duration);
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to beep", e);
            isPlaying = false;
            return -1;
        }
    }
    
    @Override
    public int playPattern(int[] pattern) {
        if (!initialized || buzzer == null || pattern == null || pattern.length < 2) {
            return -1;
        }
        
        // Play pattern in sequence
        playPatternSequence(pattern, 0);
        return 0;
    }
    
    private void playPatternSequence(final int[] pattern, final int index) {
        if (index >= pattern.length - 1) {
            isPlaying = false;
            return;
        }
        
        isPlaying = true;
        
        int frequency = pattern[index];
        int duration = pattern[index + 1];
        
        beep(frequency, duration);
        
        // Schedule next beep
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playPatternSequence(pattern, index + 2);
            }
        }, duration + 50); // Add 50ms gap between beeps
    }
    
    @Override
    public int stop() {
        if (!initialized || buzzer == null) {
            return -1;
        }
        
        try {
            // Cancel any pending pattern playback
            handler.removeCallbacksAndMessages(null);
            isPlaying = false;
            
            // Feitian SDK doesn't have a stop method, so we'll just mark as not playing
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop buzzer", e);
            return -1;
        }
    }
    
    @Override
    public int playSuccess() {
        // Success tone: two short high-pitched beeps
        int[] pattern = {
            FREQ_HIGH, 100,
            FREQ_HIGH, 100
        };
        return playPattern(pattern);
    }
    
    @Override
    public int playError() {
        // Error tone: one long low-pitched beep
        return beep(FREQ_ERROR, 500);
    }
    
    @Override
    public int playWarning() {
        // Warning tone: three medium-pitched beeps
        int[] pattern = {
            FREQ_MEDIUM, 150,
            FREQ_MEDIUM, 150,
            FREQ_MEDIUM, 150
        };
        return playPattern(pattern);
    }
    
    @Override
    public boolean isPlaying() {
        return isPlaying;
    }
    
    @Override
    public int close() {
        if (!initialized) {
            return -1;
        }
        
        try {
            // Stop any playing sounds
            stop();
            
            Object ret = FeitianReflectionHelper.invokeMethod(
                buzzer, "close", new Class<?>[0], new Object[0]
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "Failed to close buzzer: " + ret);
                return ret != null ? (Integer)ret : -1;
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to close buzzer", e);
            return -1;
        }
    }
    
    @Override
    public void release() {
        close();
        buzzer = null;
        initialized = false;
        isPlaying = false;
    }
}