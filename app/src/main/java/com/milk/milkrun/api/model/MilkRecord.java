// MilkRecord.java
package com.milk.milkrun.api.model;

import com.google.gson.annotations.SerializedName;

public class MilkRecord {
    @SerializedName("id")
    public String id;

    @SerializedName("customerNo")
    public String customerNo;

    @SerializedName("name")
    public String name;

    @SerializedName("milkType")
    public String milkType;

    @SerializedName("timePeriod")
    public String timePeriod;

    @SerializedName("date")
    public String date;

    @SerializedName("time")
    public String time;

    @SerializedName("liter")
    public double liter;

    @SerializedName("fat")
    public double fat;

    @SerializedName("rate")
    public double rate;

    @SerializedName("total")
    public double total;


    public MilkRecord() {
        this.customerNo = customerNo;
        this.name = name;
        this.milkType = milkType;
        this.timePeriod = timePeriod;
        this.date = date;
        this.time = time;
        this.liter = liter;
        this.fat = fat;
        this.rate = rate;
        this.total = total;
    }
}