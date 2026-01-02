// MilkApp.java
package com.milk.milkrun;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.milk.milkrun.workers.SyncWorker;

import java.util.concurrent.TimeUnit;

public class MilkApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        schedulePeriodicSync();
    }

    private void schedulePeriodicSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                30, // Repeat every 30 minutes
                TimeUnit.MINUTES
        ).setConstraints(constraints).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "milk_sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }
}