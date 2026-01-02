package com.milk.milkrun.collectiondatabase;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "milk_collection")
public class MilkCollection {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String aTrNo;         // Auto-generated transaction number
    public String trNo;          // Auto-generated transaction number
    public String mlkTrType;     // Auto-generated milk transaction type

    public String softCustCode;  // From LoginActivity
    public String trDate;        // Date
    public String partyCode;     // Customer No
    public String mlkTypeCode;   // 1 for Buffalo, 2 for Cow (as string to match API schema)

    public double qty;           // Liter
    public double fat;
    public double snf;
    public double rate;
    public double amt;           // Total

    public int meCode;           // 1 for Morning, 2 for Evening
    public String time;
    public String timePeriod;

    public boolean isSynced = false;
}
