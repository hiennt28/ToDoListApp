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
import com.example.todolist.notification.AlarmScheduler;
import com.example.todolist.repository.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskViewModel extends AndroidViewModel {

    public enum CompletionFilter { ALL, NOT_DONE, IN_PROGRESS, DONE }

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
    private final MutableLiveData<CompletionFilter> completionFilter = new MutableLiveData<>(CompletionFilter.ALL);
    private final MediatorLiveData<List<TaskWithSubTasks>> filteredTasks = new MediatorLiveData<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();

        filteredTasks.addSource(allTasks, tasks -> applyFilters());
        filteredTasks.addSource(searchQuery, q -> applyFilters());
        filteredTasks.addSource(completionFilter, f -> applyFilters());
    }

    private void applyFilters() {
        List<TaskWithSubTasks> source = allTasks.getValue();
        if (source == null) return;

        String query = searchQuery.getValue() == null ? "" : searchQuery.getValue().toLowerCase(Locale.getDefault());
        CompletionFilter filter = completionFilter.getValue() == null ? CompletionFilter.ALL : completionFilter.getValue();
        long startOfToday = getStartOfTodayMillis();
        long endOfToday = getEndOfTodayMillis();

        List<TaskWithSubTasks> result = new ArrayList<>();
        for (TaskWithSubTasks item : source) {
            Task task = item.task;

            boolean isToday = task.getDueDate() >= startOfToday && task.getDueDate() <= endOfToday;
            boolean isOverdueIncomplete = task.getDueDate() < startOfToday && !task.isCompleted();
            if (!isToday && !isOverdueIncomplete) continue;

            if (!query.isEmpty() && !task.getTitle().toLowerCase(Locale.getDefault()).contains(query)) continue;

            if (filter != CompletionFilter.ALL) {
                boolean matches;
                int doneSub = item.getCompletedSubTaskCount();
                int totalSub = item.subTasks.size();
                switch (filter) {
                    case DONE:
                        matches = task.isCompleted();
                        break;
                    case IN_PROGRESS:
                        // "Đang làm": chưa xong hẳn nhưng đã tick được ít nhất 1 việc con (chưa hết)
                        matches = !task.isCompleted() && totalSub > 0 && doneSub > 0 && doneSub < totalSub;
                        break;
                    case NOT_DONE:
                    default:
                        matches = !task.isCompleted() && doneSub == 0;
                        break;
                }
                if (!matches) continue;
            }
            result.add(item);
        }
        filteredTasks.setValue(result);
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
    public void setCompletionFilter(CompletionFilter filter) { completionFilter.setValue(filter); }

    // --- Toàn bộ việc đặt/hủy AlarmManager giờ nằm DUY NHẤT ở đây.
    // TaskFragment, CalendarFragment, AddEditTaskBottomSheetFragment không import
    // AlarmScheduler nữa -> tránh tình trạng sửa 1 nơi quên nơi khác. ---

    public void insertTaskWithSubTasks(Task task, List<SubTask> subTasks, TaskRepository.OnInsertCompleteListener listener) {
        repository.insertTaskWithSubTasks(task, subTasks, newTaskId -> {
            task.setId((int) newTaskId);
            AlarmScheduler.scheduleTaskAlarm(getApplication(), task);
            if (listener != null) listener.onInsertComplete(newTaskId);
        });
    }

    public void updateTaskWithSubTasks(Task task, List<SubTask> subTasks) {
        AlarmScheduler.cancelTaskAlarm(getApplication(), task);
        repository.updateTaskWithSubTasks(task, subTasks);
        if (!task.isCompleted()) {
            AlarmScheduler.scheduleTaskAlarm(getApplication(), task);
        }
    }

    // Tick/bỏ tick trực tiếp trên Task cha
    public void updateTaskCompletion(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        repository.updateTask(task);
        if (isChecked) {
            AlarmScheduler.cancelTaskAlarm(getApplication(), task);
        } else {
            // Bỏ tick -> báo thức phải kêu lại BÌNH THƯỜNG (nếu giờ hẹn còn ở tương lai)
            AlarmScheduler.scheduleTaskAlarm(getApplication(), task);
        }
    }

    // Tick/bỏ tick 1 mục con -> tự đồng bộ trạng thái Task cha
    public void updateSubTaskCompletion(TaskWithSubTasks parentItem, SubTask subTask, boolean isChecked) {
        subTask.setCompleted(isChecked);
        repository.updateSubTask(subTask);

        boolean allSubtasksDone = true;
        for (SubTask s : parentItem.subTasks) {
            if (!s.isCompleted()) { allSubtasksDone = false; break; }
        }

        Task parentTask = parentItem.task;
        if (allSubtasksDone && !parentTask.isCompleted()) {
            // Tất cả mục con đã xong -> Task cha TỰ ĐỘNG chuyển sang hoàn thành
            parentTask.setCompleted(true);
            repository.updateTask(parentTask);
            AlarmScheduler.cancelTaskAlarm(getApplication(), parentTask);
        } else if (!allSubtasksDone && parentTask.isCompleted()) {
            // Vừa bỏ tick 1 mục con trong khi Task cha đang hiện hoàn thành -> Task cha quay lại CHƯA xong
            parentTask.setCompleted(false);
            repository.updateTask(parentTask);
            AlarmScheduler.scheduleTaskAlarm(getApplication(), parentTask);
        }
    }

    public void deleteTask(Task task) {
        AlarmScheduler.cancelTaskAlarm(getApplication(), task);
        repository.deleteTask(task);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdownExecutor();
    }
}
