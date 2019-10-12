package dev.asdevs.expensebook.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import dev.asdevs.expensebook.model.Expense;
import dev.asdevs.expensebook.model.User;

@Database(entities = {Expense.class, User.class}, version = 2, exportSchema = false)
public abstract class DataBase extends RoomDatabase {
    public abstract IDataBase iDataBase();
}
