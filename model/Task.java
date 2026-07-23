package com.example.todolist.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "task_table")
public class Task implements Serializable {

    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;

    // "Thời gian thông báo": báo trước bao lâu so với giờ hẹn (ngày giờ) của Task
    public static final int REMINDER_NONE = 0;
    public static final int REMINDER_ON_TIME = 1;
    public static final int REMINDER_BEFORE_5_MIN = 2;
    public static final int REMINDER_BEFORE_10_MIN = 3;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String title; // Cũng chính là "chủ đề" dùng để lọc ở tab Task

    private String description;

    @ColumnInfo(name = "due_date")
    private long dueDate;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    private int priority;

    @ColumnInfo(name = "reminder_offset")
    private int reminderOffset;

    public Task(int id, @NonNull String title, String description, long dueDate,
                boolean isCompleted, int priority, int reminderOffset) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.reminderOffset = reminderOffset;
    }

    @Ignore
    public Task(@NonNull String title, String description, long dueDate,
                boolean isCompleted, int priority, int reminderOffset) {
        this(0, title, description, dueDate, isCompleted, priority, reminderOffset);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    @NonNull public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public int getReminderOffset() { return reminderOffset; }
    public void setReminderOffset(int reminderOffset) { this.reminderOffset = reminderOffset; }
}