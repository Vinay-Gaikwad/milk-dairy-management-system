package com.milk.milkrun.collectiondatabase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update; // ✅ Important import added

import com.milk.milkrun.customerdatabase.Customer;

import java.util.List;

@Dao
public interface MilkCollectionDao {

    @Insert
    long insert(MilkCollection milkCollection);

    @Update
    void update(MilkCollection milkCollection);  // ✅ Now update will work properly

    @Query("SELECT * FROM milk_collection WHERE isSynced = 0")

    List<MilkCollection> getUnsyncedRecords();
    @Query("SELECT * FROM milk_collection ORDER BY id DESC LIMIT 2")
    List<MilkCollection> getRecentEntries();

    @Query("UPDATE milk_collection SET isSynced = 1 WHERE id = :id")
    void markAsSynced(int id);

    @Query("SELECT * FROM milk_collection WHERE trDate LIKE :date || '%'")
    List<MilkCollection> getCollectionsForDate(String date);



    @Query("SELECT * FROM milk_collection ORDER BY id DESC")
    List<MilkCollection> getAllCollections();

    @Query("SELECT * FROM milk_collection WHERE partyCode = :customerNo AND trDate = :date AND timePeriod = :timePeriod LIMIT 1")
    MilkCollection getEntryForCustomerDateTime(String customerNo, String date, String timePeriod);

    @Query("SELECT * FROM milk_collection WHERE trDate = :date")
    LiveData<List<MilkCollection>> getCollectionsForDateLive(String date);

    @Query("SELECT * FROM milk_collection ORDER BY id DESC LIMIT 2")
    LiveData<List<MilkCollection>> getRecentEntriesLive();

    @Query("SELECT SUM(qty) FROM milk_collection WHERE trDate = :date")
    double getTotalLitersForDate(String date);

    @Query("SELECT SUM(qty) FROM milk_collection WHERE trDate LIKE :date || '%'")
    double getTotalLitersByDate(String date);

    @Query("SELECT * FROM milk_collection WHERE partyCode = :customerNo AND trDate LIKE :date || '%'")
    List<MilkCollection> getEntriesForCustomerAndDate(String customerNo, String date);


    @Query("SELECT * FROM milk_collection WHERE id = :id LIMIT 1")
    MilkCollection getById(int id);
    @Query("SELECT * FROM milk_collection WHERE id = :billId LIMIT 1")
    MilkCollection getBillById(int billId);

    @Update
    void updateMilkCollection(MilkCollection bill);


    @Query("SELECT * FROM milk_collection")
    LiveData<List<MilkCollection>> getAllCollectionsLive();

    @Query("SELECT * FROM milk_collection WHERE trDate LIKE :fullDate")
    LiveData<List<MilkCollection>> getLiveCollectionsForDate(String fullDate);

    @Query("SELECT * FROM milk_collection ORDER BY id DESC LIMIT 10")
    LiveData<List<MilkCollection>> getLiveRecentEntries();



    @Query("SELECT * FROM milk_collection WHERE trDate LIKE :today OR trDate LIKE :yesterday")
    LiveData<List<MilkCollection>> getMilkForTodayAndYesterday(String today, String yesterday);





}
