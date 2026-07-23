package com.example.todolist.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table", indices = {@Index(value = "username", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String username;

    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "password_salt")
    private String passwordSalt;

    @ColumnInfo(name = "display_name")
    private String displayName;

    public User(@NonNull String username, String passwordHash, String passwordSalt, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.displayName = displayName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    @NonNull public String getUsername() { return username; }
    public void setUsername(@NonNull String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}