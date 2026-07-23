package com.example.todolist.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.todolist.model.SubTask;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskWithSubTasks;
import com.example.todolist.repository.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskViewModel extends AndroidViewModel {

    public static class TodayProgress {
        public final int completed;
        public final int total;
        public TodayProgress(int completed, int total) {
            this.completed = completed;
            this.total = total;
        }
    }

    private final TaskRepository repository;
    private final LiveData<List<TaskWithSubTasks>> allTasks;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedTopic = new MutableLiveData<>(""); // "" = không lọc
    private final MediatorLiveData<List<TaskWithSubTasks>> filteredTasks = new MediatorLiveData<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();

        filteredTasks.addSource(allTasks, tasks -> applyFilters());
        filteredTasks.addSource(searchQuery, q -> applyFilters());
        filteredTasks.addSource(selectedTopic, t -> applyFilters());
    }

    private void applyFilters() {
        List<TaskWithSubTasks> source = allTasks.getValue();
        if (source == null) return;

        String query = searchQuery.getValue() == null ? "" : searchQuery.getValue().toLowerCase(Locale.getDefault());
        String topic = selectedTopic.getValue() == null ? "" : selectedTopic.getValue();
        long startOfToday = getStartOfTodayMillis();
        long endOfToday = getEndOfTodayMillis();

        List<TaskWithSubTasks> result = new ArrayList<>();
        for (TaskWithSubTasks item : source) {
            Task task = item.task;

            // Tab Task chỉ hiện việc HÔM NAY (xong hay chưa đều hiện) + việc QUÁ HẠN chưa xong.
            // Muốn xem ngày khác thì qua tab Calendar.
            boolean isToday = task.getDueDate() >= startOfToday && task.getDueDate() <= endOfToday;
            boolean isOverdueIncomplete = task.getDueDate() < startOfToday && !task.isCompleted();
            if (!isToday && !isOverdueIncomplete) continue;

            if (!query.isEmpty() && !task.getTitle().toLowerCase(Locale.getDefault()).contains(query)) continue;
            if (!topic.isEmpty() && !task.getTitle().equals(topic)) continue;

            result.add(item);
        }
        filteredTasks.setValue(result);
    }

    // "Chủ đề" = các Tiêu đề khác nhau đang có -> dùng để tạo Chip lọc động
    public LiveData<List<String>> getDistinctTopics() {
        return Transformations.map(allTasks, list -> {
            LinkedHashSet<String> topics = new LinkedHashSet<>();
            for (TaskWithSubTasks item : list) topics.add(item.task.getTitle());
            return new ArrayList<>(topics);
        });
    }

    public LiveData<TodayProgress> getTodayProgress() {
        return Transformations.map(allTasks, list -> {
            long start = getStartOfTodayMillis();
            long end = getEndOfTodayMillis();
            int total = 0, completed = 0;
            for (TaskWithSubTasks item : list) {
                long due = item.task.getDueDate();
                if (due >= start && due <= end) {
                    total++;
                    if (item.task.isCompleted()) completed++;
                }
            }
            return new TodayProgress(completed, total);
        });
    }

    // Gom Task theo ngày (yyyy-MM-dd) để tab Calendar biết ngày nào có việc + hiện đúng danh sách
    public LiveData<Map<String, List<TaskWithSubTasks>>> getTasksGroupedByDate() {
        return Transformations.map(allTasks, list -> {
            Map<String, List<TaskWithSubTasks>> map = new HashMap<>();
            SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            for (TaskWithSubTasks item : list) {
                String key = keyFormat.format(new Date(item.task.getDueDate()));
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
            }
            return map;
        });
    }

    private long getStartOfTodayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long getEndOfTodayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    public LiveData<List<TaskWithSubTasks>> getFilteredTasks() { return filteredTasks; }
    public LiveData<List<TaskWithSubTasks>> getAllTasksWithSubTasks() { return allTasks; }

    public void setSearchQuery(String query) { searchQuery.setValue(query); }
    public void setSelectedTopic(String topic) { selectedTopic.setValue(topic); }

    public void insertTaskWithSubTasks(Task task, List<SubTask> subTasks, TaskRepository.OnInsertCompleteListener listener) {
        repository.insertTaskWithSubTasks(task, subTasks, listener);
    }
    public void updateTaskWithSubTasks(Task task, List<SubTask> subTasks) {
        repository.updateTaskWithSubTasks(task, subTasks);
    }
    public void updateTask(Task task) { repository.updateTask(task); }
    public void deleteTask(Task task) { repository.deleteTask(task); }
    public void updateSubTask(SubTask subTask) { repository.updateSubTask(subTask); }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdownExecutor();
    }
}