package com.example.todolist.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.model.SubTask;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskWithSubTasks;
import com.example.todolist.notification.AlarmScheduler;
import com.example.todolist.viewmodel.TaskViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {

    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private View layoutEmptyState;
    private ChipGroup chipGroupTopics;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        bindGreetingHeader(view);
        bindSearchBar(view);
        chipGroupTopics = view.findViewById(R.id.chip_group_topics);

        TextView tvProgressLabel = view.findViewById(R.id.tv_progress_label);
        ProgressBar progressToday = view.findViewById(R.id.progress_today);

        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(this);
        recyclerView.setAdapter(taskAdapter);

        taskViewModel.getTodayProgress().observe(getViewLifecycleOwner(), progress -> {
            progressToday.setMax(Math.max(progress.total, 1));
            progressToday.setProgress(progress.completed);
            tvProgressLabel.setText(getString(R.string.label_today_progress, progress.completed, progress.total));
        });

        taskViewModel.getDistinctTopics().observe(getViewLifecycleOwner(), this::rebuildTopicChips);

        taskViewModel.getFilteredTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
            boolean isEmpty = tasks == null || tasks.isEmpty();
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
    }

    private void bindGreetingHeader(View view) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        TextView tvDate = view.findViewById(R.id.tv_today_date);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetingRes = hour < 11 ? R.string.greeting_morning : hour < 18 ? R.string.greeting_afternoon : R.string.greeting_evening;
        tvGreeting.setText(greetingRes);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'tháng' M", new Locale("vi", "VN"));
        String dateLabel = sdf.format(new Date());
        tvDate.setText(dateLabel.substring(0, 1).toUpperCase(Locale.getDefault()) + dateLabel.substring(1));
    }

    private void bindSearchBar(View view) {
        TextInputEditText edtSearch = view.findViewById(R.id.edt_search);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                taskViewModel.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void rebuildTopicChips(List<String> topics) {
        int previouslyCheckedId = chipGroupTopics.getCheckedChipId();
        String previouslySelectedText = null;
        if (previouslyCheckedId != View.NO_ID) {
            Chip checkedChip = chipGroupTopics.findViewById(previouslyCheckedId);
            if (checkedChip != null) previouslySelectedText = checkedChip.getText().toString();
        }

        chipGroupTopics.removeAllViews();

        Chip chipAll = createTopicChip(getString(R.string.filter_all));
        chipGroupTopics.addView(chipAll);

        boolean reselected = false;
        for (String topic : topics) {
            Chip chip = createTopicChip(topic);
            chipGroupTopics.addView(chip);
            if (topic.equals(previouslySelectedText)) {
                chip.setChecked(true);
                reselected = true;
            }
        }
        if (!reselected) chipAll.setChecked(true);

        chipGroupTopics.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip checked = group.findViewById(checkedIds.get(0));
            String selectedText = checked != null ? checked.getText().toString() : "";
            taskViewModel.setSelectedTopic(selectedText.equals(getString(R.string.filter_all)) ? "" : selectedText);
        });
    }

    private Chip createTopicChip(String text) {
        Chip chip = (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.chip_choice, chipGroupTopics, false);
        chip.setId(View.generateViewId());
        chip.setText(text);
        return chip;
    }

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        taskViewModel.updateTask(task);
    }

    @Override
    public void onSubTaskCheckedChanged(SubTask subTask, boolean isChecked) {
        subTask.setCompleted(isChecked);
        taskViewModel.updateSubTask(subTask);
    }

    @Override
    public void onEditClicked(TaskWithSubTasks item) {
        AddEditTaskBottomSheetFragment.newInstanceForEdit(item)
                .show(getParentFragmentManager(), "EditTaskBottomSheet");
    }

    @Override
    public void onDeleteClicked(TaskWithSubTasks item) {
        AlarmScheduler.cancelTaskAlarm(requireContext(), item.task);
        taskViewModel.deleteTask(item.task);
    }
}