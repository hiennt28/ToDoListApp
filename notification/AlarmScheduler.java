package com.example.todolist.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.todolist.model.Task;

public class AlarmScheduler {

    public static void scheduleTaskAlarm(Context context, Task task) {
        long triggerTime = computeTriggerTime(task);
        if (triggerTime <= System.currentTimeMillis()) {
            return; // REMINDER_NONE hoặc thời điểm báo đã ở quá khứ -> không đặt lịch
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = buildPendingIntent(context, task);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    // Chuyển lựa chọn "thời gian thông báo" thành mốc thời gian THỰC SỰ sẽ bắn báo
    private static long computeTriggerTime(Task task) {
        switch (task.getReminderOffset()) {
            case Task.REMINDER_BEFORE_5_MIN:  return task.getDueDate() - 5 * 60 * 1000L;
            case Task.REMINDER_BEFORE_10_MIN: return task.getDueDate() - 10 * 60 * 1000L;
            case Task.REMINDER_ON_TIME:       return task.getDueDate();
            case Task.REMINDER_NONE:
            default: return -1;
        }
    }

    public static void cancelTaskAlarm(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        alarmManager.cancel(buildPendingIntent(context, task));
    }

    private static PendingIntent buildPendingIntent(Context context, Task task) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TASK_ID, task.getId());
        intent.putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(AlarmReceiver.EXTRA_TASK_DESCRIPTION, task.getDescription());
        return PendingIntent.getBroadcast(context, task.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}