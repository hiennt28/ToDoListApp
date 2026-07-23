package com.example.todolist.ui;

import android.content.Intent;
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
import com.example.todolist.model.TaskWithSubTasks;
import com.example.todolist.model.User;
import com.example.todolist.util.SessionManager;
import com.example.todolist.util.ThemePreferences;
import com.example.todolist.viewmodel.TaskViewModel;
import com.example.todolist.viewmodel.UserViewModel;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;
import java.util.Locale;

public class UserFragment extends Fragment {

    private TextView tvTotalTasks, tvCompletedTasks, tvDisplayName, tvUsername, tvAvatarInitial;
    private CircularProgressView progressRing;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalTasks = view.findViewById(R.id.tv_total_tasks);
        tvCompletedTasks = view.findViewById(R.id.tv_completed_tasks);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvUsername = view.findViewById(R.id.tv_username);
        tvAvatarInitial = view.findViewById(R.id.tv_avatar_initial);
        progressRing = view.findViewById(R.id.progress_ring);

        MaterialSwitch switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchDarkMode.setChecked(ThemePreferences.isDarkModeEnabled(requireContext()));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemePreferences.setDarkModeEnabled(requireContext(), isChecked));

        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        int userId = SessionManager.getLoggedInUserId(requireContext());
        userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), this::bindUserInfo);

        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                new EditProfileBottomSheetFragment().show(getParentFragmentManager(), "EditProfile"));

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SessionManager.logout(requireContext());
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        TaskViewModel taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskViewModel.getAllTasksWithSubTasks().observe(getViewLifecycleOwner(), this::updateStatistics);
    }

    private void bindUserInfo(User user) {
        if (user == null) return;
        tvDisplayName.setText(user.getDisplayName());
        tvUsername.setText("@" + user.getUsername());
        String initial = user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                ? user.getDisplayName().substring(0, 1).toUpperCase(Locale.getDefault()) : "?";
        tvAvatarInitial.setText(initial);
    }

    private void updateStatistics(List<TaskWithSubTasks> tasks) {
        int completed = 0;
        for (TaskWithSubTasks item : tasks) if (item.task.isCompleted()) completed++;
        tvTotalTasks.setText(String.valueOf(tasks.size()));
        tvCompletedTasks.setText(String.valueOf(completed));
        float percent = tasks.isEmpty() ? 0f : (completed * 100f / tasks.size());
        progressRing.setProgressPercent(percent);
    }
}