package dev.asdevs.expensebook.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import dev.asdevs.expensebook.model.Expense;
import dev.asdevs.expensebook.model.User;

@Database(entities = {Expense.class, User.class}, version = 2, exportSchema = false)
public abstract class DataBase extends RoomDatabase {
    public abstract IDataBase iDataBase();
}
