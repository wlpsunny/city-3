package com.sensoro.common.widgets.statusbar;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.lang.reflect.Method;

/**
 * Created by sensoro on 17/10/13.
 */

public class StatusBarCompat {

    private static int color = 0;

    //Get alpha color
    static int calculateStatusBarColor(int color, int alpha) {
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }

    /**
     * set statusBarColor
     *
     * @param statusColor color
     * @param alpha       0 - 255
     */
    public static void setStatusBarColor(@NonNull Activity activity, @ColorInt int statusColor, int alpha) {
        setStatusBarColor(activity, calculateStatusBarColor(statusColor, alpha));
    }

    public static void setStatusBarColor(@NonNull Activity activity, @ColorInt int statusColor) {
        color = statusColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompatLollipop.setStatusBarColor(activity, statusColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompatKitKat.setStatusBarColor(activity, statusColor);
        }
    }

    public static void setStatusBarColor(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompatLollipop.setStatusBarColor(activity, color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompatKitKat.setStatusBarColor(activity, color);
        }
    }

    public static int getStatusBarHeight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return StatusBarCompatKitKat.getStatusBarHeight(context);
        } else {
            return 50;
        }
    }

    public static void translucentStatusBar(@NonNull Activity activity) {
        translucentStatusBar(activity, false);
    }

    /**
     * change to full screen mode
     *
     * @param hideStatusBarBackground hide status bar alpha Background when SDK > 21, true if hide it
     */
    public static void translucentStatusBar(@NonNull Activity activity, boolean hideStatusBarBackground) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompatLollipop.translucentStatusBar(activity, hideStatusBarBackground);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompatKitKat.translucentStatusBar(activity);
        }
    }

    public static void setStatusBarColorForCollapsingToolbar(@NonNull Activity activity, AppBarLayout appBarLayout, CollapsingToolbarLayout collapsingToolbarLayout,
                                                             Toolbar toolbar, @ColorInt int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompatLollipop.setStatusBarColorForCollapsingToolbar(activity, appBarLayout, collapsingToolbarLayout, toolbar, statusColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarCompatKitKat.setStatusBarColorForCollapsingToolbar(activity, appBarLayout, collapsingToolbarLayout, toolbar, statusColor);
        }
    }

    public static void setStatusBarIconDark(Activity activity, boolean dark) {
        try {
            Object win = activity.getWindow();
            Class<?> cls = win.getClass();
            Method method = cls.getDeclaredMethod("setStatusBarIconDark", boolean.class);
            method.invoke(win, dark);
        } catch (Exception e) {
            Log.v("ff", "statusBarIconDark,e=" + e);
        }
    }
}
