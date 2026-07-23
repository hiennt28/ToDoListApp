package com.example.todolist.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task); // CASCADE ở SubTask lo phần xóa sub-task con

    // @Transaction bắt buộc khi query trả về POJO có @Relation, để Room chạy
    // câu SELECT chính + câu SELECT lấy sub-task trong cùng 1 giao dịch an toàn.
    @Transaction
    @Query("SELECT * FROM task_table ORDER BY due_date ASC")
    LiveData<List<TaskWithSubTasks>> getAllTasksWithSubTasks();
}