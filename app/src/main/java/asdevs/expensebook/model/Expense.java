package asdevs.expensebook.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;

import asdevs.expensebook.database.DateConverter;

@Entity
public class Expense implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "amount")
    private double amount;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "date")
    @TypeConverters(DateConverter.class)
    private Date date;

    @Ignore
    public Expense(int id, String name, double amount, String type, Date date) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public Expense(String name, double amount, String type, Date date){
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double money) {
        this.amount = money;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
