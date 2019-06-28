package asdevs.expensebook.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import asdevs.expensebook.model.Expense;

@Dao
public interface IDataBase {

    @Query("SELECT * FROM expense")
    List<Expense> getAllExpenses();

    @Insert
    void createExpense(Expense expense);

    @Delete
    void deleteExpense(Expense expense);

    @Update
    void updateExpense(Expense expense);
}
