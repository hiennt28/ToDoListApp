package com.example.todolist.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SubTaskDao {

    @Insert
    long insert(SubTask subTask);

    @Update
    void update(SubTask subTask);

    @Query("DELETE FROM subtask_table WHERE parent_task_id = :taskId")
    void deleteAllForTask(int taskId);
}