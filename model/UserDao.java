package com.example.todolist.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    // Chỉ đọc 1 lần lúc đăng nhập/đăng ký để so khớp mật khẩu -> không cần LiveData
    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    // LiveData vì UserFragment cần tự cập nhật khi displayName/mật khẩu đổi
    @Query("SELECT * FROM user_table WHERE id = :userId LIMIT 1")
    LiveData<User> getUserById(int userId);
}