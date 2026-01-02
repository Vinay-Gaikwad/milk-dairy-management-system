package com.milk.milkrun.localrate;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "milk_rate")
public class LocalMilkRate {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int mlkTypeCode;
    public String fat;
    public String snf;
    public String rate;
}
