// PendingRequest.java

package com.milk.milkrun;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_requests")
public class PendingRequest {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "json_data")
    public String jsonData;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "retry_count")
    public int retryCount = 0;

    @ColumnInfo(name = "last_attempt")
    public String lastAttempt;
}