package com.example.todolist.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public static String formatDateTime(long millis) {
        return new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(new Date(millis));
    }
    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(millis));
    }
    public static String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }
}
