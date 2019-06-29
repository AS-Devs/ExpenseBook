package asdevs.expensebook.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import asdevs.expensebook.model.Expense;
import asdevs.expensebook.model.User;

@Database(entities = {Expense.class, User.class}, version = 2)
public abstract class DataBase extends RoomDatabase {
    public abstract IDataBase iDataBase();
}
