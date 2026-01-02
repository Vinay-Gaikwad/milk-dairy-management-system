package com.milk.milkrun.localrate;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MilkRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocalMilkRate rate);

    @Query("SELECT * FROM milk_rate")
    List<LocalMilkRate> getAllRates();

    @Query("DELETE FROM milk_rate")
    void clearRates();
}
