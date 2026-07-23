package com.example.todolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.R;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.util.SessionManager;
import com.example.todolist.util.ThemePreferences;
import com.example.todolist.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private boolean isRegisterMode = false;
    private UserViewModel userViewModel;

    private TextInputLayout layoutDisplayName, layoutConfirmPassword;
    private EditText edtUsername, edtDisplayName, edtPassword, edtConfirmPassword;
    private Button btnSubmit;
    private TextView tvToggleMode, tvError, tvSubtitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemePreferences.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        // Đã đăng nhập từ trước (session còn lưu trong SharedPreferences) -> vào thẳng MainActivity
        if (SessionManager.isLoggedIn(this)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        edtUsername = findViewById(R.id.edt_username);
        edtDisplayName = findViewById(R.id.edt_display_name);
        edtPassword = findViewById(R.id.edt_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        layoutDisplayName = findViewById(R.id.layout_display_name);
        layoutConfirmPassword = findViewById(R.id.layout_confirm_password);
        btnSubmit = findViewById(R.id.btn_submit);
        tvToggleMode = findViewById(R.id.tv_toggle_mode);
        tvError = findViewById(R.id.tv_error);
        tvSubtitle = findViewById(R.id.tv_subtitle);

        updateModeUI();

        tvToggleMode.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            updateModeUI();
        });

        btnSubmit.setOnClickListener(v -> {
            if (isRegisterMode) attemptRegister(); else attemptLogin();
        });
    }

    private void updateModeUI() {
        layoutDisplayName.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);
        layoutConfirmPassword.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);
        btnSubmit.setText(isRegisterMode ? R.string.action_register : R.string.action_login);
        tvToggleMode.setText(isRegisterMode ? R.string.msg_have_account : R.string.msg_no_account);
        tvSubtitle.setText(isRegisterMode ? R.string.msg_register_subtitle : R.string.msg_login_subtitle);
        tvError.setVisibility(View.GONE);
    }

    private void attemptLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_fields_required));
            return;
        }

        userViewModel.login(username, password, new UserRepository.OnAuthResultListener() {
            @Override public void onSuccess(User user) {
                SessionManager.setLoggedInUserId(LoginActivity.this, user.getId());
                goToMain();
            }
            @Override public void onFailure(String errorMessage) { showError(errorMessage); }
        });
    }

    private void attemptRegister() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();
        String displayName = edtDisplayName.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_fields_required));
            return;
        }
        if (password.length() < 6) {
            showError(getString(R.string.error_password_too_short));
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(getString(R.string.error_password_mismatch));
            return;
        }
        if (displayName.isEmpty()) displayName = username;

        userViewModel.register(username, password, displayName, new UserRepository.OnAuthResultListener() {
            @Override public void onSuccess(User user) {
                SessionManager.setLoggedInUserId(LoginActivity.this, user.getId());
                goToMain();
            }
            @Override public void onFailure(String errorMessage) { showError(errorMessage); }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}