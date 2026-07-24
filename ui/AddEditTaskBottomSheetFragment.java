package com.example.todolist.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.R;
import com.example.todolist.model.SubTask;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskWithSubTasks;
import com.example.todolist.notification.AlarmScheduler;
import com.example.todolist.util.DateTimeUtils;
import com.example.todolist.viewmodel.TaskViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddEditTaskBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_EDITING_ITEM = "arg_editing_item";

    private TaskViewModel taskViewModel;
    private EditText edtTitle, edtDescription;
    private Button btnPickDate, btnPickTime;
    private RadioGroup radioGroupPriority;
    private Spinner spinnerReminder;
    private LinearLayout containerSubtaskInputs;

    private final Calendar selectedDateTime = Calendar.getInstance();
    private TaskWithSubTasks editingItem; // null nếu đang THÊM MỚI

    public static AddEditTaskBottomSheetFragment newInstanceForAdd() {
        return new AddEditTaskBottomSheetFragment();
    }

    public static AddEditTaskBottomSheetFragment newInstanceForEdit(TaskWithSubTasks item) {
        AddEditTaskBottomSheetFragment fragment = new AddEditTaskBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EDITING_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            editingItem = (TaskWithSubTasks) getArguments().getSerializable(ARG_EDITING_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        edtTitle = view.findViewById(R.id.edt_title);
        edtDescription = view.findViewById(R.id.edt_description);
        btnPickDate = view.findViewById(R.id.btn_pick_date);
        btnPickTime = view.findViewById(R.id.btn_pick_time);
        radioGroupPriority = view.findViewById(R.id.radio_group_priority);
        spinnerReminder = view.findViewById(R.id.spinner_reminder);
        containerSubtaskInputs = view.findViewById(R.id.container_subtask_inputs);
        TextView tvSheetTitle = view.findViewById(R.id.tv_sheet_title);
        Button btnSave = view.findViewById(R.id.btn_save_task);
        ImageButton btnAddSubtask = view.findViewById(R.id.btn_add_subtask);

        // Spinner dùng string-array có sẵn trong strings.xml - không cần vẽ icon/ảnh gì thêm
        ArrayAdapter<CharSequence> reminderAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.reminder_options, android.R.layout.simple_spinner_item);
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(reminderAdapter);

        btnAddSubtask.setOnClickListener(v -> addSubtaskRow(""));

        if (editingItem != null) {
            tvSheetTitle.setText(R.string.title_edit_task);
            prefillFromEditingItem();
        } else {
            tvSheetTitle.setText(R.string.title_add_task);
            updateDateButtonLabel();
            updateTimeButtonLabel();
        }

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void prefillFromEditingItem() {
        Task task = editingItem.task;
        edtTitle.setText(task.getTitle());
        edtDescription.setText(task.getDescription());
        selectedDateTime.setTimeInMillis(task.getDueDate());
        updateDateButtonLabel();
        updateTimeButtonLabel();

        int radioId = task.getPriority() == Task.PRIORITY_HIGH ? R.id.radio_priority_high
                : task.getPriority() == Task.PRIORITY_MEDIUM ? R.id.radio_priority_medium
                : R.id.radio_priority_low;
        radioGroupPriority.check(radioId);
        spinnerReminder.setSelection(task.getReminderOffset());

        for (SubTask subTask : editingItem.subTasks) addSubtaskRow(subTask.getTitle());
    }

    private void addSubtaskRow(String initialText) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_subtask_input, containerSubtaskInputs, false);
        EditText edtSubtaskTitle = row.findViewById(R.id.edt_subtask_title);
        ImageButton btnRemove = row.findViewById(R.id.btn_remove_subtask);
        edtSubtaskTitle.setText(initialText);
        btnRemove.setOnClickListener(v -> containerSubtaskInputs.removeView(row));
        containerSubtaskInputs.addView(row);
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButtonLabel();
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            selectedDateTime.set(Calendar.SECOND, 0);
            updateTimeButtonLabel();
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true).show();
    }

    private void updateDateButtonLabel() { btnPickDate.setText(DateTimeUtils.formatDate(selectedDateTime.getTimeInMillis())); }
    private void updateTimeButtonLabel() { btnPickTime.setText(DateTimeUtils.formatTime(selectedDateTime.getTimeInMillis())); }

    private void saveTask() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (title.isEmpty()) {
            edtTitle.setError(getString(R.string.error_title_required));
            return;
        }

        long dueDateMillis = selectedDateTime.getTimeInMillis();
        if (dueDateMillis <= System.currentTimeMillis()) {
            Toast.makeText(requireContext(), R.string.error_due_date_in_past, Toast.LENGTH_SHORT).show();
            return;
        }

        int priority = mapCheckedIdToPriority(radioGroupPriority.getCheckedRadioButtonId());
        int reminderOffset = spinnerReminder.getSelectedItemPosition();
        List<SubTask> subTasks = collectSubTasksFromInputs();

        if (editingItem == null) {
            Task newTask = new Task(title, description, dueDateMillis, false, priority, reminderOffset);
            taskViewModel.insertTaskWithSubTasks(newTask, subTasks, newTaskId -> {
                Toast.makeText(requireContext(), R.string.msg_task_saved, Toast.LENGTH_SHORT).show();
                dismiss();
            });
        } else {
            Task task = editingItem.task;
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDateMillis);
            task.setPriority(priority);
            task.setReminderOffset(reminderOffset);

            taskViewModel.updateTaskWithSubTasks(task, subTasks);
            Toast.makeText(requireContext(), R.string.msg_task_saved, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private List<SubTask> collectSubTasksFromInputs() {
        List<SubTask> result = new ArrayList<>();
        for (int i = 0; i < containerSubtaskInputs.getChildCount(); i++) {
            EditText edt = containerSubtaskInputs.getChildAt(i).findViewById(R.id.edt_subtask_title);
            String text = edt.getText().toString().trim();
            if (text.isEmpty()) continue;

            // Giữ nguyên trạng thái đã tick nếu mục con này đã tồn tại từ trước (so theo tên),
            // tránh việc sửa Task làm mất tiến độ checklist đã đánh dấu.
            boolean wasCompleted = false;
            if (editingItem != null) {
                for (SubTask old : editingItem.subTasks) {
                    if (old.getTitle().equals(text)) { wasCompleted = old.isCompleted(); break; }
                }
            }
            result.add(new SubTask(0, text, wasCompleted));
        }
        return result;
    }

    private int mapCheckedIdToPriority(int checkedId) {
        if (checkedId == R.id.radio_priority_high) return Task.PRIORITY_HIGH;
        if (checkedId == R.id.radio_priority_medium) return Task.PRIORITY_MEDIUM;
        return Task.PRIORITY_LOW;
    }
}
