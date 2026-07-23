package com.example.todolist.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.todolist.model.AppDatabase;
import com.example.todolist.model.SubTask;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskWithSubTasks;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final com.example.todolist.model.TaskDao taskDao;
    private final com.example.todolist.model.SubTaskDao subTaskDao;
    private final LiveData<List<TaskWithSubTasks>> allTasks;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface OnInsertCompleteListener {
        void onInsertComplete(long newTaskId);
    }

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        taskDao = database.taskDao();
        subTaskDao = database.subTaskDao();
        allTasks = taskDao.getAllTasksWithSubTasks();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<List<TaskWithSubTasks>> getAllTasks() {
        return allTasks;
    }

    public void insertTaskWithSubTasks(Task task, List<SubTask> subTasks, OnInsertCompleteListener listener) {
        executorService.execute(() -> {
            long newTaskId = taskDao.insert(task);
            for (SubTask subTask : subTasks) {
                subTask.setParentTaskId((int) newTaskId);
                subTaskDao.insert(subTask);
            }
            if (listener != null) mainHandler.post(() -> listener.onInsertComplete(newTaskId));
        });
    }

    public void updateTaskWithSubTasks(Task task, List<SubTask> subTasks) {
        executorService.execute(() -> {
            taskDao.update(task);
            // Cách đơn giản để đồng bộ checklist: xóa hết sub-task cũ rồi ghi lại danh sách mới
            subTaskDao.deleteAllForTask(task.getId());
            for (SubTask subTask : subTasks) {
                subTask.setParentTaskId(task.getId());
                subTaskDao.insert(subTask);
            }
        });
    }

    public void updateTask(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }

    public void updateSubTask(SubTask subTask) {
        executorService.execute(() -> subTaskDao.update(subTask));
    }

    public void shutdownExecutor() {
        executorService.shutdown();
    }
}