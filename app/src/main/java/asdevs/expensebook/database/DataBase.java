package asdevs.expensebook.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import asdevs.expensebook.model.Expense;

@Database(entities = {Expense.class}, version = 1)
public abstract class DataBase extends RoomDatabase {
    public abstract IDataBase iDataBase();
}
