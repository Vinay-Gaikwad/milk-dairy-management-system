// SyncWorker.java
package com.milk.milkrun.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.milk.milkrun.collectiondatabase.MilkCollection;
import com.milk.milkrun.repository.MilkRepository;

import java.util.List;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private final MilkRepository repository;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.repository = new MilkRepository(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            List<MilkCollection> unsynced = repository.getUnsyncedRecords();
            Log.d(TAG, "Syncing " + unsynced.size() + " records");
            for (MilkCollection record : unsynced) {
                repository.syncRecord(record);
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Sync failed", e);
            return Result.retry();
        }
    }
}