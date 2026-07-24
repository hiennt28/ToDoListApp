package com.example.todolist.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.model.SubTask;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskWithSubTasks;
import com.example.todolist.viewmodel.TaskViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {

    private TaskViewModel taskViewModel;
    private CalendarDayAdapter calendarDayAdapter;
    private TaskAdapter dayTaskAdapter;
    private TextView tvMonthLabel, tvSelectedDateLabel, tvNoTaskThisDay;
    private RecyclerView recyclerDayTasks;

    private final Calendar displayedMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private Map<String, List<TaskWithSubTasks>> tasksByDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        tvMonthLabel = view.findViewById(R.id.tv_month_label);
        tvSelectedDateLabel = view.findViewById(R.id.tv_selected_date_label);
        tvNoTaskThisDay = view.findViewById(R.id.tv_no_task_this_day);
        recyclerDayTasks = view.findViewById(R.id.recycler_day_tasks);

        buildWeekdayHeader(view.findViewById(R.id.header_weekdays));

        RecyclerView recyclerGrid = view.findViewById(R.id.recycler_calendar_grid);
        recyclerGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarDayAdapter = new CalendarDayAdapter(this::onDaySelected);
        recyclerGrid.setAdapter(calendarDayAdapter);

        recyclerDayTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        dayTaskAdapter = new TaskAdapter(this);
        recyclerDayTasks.setAdapter(dayTaskAdapter);

        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> changeMonth(-1));
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> changeMonth(1));

        taskViewModel.getTasksGroupedByDate().observe(getViewLifecycleOwner(), grouped -> {
            tasksByDate = grouped;
            renderMonthGrid();
            renderSelectedDayTasks();
        });

        renderMonthLabel();
    }

    private void buildWeekdayHeader(LinearLayout headerWeekdays) {
        String[] labels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String label : labels) {
            TextView tv = new TextView(requireContext());
            tv.setText(label);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tv.setTextSize(12);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            headerWeekdays.addView(tv);
        }
    }

    private void changeMonth(int delta) {
        displayedMonth.add(Calendar.MONTH, delta);
        renderMonthLabel();
        renderMonthGrid();
    }

    private void renderMonthLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("'Tháng' M, yyyy", new Locale("vi", "VN"));
        tvMonthLabel.setText(sdf.format(displayedMonth.getTime()));
    }

    private void renderMonthGrid() {
        List<CalendarDayAdapter.DayCell> cells = new ArrayList<>();
        Calendar cursor = (Calendar) displayedMonth.clone();
        cursor.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cursor.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - Calendar.MONDAY;
        cursor.add(Calendar.DAY_OF_MONTH, -offset);

        int targetMonth = displayedMonth.get(Calendar.MONTH);
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        for (int i = 0; i < 42; i++) {
            boolean isCurrentMonth = cursor.get(Calendar.MONTH) == targetMonth;
            CalendarDayAdapter.DayCell cell = new CalendarDayAdapter.DayCell((Calendar) cursor.clone(), isCurrentMonth);
            String key = keyFormat.format(cursor.getTime());
            cell.hasTasks = tasksByDate != null && tasksByDate.containsKey(key);
            cell.isSelected = isSameDay(cursor, selectedDate);
            cells.add(cell);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendarDayAdapter.submitDays(cells);
    }

    private void onDaySelected(Calendar day) {
        selectedDate.setTime(day.getTime());
        if (day.get(Calendar.MONTH) != displayedMonth.get(Calendar.MONTH)
                || day.get(Calendar.YEAR) != displayedMonth.get(Calendar.YEAR)) {
            displayedMonth.setTime(day.getTime());
            renderMonthLabel();
        }
        renderMonthGrid();
        renderSelectedDayTasks();
    }

    private void renderSelectedDayTasks() {
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String key = keyFormat.format(selectedDate.getTime());
        List<TaskWithSubTasks> tasksForDay = tasksByDate != null ? tasksByDate.get(key) : null;
        if (tasksForDay == null) tasksForDay = new ArrayList<>();

        dayTaskAdapter.submitList(tasksForDay);
        boolean isEmpty = tasksForDay.isEmpty();
        recyclerDayTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvNoTaskThisDay.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        SimpleDateFormat labelFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        String label = labelFormat.format(selectedDate.getTime());
        tvSelectedDateLabel.setText(label.substring(0, 1).toUpperCase(Locale.getDefault()) + label.substring(1));
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        taskViewModel.updateTaskCompletion(task, isChecked);
    }

    @Override
    public void onSubTaskCheckedChanged(TaskWithSubTasks parentItem, SubTask subTask, boolean isChecked) {
        taskViewModel.updateSubTaskCompletion(parentItem, subTask, isChecked);
    }

    @Override
    public void onEditClicked(TaskWithSubTasks item) {
        AddEditTaskBottomSheetFragment.newInstanceForEdit(item)
                .show(getParentFragmentManager(), "EditTaskBottomSheet");
    }

    @Override
    public void onDeleteClicked(TaskWithSubTasks item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_confirm_delete)
                .setMessage(getString(R.string.msg_confirm_delete, item.task.getTitle()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> taskViewModel.deleteTask(item.task))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
