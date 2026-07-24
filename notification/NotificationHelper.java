package com.example.todolist.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todolist.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "todo_reminder_channel";
    private static final String CHANNEL_NAME = "Nhắc nhở công việc";
    private static final String CHANNEL_DESC = "Thông báo khi công việc đến hạn hoàn thành";

    // Gọi 1 lần lúc app khởi động (đặt trong MainActivity.onCreate() ở Bước 5).
    // Notification Channel là bắt buộc từ Android 8.0 (API 26) trở lên.
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, int notificationId, String title, String content) {
        // Android 13+ (API 33) BẮT BUỘC phải có quyền POST_NOTIFICATIONS mới hiển thị được,
        // nếu thiếu quyền mà vẫn gọi notify() sẽ ném SecurityException làm crash app.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return; // Chưa được cấp quyền -> thoát sớm, không hiển thị
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add) // Tạm dùng ic_add, nên thay bằng icon riêng cho thông báo
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
