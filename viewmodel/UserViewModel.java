package com.example.todolist.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void login(String username, String password, UserRepository.OnAuthResultListener listener) {
        repository.login(username, password, listener);
    }

    public void register(String username, String password, String displayName, UserRepository.OnAuthResultListener listener) {
        repository.register(username, password, displayName, listener);
    }

    public LiveData<User> getUserById(int userId) {
        return repository.getUserById(userId);
    }

    public void updateUser(User user) {
        repository.updateUser(user);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdownExecutor();
    }
}