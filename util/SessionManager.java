package com.example.todolist.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Lưu "ai đang đăng nhập" bằng SharedPreferences - độc lập với ThemePreferences. */
public class SessionManager {

    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";
    private static final int NO_USER = -1; // Room autoGenerate bắt đầu từ 1 nên -1 luôn an toàn làm cờ "chưa đăng nhập"

    public static void setLoggedInUserId(Context context, int userId) {
        prefs(context).edit().putInt(KEY_LOGGED_IN_USER_ID, userId).apply();
    }

    public static int getLoggedInUserId(Context context) {
        return prefs(context).getInt(KEY_LOGGED_IN_USER_ID, NO_USER);
    }

    public static boolean isLoggedIn(Context context) {
        return getLoggedInUserId(context) != NO_USER;
    }

    public static void logout(Context context) {
        prefs(context).edit().remove(KEY_LOGGED_IN_USER_ID).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}