package io.hefuyi.listener.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by hefuyi on 2016/11/23.
 */

public class ColorUtil {

    public static void setStatusBarColor(Activity activity, int color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = activity.getWindow();
                window.setStatusBarColor(getStatusBarColor(color));
                applyTaskDescription(activity, color);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final View decorView = activity.getWindow().getDecorView();
                boolean lightStatusEnabled = isColorLight(color);
                final int systemUiVisibility = decorView.getSystemUiVisibility();
                if (lightStatusEnabled) {
                    decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    decorView.setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            }

            ((DrawerLayout) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0)).setStatusBarBackgroundColor(getStatusBarColor(color));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isColorLight(@ColorInt int color) {
        double darkness = 1.0D - (0.299D * (double)Color.red(color) + 0.587D * (double)Color.green(color) + 0.114D * (double)Color.blue(color)) / 255.0D;
        return darkness < 0.4D;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void applyTaskDescription(@NonNull Activity activity, int color) {
        // Sets color of entry in the system recents page
        try {
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                    (String) activity.getTitle(),
                    ((BitmapDrawable) activity.getApplicationInfo().loadIcon(activity.getPackageManager())).getBitmap(),
                    color);
            activity.setTaskDescription(td);
        } catch (Exception ignored) {

        }
    }

    public static int getDarkenColor(int color) {
        float[] arrayOfFloat = new float[3];
        Color.colorToHSV(color, arrayOfFloat);
        arrayOfFloat[2] *= 0.8F;
        return Color.HSVToColor(arrayOfFloat);
    }
    public static int getLightenColor(int color) {
        float[] arrayOfFloat = new float[3];
        Color.colorToHSV(color, arrayOfFloat);
        arrayOfFloat[2] = 0.2f + 0.8f * arrayOfFloat[2];
        return Color.HSVToColor(arrayOfFloat);
    }

    public static int getStatusBarColor(int color) {
        float[] arrayOfFloat = new float[3];
        Color.colorToHSV(color, arrayOfFloat);
        arrayOfFloat[2] *= 0.9F;
        return Color.HSVToColor(arrayOfFloat);
    }

    public static int getBlackWhiteColor(int color) { //根据颜色的亮度转换为黑白色
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness >= 0.5) {
            return Color.WHITE;
        } else return Color.BLACK;
    }

    public static int getOpaqueColor(@ColorInt int paramInt) {
        return 0xFF000000 | paramInt;
    }

    public static @Nullable Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (mostPopulous == null || swatch.getPopulation() > mostPopulous.getPopulation()) {
                    mostPopulous = swatch;
                }
            }
        }
        return mostPopulous;
    }
}
