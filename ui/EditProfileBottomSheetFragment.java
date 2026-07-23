package com.example.todolist.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.R;
import com.example.todolist.model.User;
import com.example.todolist.util.PasswordUtils;
import com.example.todolist.util.SessionManager;
import com.example.todolist.viewmodel.UserViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditProfileBottomSheetFragment extends BottomSheetDialogFragment {

    private UserViewModel userViewModel;
    private User currentUser;
    private EditText edtDisplayName, edtNewPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtDisplayName = view.findViewById(R.id.edt_display_name);
        edtNewPassword = view.findViewById(R.id.edt_new_password);
        Button btnSave = view.findViewById(R.id.btn_save_profile);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        int userId = SessionManager.getLoggedInUserId(requireContext());

        userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            currentUser = user;
            if (edtDisplayName.getText().toString().isEmpty()) {
                edtDisplayName.setText(user.getDisplayName());
            }
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String newDisplayName = edtDisplayName.getText().toString().trim();
        if (newDisplayName.isEmpty()) {
            edtDisplayName.setError(getString(R.string.error_display_name_required));
            return;
        }
        currentUser.setDisplayName(newDisplayName);

        String newPassword = edtNewPassword.getText().toString();
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                edtNewPassword.setError(getString(R.string.error_password_too_short));
                return;
            }
            String newSalt = PasswordUtils.generateSalt();
            currentUser.setPasswordSalt(newSalt);
            currentUser.setPasswordHash(PasswordUtils.hashPassword(newPassword, newSalt));
        }

        userViewModel.updateUser(currentUser);
        Toast.makeText(requireContext(), R.string.msg_profile_updated, Toast.LENGTH_SHORT).show();
        dismiss();
    }
}