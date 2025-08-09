package id.uniflo.uniedc.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Utility class for setting custom screen density in Android applications.
 * This is particularly useful for ensuring consistent UI scaling across different devices.
 */
public class DensityUtil {
    public static final int TARGET_DPI = 320;
    public static float density;
    public static float scaledDensity;

    public static void setCustomDensity(Activity activity, Application application) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        if (density == 0) {
            density = displayMetrics.density;
            scaledDensity = displayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        scaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {
                }
            });
        }
        float targetDensity = (float) (displayMetrics.widthPixels / TARGET_DPI);
        int angle = ((WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        switch (angle) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                targetDensity = (float) (displayMetrics.heightPixels / TARGET_DPI);
                break;
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
            default:
                targetDensity = (float) (displayMetrics.widthPixels / TARGET_DPI);
                break;
        }
        float targetScaledDensity = targetDensity * (scaledDensity / density);
        int targetDensityDpi = (int) (160 * targetDensity);
        displayMetrics.density = targetDensity;
        displayMetrics.scaledDensity = targetScaledDensity;
        displayMetrics.densityDpi = targetDensityDpi;

        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }
}
