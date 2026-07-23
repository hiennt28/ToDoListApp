package com.example.todolist.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.todolist.R;
import com.example.todolist.model.AppDatabase;
import com.example.todolist.model.User;
import com.example.todolist.model.UserDao;
import com.example.todolist.util.PasswordUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDao userDao;
    private final Application application;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface OnAuthResultListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public UserRepository(Application application) {
        this.application = application;
        AppDatabase database = AppDatabase.getInstance(application);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void register(String username, String rawPassword, String displayName, OnAuthResultListener listener) {
        executorService.execute(() -> {
            if (userDao.findByUsername(username) != null) {
                mainHandler.post(() -> listener.onFailure(application.getString(R.string.error_username_taken)));
                return;
            }
            String salt = PasswordUtils.generateSalt();
            String hash = PasswordUtils.hashPassword(rawPassword, salt);
            User newUser = new User(username, hash, salt, displayName);
            long id = userDao.insert(newUser);
            newUser.setId((int) id);
            mainHandler.post(() -> listener.onSuccess(newUser));
        });
    }

    public void login(String username, String rawPassword, OnAuthResultListener listener) {
        executorService.execute(() -> {
            User user = userDao.findByUsername(username);
            boolean valid = user != null && PasswordUtils.verifyPassword(rawPassword, user.getPasswordHash(), user.getPasswordSalt());
            if (!valid) {
                mainHandler.post(() -> listener.onFailure(application.getString(R.string.error_login_failed)));
                return;
            }
            mainHandler.post(() -> listener.onSuccess(user));
        });
    }

    public LiveData<User> getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public void updateUser(User user) {
        executorService.execute(() -> userDao.update(user));
    }

    public void shutdownExecutor() {
        executorService.shutdown();
    }
}