package com.example.todolist.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver được AlarmManager "đánh thức" đúng thời điểm đã lên lịch (dueDate),
 * kể cả khi app đang bị đóng hoàn toàn (vì đây là receiver khai báo tĩnh trong Manifest,
 * không phải receiver đăng ký động - xem giải thích ở AndroidManifest.xml bên dưới).
 *
 * onReceive() chỉ có khoảng 10 giây trước khi hệ thống có thể "giết" tiến trình,
 * nên tuyệt đối KHÔNG xử lý logic nặng ở đây - chỉ chuyển tiếp dữ liệu cho NotificationHelper.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "extra_task_description";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(EXTRA_TASK_ID, 0);
        String title = intent.getStringExtra(EXTRA_TASK_TITLE);
        String description = intent.getStringExtra(EXTRA_TASK_DESCRIPTION);

        NotificationHelper.showNotification(context, taskId, title, description);
    }
}
