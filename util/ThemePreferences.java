package com.example.todolist.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Lưu lựa chọn Dark Mode của người dùng vào SharedPreferences (độc lập với cài đặt
 * hệ thống), để app nhớ đúng lựa chọn kể cả sau khi khởi động lại.
 */
public class ThemePreferences {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    public static boolean isDarkModeEnabled(Context context) {
        return prefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkModeEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        AppCompatDelegate.setDefaultNightMode(
                enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Gọi hàm này TRƯỚC super.onCreate() của MainActivity để áp dụng đúng theme
    // ngay từ đầu, tránh hiện tượng "nháy" sáng rồi mới chuyển tối.
    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled(context) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
