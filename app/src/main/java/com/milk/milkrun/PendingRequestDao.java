package com.milk.milkrun;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

// PendingRequestDao.java
@Dao
public interface PendingRequestDao {
    @Insert
    void insert(PendingRequest request);

    @Query("SELECT * FROM pending_requests ORDER BY created_at ASC")
    List<PendingRequest> getAllPendingRequests();

    @Query("DELETE FROM pending_requests WHERE id = :id")
    void deleteById(int id);

    @Query("UPDATE pending_requests SET retry_count = retry_count + 1, last_attempt = :timestamp WHERE id = :id")
    void incrementRetryCount(int id, String timestamp);
}
