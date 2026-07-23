package com.example.todolist.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.R;
import com.example.todolist.model.Task;
import com.example.todolist.viewmodel.TaskViewModel;

import java.util.List;

/**
 * Minh họa việc dùng CHUNG 1 TaskViewModel (Activity-scope) giữa nhiều Fragment:
 * ProfileFragment không hề gọi lại Repository, chỉ observe đúng LiveData mà
 * HomeFragment cũng đang observe - dữ liệu luôn đồng bộ 100% giữa các tab.
 */
public class UserFragment extends Fragment {

    private TextView tvTotalTasks, tvCompletedTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalTasks = view.findViewById(R.id.tv_total_tasks);
        tvCompletedTasks = view.findViewById(R.id.tv_completed_tasks);

        TaskViewModel taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), this::updateStatistics);
    }

    private void updateStatistics(List<Task> tasks) {
        int completed = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) completed++;
        }
        tvTotalTasks.setText(getString(R.string.label_total_tasks, tasks.size()));
        tvCompletedTasks.setText(getString(R.string.label_completed_tasks, completed));
    }
}