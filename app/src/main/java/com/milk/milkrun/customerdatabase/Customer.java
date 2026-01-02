package com.milk.milkrun.customerdatabase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "customers",
        indices = {
                @Index(value = {"number"}, unique = true),
                @Index(value = {"name"})
        }
)
public class Customer {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "number")
    public String number;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "mobile")
    public String mobile;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "fatType")
    public int fatType;

    // ✅ Required no-argument constructor
    public Customer() {
    }

    // ✅ Constructor with all fields
    public Customer(String number, String name, String address, String mobile, String date, int fatType) {
        this.number = number;
        this.name = name;
        this.address = address;
        this.mobile = mobile;
        this.date = date;
        this.fatType = fatType;
    }

    // ✅ Short constructor for simple use
    public Customer(String number, String name) {
        this.number = number;
        this.name = name;
        this.address = "";
        this.mobile = "";
        this.date = "";
        this.fatType = 1;
    }

}
