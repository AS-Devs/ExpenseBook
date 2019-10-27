package dev.asdevs.expensebook.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

public class DataBaseClient {
    private Context mCtx;
    private static DataBaseClient mInstance;
    public static String DATABASE_NAME = "Expenses";

    //our app database object
    private DataBase dataBase;

    private DataBaseClient(Context mCtx) {
        this.mCtx = mCtx;

        //creating the app database with Room database builder
        //MyToDos is the name of the database
        dataBase = Room.databaseBuilder(mCtx, DataBase.class, DATABASE_NAME).fallbackToDestructiveMigration()
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .build();
    }

    public static synchronized DataBaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DataBaseClient(mCtx);
        }
        return mInstance;
    }

    public DataBase getDataBase() {
        return dataBase;
    }

}
