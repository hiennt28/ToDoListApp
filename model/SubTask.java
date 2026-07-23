package com.example.todolist.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Danh sách con bên trong 1 Task, vd Task "Làm bài tập" có SubTask "Toán", "Văn".
 * onDelete = CASCADE: xóa Task cha thì toàn bộ SubTask con bị xóa theo, không cần code thủ công.
 */
@Entity(
        tableName = "subtask_table",
        foreignKeys = @ForeignKey(
                entity = Task.class,
                parentColumns = "id",
                childColumns = "parent_task_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parent_task_id")}
)
public class SubTask implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "parent_task_id")
    private int parentTaskId;

    private String title;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    public SubTask(int parentTaskId, String title, boolean isCompleted) {
        this.parentTaskId = parentTaskId;
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(int parentTaskId) { this.parentTaskId = parentTaskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}