package dev.asdevs.expensebook.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.asdevs.expensebook.model.Expense;
import dev.asdevs.expensebook.model.User;

@Dao
public interface IDataBase {

    @Query("SELECT * FROM expense")
    List<Expense> getAllExpenses();

    @Query("SELECT * FROM user")
    List<User> getAllUsers();

    @Insert
    void createExpense(Expense expense);

    @Insert
    void createUser(User user);

    @Delete
    void deleteExpense(Expense expense);

    @Delete
    void deleteUser(User user);

    @Update
    void updateExpense(Expense expense);

    @Update
    void updateUser(User user);

    @Update
    void resetUserExpense(User user);
}
