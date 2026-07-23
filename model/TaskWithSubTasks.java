package com.example.todolist.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskWithSubTasks implements Serializable {

    @Embedded
    public Task task;

    @Relation(parentColumn = "id", entityColumn = "parent_task_id")
    public List<SubTask> subTasks = new ArrayList<>();

    public int getCompletedSubTaskCount() {
        int count = 0;
        for (SubTask s : subTasks) if (s.isCompleted()) count++;
        return count;
    }
}